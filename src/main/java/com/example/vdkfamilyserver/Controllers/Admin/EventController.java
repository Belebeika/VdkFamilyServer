package com.example.vdkfamilyserver.Controllers.Admin;

import com.example.vdkfamilyserver.Models.Event.Event;
import com.example.vdkfamilyserver.Models.Event.EventBlock;
import com.example.vdkfamilyserver.Repositories.Event.EventBlockRepository;
import com.example.vdkfamilyserver.Repositories.Event.EventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;


@Controller
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepo;
    private final EventBlockRepository blockRepo;
    private final Path imageRoot = Paths.get("uploads/EVENTS");

    @GetMapping
    public String listEvents(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Event> events = eventRepo.findAll(PageRequest.of(page, 10, Sort.by("eventDate").descending()));
        model.addAttribute("events", events.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", events.getTotalPages());
        return "admin/eventboard/events";
    }

    @GetMapping("/new")
    public String newEvent(Model model) {
        return "admin/eventboard/event_add";
    }

    @PostMapping("/save")
    @Transactional
    public String saveEvent(@RequestParam String title,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                           @RequestParam Map<String, String> params,
                           @RequestParam Map<String, MultipartFile> files,
                           Model model) throws IOException {

        Event event = new Event();
        event.setTitle(title);
        event.setEventDate(eventDate);

        event = eventRepo.save(event);

        Files.createDirectories(imageRoot);

        List<EventBlock> eventBlocks = new ArrayList<>();
        int i = 0;
        while (true) {
            String typeKey = "block_type_" + i;
            String contentKey = "block_content_" + i;
            String positionKey = "block_position_" + i;

            if (!params.containsKey(typeKey) || !params.containsKey(positionKey)) break;

            String typeStr = params.get(typeKey);
            String content = params.get(contentKey);
            MultipartFile file = files.get(contentKey);

            EventBlock block = new EventBlock();
            block.setEvent(event);
            block.setType(EventBlock.BlockType.valueOf(typeStr));
            block.setPosition(Integer.parseInt(params.get(positionKey)));

            if (block.getType() == EventBlock.BlockType.IMAGE) {
                String ext = Objects.requireNonNull(file.getOriginalFilename())
                        .substring(file.getOriginalFilename().lastIndexOf('.'));
                String filename = UUID.randomUUID() + ext;
                Path dest = imageRoot.resolve(filename);
                Files.copy(file.getInputStream(), dest);
                block.setContent("/uploads/EVENTS/" + filename);
            } else {
                block.setContent(content);
            }

            eventBlocks.add(block);
            i++;
        }

        blockRepo.saveAll(eventBlocks);
        return "redirect:/admin/events";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        Event event = eventRepo.findById(id).orElse(null);
        if (event != null) {
            for (EventBlock block : event.getBlocks()) {
                if (block.getType() == EventBlock.BlockType.IMAGE) {
                    Path p = Paths.get(block.getContent().replace("/uploads/", "uploads/"));
                    Files.deleteIfExists(p);
                }
            }
            eventRepo.delete(event);
            redirectAttributes.addFlashAttribute("message", "Мероприятие удалено");
        } else {
            redirectAttributes.addFlashAttribute("error", "Мероприятие не найдено");
        }
        return "redirect:/admin/events";
    }

    @GetMapping("/edit/{id}")
    public String editEvent(@PathVariable Long id, Model model) throws JsonProcessingException {
        Event event = eventRepo.findById(id).orElseThrow();
        List<EventBlock> blocks = event.getBlocks();

        // Сериализация блоков в JSON для фронта
        ObjectMapper mapper = new ObjectMapper();
        String blocksJson = mapper.writeValueAsString(
                blocks.stream().map(b -> Map.of(
                        "type", b.getType().name(),
                        "content", b.getContent()
                )).toList()
        );

        model.addAttribute("event", event);
        model.addAttribute("blocksJson", blocksJson);
        return "admin/eventboard/event_edit";
    }

    @PostMapping("/update/{id}")
    @Transactional
    public String updateEvent(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                             @RequestParam Map<String, String> params,
                             @RequestParam Map<String, MultipartFile> files,
                             RedirectAttributes redirectAttributes) throws IOException {

        Event event = eventRepo.findById(id).orElseThrow();
        event.setTitle(title);
        event.setEventDate(eventDate);

        Files.createDirectories(imageRoot);
        List<EventBlock> newBlocks = new ArrayList<>();
        List<String> preservedImages = new ArrayList<>();

        int i = 0;
        while (true) {
            String typeKey = "block_type_" + i;
            String contentKey = "block_content_" + i;
            String positionKey = "block_position_" + i;
            String existingKey = "block_content_existing_" + i;

            if (!params.containsKey(typeKey) || !params.containsKey(positionKey)) break;

            String typeStr = params.get(typeKey);
            String content = params.get(contentKey);
            String existing = params.get(existingKey);
            MultipartFile file = files.get(contentKey);

            EventBlock.BlockType type = EventBlock.BlockType.valueOf(typeStr);
            String finalContent;

            if (type == EventBlock.BlockType.IMAGE) {
                if (file != null && !file.isEmpty()) {
                    String ext = Objects.requireNonNull(file.getOriginalFilename())
                            .substring(file.getOriginalFilename().lastIndexOf('.'));
                    String filename = UUID.randomUUID() + ext;
                    Path dest = imageRoot.resolve(filename);
                    Files.copy(file.getInputStream(), dest);
                    finalContent = "/uploads/EVENTS/" + filename;
                } else {
                    finalContent = existing;
                    preservedImages.add(existing.replace("/uploads/", "uploads/"));
                }
            } else {
                finalContent = content;
            }

            EventBlock block = new EventBlock();
            block.setEvent(event);
            block.setType(type);
            block.setPosition(Integer.parseInt(params.get(positionKey)));
            block.setContent(finalContent);

            newBlocks.add(block);
            i++;
        }

        for (EventBlock block : event.getBlocks()) {
            if (block.getType() == EventBlock.BlockType.IMAGE) {
                String relPath = block.getContent().replace("/uploads/", "uploads/");
                if (!preservedImages.contains(relPath)) {
                    Files.deleteIfExists(Paths.get(relPath));
                }
            }
        }

        blockRepo.deleteAll(event.getBlocks());
        event.getBlocks().clear();
        blockRepo.saveAll(newBlocks);

        redirectAttributes.addFlashAttribute("message", "Мероприятие обновлено");
        return "redirect:/admin/events";
    }
}
