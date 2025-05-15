package com.example.vdkfamilyserver.Controllers.Admin;

import com.example.vdkfamilyserver.Models.Image;
import com.example.vdkfamilyserver.Models.News;
import com.example.vdkfamilyserver.Repositories.NewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/admin/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsRepository newsRepo;
    private final Path root = Paths.get("uploads/NEWS");

    @GetMapping
    public String listNews(Model model) {
        List<News> newsList = newsRepo.findAll();
        model.addAttribute("newsList", newsList);
        return "admin/newsboard/news";
    }

    @PostMapping("/upload")
    public String uploadNews(@RequestParam("image") MultipartFile file,
                             @RequestParam("linkUrl") String linkUrl,
                             @RequestParam("title") String title,
                             RedirectAttributes redirectAttributes) throws IOException {

        Files.createDirectories(root);

        String ext = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf('.'));
        String filename = UUID.randomUUID() + ext;
        Path dest = root.resolve(filename);
        Files.copy(file.getInputStream(), dest);

        Image img = new Image();
        img.setImageUrl("/uploads/NEWS/" + filename);

        News news = new News();
        news.setTitle(title);
        news.setLinkUrl(linkUrl);
        news.setImage(img);

        newsRepo.save(news);
        redirectAttributes.addFlashAttribute("message", "Новость добавлена");
        return "redirect:/admin/news";
    }

    @GetMapping("/edit/{id}")
    public String editNews(@PathVariable Long id, Model model) {
        News news = newsRepo.findById(id).orElseThrow();
        model.addAttribute("news", news);
        return "admin/newsboard/news_edit";
    }

    @PostMapping("/edit/{id}")
    public String updateNews(@PathVariable Long id,
                             @RequestParam("title") String title,
                             @RequestParam("linkUrl") String linkUrl,
                             @RequestParam(value = "image", required = false) MultipartFile newImage,
                             RedirectAttributes redirectAttributes) throws IOException {

        News news = newsRepo.findById(id).orElseThrow();
        news.setTitle(title);
        news.setLinkUrl(linkUrl);

        if (newImage != null && !newImage.isEmpty()) {
            String oldPath = news.getImage().getImageUrl().replace("/uploads/", "uploads/");
            Files.deleteIfExists(Paths.get(oldPath));

            String ext = newImage.getOriginalFilename().substring(newImage.getOriginalFilename().lastIndexOf('.'));
            String filename = UUID.randomUUID() + ext;
            Path newPath = root.resolve(filename);
            Files.createDirectories(newPath.getParent());
            Files.copy(newImage.getInputStream(), newPath);
            news.getImage().setImageUrl("/uploads/NEWS/" + filename);
        }

        newsRepo.save(news);
        redirectAttributes.addFlashAttribute("message", "Новость обновлена");
        return "redirect:/admin/news";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteNews(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        News news = newsRepo.findById(id).orElse(null);

        if (news != null) {
            if (news.getImage() != null) {
                String path = news.getImage().getImageUrl().replace("/uploads/", "uploads/");
                Files.deleteIfExists(Paths.get(path));
            }
            newsRepo.delete(news);
            redirectAttributes.addFlashAttribute("message", "Новость удалена");
        } else {
            redirectAttributes.addFlashAttribute("error", "Новость не найдена");
        }

        return "redirect:/admin/news";
    }
}
