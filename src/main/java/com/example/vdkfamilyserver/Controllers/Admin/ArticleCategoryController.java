package com.example.vdkfamilyserver.Controllers.Admin;

import com.example.vdkfamilyserver.Models.Article.ArticleCategory;
import com.example.vdkfamilyserver.Repositories.Article.ArticleCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/article-categories")
@RequiredArgsConstructor
public class ArticleCategoryController {

    private final ArticleCategoryRepository categoryRepo;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/articleboard/article_categories";
    }

    @PostMapping
    public String createCategory(@RequestParam String code,
                                 @RequestParam String name,
                                 RedirectAttributes redirectAttributes) {
        ArticleCategory category = new ArticleCategory();
        category.setCode(code);
        category.setName(name);
        categoryRepo.save(category);
        redirectAttributes.addFlashAttribute("message", "Категория добавлена");
        return "redirect:/admin/article-categories";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String code,
                                 @RequestParam String name,
                                 RedirectAttributes redirectAttributes) {
        ArticleCategory category = categoryRepo.findById(id).orElse(null);
        if (category != null) {
            category.setCode(code);
            category.setName(name);
            categoryRepo.save(category);
            redirectAttributes.addFlashAttribute("message", "Категория обновлена");
        } else {
            redirectAttributes.addFlashAttribute("error", "Категория не найдена");
        }
        return "redirect:/admin/article-categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        if (categoryRepo.existsById(id)) {
            categoryRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Категория удалена");
        } else {
            redirectAttributes.addFlashAttribute("error", "Категория не найдена");
        }
        return "redirect:/admin/article-categories";
    }
}
