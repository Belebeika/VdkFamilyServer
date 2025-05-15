package com.example.vdkfamilyserver.Controllers.Admin;

import com.example.vdkfamilyserver.Models.Banner;
import com.example.vdkfamilyserver.Models.Image;
import com.example.vdkfamilyserver.Repositories.BannerRepository;
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
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerRepository bannerRepository;
    private final Path root = Paths.get("uploads/BANNERS");

    @GetMapping
    public String listBanners(Model model) {
        List<Banner> banners = bannerRepository.findAll();
        model.addAttribute("banners", banners);
        return "admin/bannerboard/banners";
    }

    @PostMapping("/upload")
    public String uploadBanner(@RequestParam("image") MultipartFile file,
                               @RequestParam("linkUrl") String linkUrl,
                               RedirectAttributes redirectAttributes) throws IOException {

        Files.createDirectories(root);

        String ext = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf('.'));
        String filename = UUID.randomUUID() + ext;
        Path dest = root.resolve(filename);
        Files.copy(file.getInputStream(), dest);

        Banner banner = new Banner();
        banner.setLinkUrl(linkUrl);

        Image img = new Image();
        img.setImageUrl("/uploads/BANNERS/" + filename);

        banner.setImage(img);

        bannerRepository.save(banner);
        redirectAttributes.addFlashAttribute("message", "Баннер добавлен");
        return "redirect:/admin/banners";
    }

    @GetMapping("/edit/{id}")
    public String editBanner(@PathVariable Long id, Model model) {
        Banner banner = bannerRepository.findById(id).orElseThrow();
        model.addAttribute("banner", banner);
        return "admin/bannerboard/banners";
    }

    @PostMapping("/edit/{id}")
    public String updateBanner(@PathVariable Long id,
                               @RequestParam("linkUrl") String linkUrl,
                               @RequestParam(value = "image", required = false) MultipartFile newImage,
                               RedirectAttributes redirectAttributes) throws IOException {

        Banner banner = bannerRepository.findById(id).orElseThrow();
        banner.setLinkUrl(linkUrl);

        if (newImage != null && !newImage.isEmpty()) {
            String oldPath = banner.getImage().getImageUrl().replace("/uploads/", "uploads/");
            Files.deleteIfExists(Paths.get(oldPath));
            String ext = newImage.getOriginalFilename().substring(newImage.getOriginalFilename().lastIndexOf('.'));
            String filename = UUID.randomUUID() + ext;
            Path newPath = root.resolve(filename);
            Files.createDirectories(newPath.getParent());
            Files.copy(newImage.getInputStream(), newPath);
            banner.getImage().setImageUrl("/uploads/BANNERS/" + filename);
        }


        bannerRepository.save(banner);
        redirectAttributes.addFlashAttribute("message", "Баннер обновлён");
        return "redirect:/admin/banners";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteBanner(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        Banner banner = bannerRepository.findById(id).orElse(null);

        if (banner != null) {
            if (banner.getImage() != null) {
                String path = banner.getImage().getImageUrl().replace("/uploads/", "uploads/");
                Files.deleteIfExists(Paths.get(path));
            }
            bannerRepository.delete(banner);
        } else {
            redirectAttributes.addFlashAttribute("error", "Баннер не найден");
        }

        return "redirect:/admin/banners";
    }
}
