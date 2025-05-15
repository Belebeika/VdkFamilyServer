package com.example.vdkfamilyserver.Controllers.Admin;

import com.example.vdkfamilyserver.Models.Forum.ForumAttachment;
import com.example.vdkfamilyserver.Models.Forum.ForumPost;
import com.example.vdkfamilyserver.Models.Forum.ForumTopic;
import com.example.vdkfamilyserver.Repositories.Forum.ForumPostRepository;
import com.example.vdkfamilyserver.Repositories.Forum.ForumTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Controller
@RequestMapping("/admin/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumTopicRepository topicRepository;
    private final ForumPostRepository postRepository;

    private boolean hasCycle(ForumPost start, Set<Long> visited) {
        ForumPost current = start.getParent();
        while (current != null) {
            if (!visited.add(current.getId())) return true;
            current = current.getParent();
        }
        return false;
    }

    @GetMapping
    public String forumIndex(Model model) {
        List<ForumTopic> topics = topicRepository.findAll();
        model.addAttribute("topics", topics);
        return "admin/forum/index";
    }

    @GetMapping("/topic/{id}")
    public String viewTopic(@PathVariable Long id, Model model) {
        ForumTopic topic = topicRepository.findById(id).orElseThrow();
        List<ForumPost> allPosts = postRepository.findByTopicIdOrderByCreatedAtAsc(id);

        Map<Long, ForumPost> postsById = new HashMap<>();
        for (ForumPost post : allPosts) {
            postsById.put(post.getId(), post);
        }

        model.addAttribute("posts", allPosts);
        model.addAttribute("postsById", postsById); // пригодится для отображения ссылок на родителей
        model.addAttribute("topic", topic);

        return "admin/forum/topic";
    }

    @PostMapping("/topic/{id}/delete")
    public String deleteTopic(@PathVariable Long id) {
        Optional<ForumTopic> topicOptional = topicRepository.findById(id);
        if (topicOptional.isPresent()) {
            ForumTopic topic = topicOptional.get();

            // Проходим по всем постам и их вложениям
            for (ForumPost post : topic.getPosts()) {
                for (ForumAttachment attachment : post.getAttachments()) {
                    try {
                        Path path = Path.of(attachment.getUrl());
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        System.err.println("Не удалось удалить файл: " + attachment.getUrl());
                        e.printStackTrace();
                    }
                }
            }

            // Удаление самой темы (каскадно удалит посты и вложения)
            topicRepository.delete(topic);
        }

        return "redirect:/admin/forum";
    }

    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id, @RequestHeader("Referer") String referer) {
        ForumPost post = postRepository.findById(id).orElse(null);
        if (post != null) {
            // Удаляем файлы, связанные с вложениями
            for (ForumAttachment attachment : post.getAttachments()) {
                try {
                    Path path = Path.of(attachment.getUrl());
                    Files.deleteIfExists(path); // Безопасно: не выбрасывает исключение, если файл уже удалён
                } catch (IOException e) {
                    // Логгировать можно, чтобы видеть ошибку
                    System.err.println("Не удалось удалить файл: " + attachment.getUrl());
                    e.printStackTrace();
                }
            }

            // Удаляем сам пост (вложения удаляются каскадно)
            postRepository.delete(post);
        }

        return "redirect:" + referer;
    }

    @GetMapping("/post/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост не найден"));
        model.addAttribute("post", post);
        return "admin/forum/edit_post";
    }

    @PostMapping("/post/{id}/edit")
    public String editPostSave(@PathVariable Long id, @RequestParam("content") String content) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост не найден"));
        post.setContent(content);
        postRepository.save(post);
        return "redirect:/admin/forum/topic/" + post.getTopic().getId();
    }
}

