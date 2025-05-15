package com.example.vdkfamilyserver.Controllers.Admin;

import com.example.vdkfamilyserver.Models.Article.Article;
import com.example.vdkfamilyserver.Models.Article.ArticleBlock;
import com.example.vdkfamilyserver.Models.Article.ArticleCategory;
import com.example.vdkfamilyserver.Repositories.Article.ArticleBlockRepository;
import com.example.vdkfamilyserver.Repositories.Article.ArticleCategoryRepository;
import com.example.vdkfamilyserver.Repositories.Article.ArticleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleRepository articleRepo;
    private final ArticleBlockRepository blockRepo;
    private final ArticleCategoryRepository categoryRepo;
    private final Path imageRoot = Paths.get("uploads/ARTICLES");

    @GetMapping
    public String listArticles(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Article> articles = articleRepo.findAll(PageRequest.of(page, 10, Sort.by("createdAt").descending()));
        model.addAttribute("articles", articles.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articles.getTotalPages());
        return "admin/articleboard/articles";
    }

    @GetMapping("/new")
    public String newArticle(Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/articleboard/article_add";
    }

    @PostMapping("/save")
    @Transactional
    public String saveArticle(@RequestParam String title,
                              @RequestParam List<Long> categoryIds,
                              @RequestParam Map<String, String> params,
                              @RequestParam Map<String, MultipartFile> files,
                              Model model) throws IOException {

        Article article = new Article();
        article.setTitle(title);
        article.setCategories(categoryRepo.findAllById(categoryIds));

        article = articleRepo.save(article);

        Files.createDirectories(imageRoot);

        List<ArticleBlock> articleBlocks = new ArrayList<>();
        int i = 0;
        while (true) {
            String typeKey = "block_type_" + i;
            String contentKey = "block_content_" + i;
            String positionKey = "block_position_" + i;

            if (!params.containsKey(typeKey) || !params.containsKey(positionKey)) break;

            String typeStr = params.get(typeKey);
            String content = params.get(contentKey);
            MultipartFile file = files.get(contentKey);

            ArticleBlock block = new ArticleBlock();
            block.setArticle(article);
            block.setType(ArticleBlock.BlockType.valueOf(typeStr));
            block.setPosition(Integer.parseInt(params.get(positionKey)));

            if (block.getType() == ArticleBlock.BlockType.IMAGE) {
                String ext = Objects.requireNonNull(file.getOriginalFilename())
                        .substring(file.getOriginalFilename().lastIndexOf('.'));
                String filename = UUID.randomUUID() + ext;
                Path dest = imageRoot.resolve(filename);
                Files.copy(file.getInputStream(), dest);
                block.setContent("/uploads/ARTICLES/" + filename);
            } else {
                block.setContent(content);
            }

            articleBlocks.add(block);
            i++;
        }

        blockRepo.saveAll(articleBlocks);
        return "redirect:/admin/articles";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        Article article = articleRepo.findById(id).orElse(null);
        if (article != null) {
            for (ArticleBlock block : article.getBlocks()) {
                if (block.getType() == ArticleBlock.BlockType.IMAGE) {
                    Path p = Paths.get(block.getContent().replace("/uploads/", "uploads/"));
                    Files.deleteIfExists(p);
                }
            }
            articleRepo.delete(article);
            redirectAttributes.addFlashAttribute("message", "Статья удалена");
        } else {
            redirectAttributes.addFlashAttribute("error", "Статья не найдена");
        }
        return "redirect:/admin/articles";
    }

    @GetMapping("/edit/{id}")
    public String editArticle(@PathVariable Long id, Model model) throws JsonProcessingException {
        Article article = articleRepo.findById(id).orElseThrow();
        List<ArticleBlock> blocks = article.getBlocks();

        // Сериализация блоков в JSON для фронта
        ObjectMapper mapper = new ObjectMapper();
        String blocksJson = mapper.writeValueAsString(
                blocks.stream().map(b -> Map.of(
                        "type", b.getType().name(),
                        "content", b.getContent()
                )).toList()
        );

        model.addAttribute("article", article);
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("blocksJson", blocksJson);
        Set<Long> categoryIds = article.getCategories().stream()
                .map(c -> c.getId())
                .collect(Collectors.toSet());

        model.addAttribute("categoryIds", categoryIds);
        return "admin/articleboard/article_edit";
    }

    @PostMapping("/update/{id}")
    @Transactional
    public String updateArticle(@PathVariable Long id,
                                @RequestParam String title,
                                @RequestParam List<Long> categoryIds,
                                @RequestParam Map<String, String> params,
                                @RequestParam Map<String, MultipartFile> files,
                                RedirectAttributes redirectAttributes) throws IOException {

        Article article = articleRepo.findById(id).orElseThrow();
        article.setTitle(title);
        List<ArticleCategory> categories = categoryRepo.findAllById(categoryIds);
        article.setCategories(categories);

        Files.createDirectories(imageRoot);
        List<ArticleBlock> newBlocks = new ArrayList<>();
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

            ArticleBlock.BlockType type = ArticleBlock.BlockType.valueOf(typeStr);
            String finalContent;

            if (type == ArticleBlock.BlockType.IMAGE) {
                if (file != null && !file.isEmpty()) {
                    String ext = Objects.requireNonNull(file.getOriginalFilename())
                            .substring(file.getOriginalFilename().lastIndexOf('.'));
                    String filename = UUID.randomUUID() + ext;
                    Path dest = imageRoot.resolve(filename);
                    Files.copy(file.getInputStream(), dest);
                    finalContent = "/uploads/ARTICLES/" + filename;
                } else {
                    finalContent = existing;
                    preservedImages.add(existing.replace("/uploads/", "uploads/"));
                }
            } else {
                finalContent = content;
            }

            ArticleBlock block = new ArticleBlock();
            block.setArticle(article);
            block.setType(type);
            block.setPosition(Integer.parseInt(params.get(positionKey)));
            block.setContent(finalContent);

            newBlocks.add(block);
            i++;
        }

        for (ArticleBlock block : article.getBlocks()) {
            if (block.getType() == ArticleBlock.BlockType.IMAGE) {
                String relPath = block.getContent().replace("/uploads/", "uploads/");
                if (!preservedImages.contains(relPath)) {
                    Files.deleteIfExists(Paths.get(relPath));
                }
            }
        }

        blockRepo.deleteAll(article.getBlocks());
        article.getBlocks().clear();
        blockRepo.saveAll(newBlocks);

        redirectAttributes.addFlashAttribute("message", "Статья обновлена");
        return "redirect:/admin/articles";
    }
}