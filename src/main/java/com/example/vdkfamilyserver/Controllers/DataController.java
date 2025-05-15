package com.example.vdkfamilyserver.Controllers;

import com.example.vdkfamilyserver.DTO.Data.Article.ArticleCategoryDTO;
import com.example.vdkfamilyserver.DTO.Data.Article.ArticleDTO;
import com.example.vdkfamilyserver.DTO.Data.BannerDTO;
import com.example.vdkfamilyserver.DTO.Data.Article.BlockDTO;
import com.example.vdkfamilyserver.DTO.Data.NewsDTO;
import com.example.vdkfamilyserver.Models.Article.ArticleCategory;
import com.example.vdkfamilyserver.Repositories.Article.ArticleCategoryRepository;
import com.example.vdkfamilyserver.Repositories.Article.ArticleRepository;
import com.example.vdkfamilyserver.Repositories.BannerRepository;
import com.example.vdkfamilyserver.Repositories.NewsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final BannerRepository bannerRepository;
    private final NewsRepository newsRepository;
    private final ArticleRepository articleRepository;
    private final ArticleCategoryRepository articleCategoryRepository;

    @GetMapping("/banner")
    public List<BannerDTO> getAdviceImages(HttpServletRequest request) {
        return bannerRepository.findTop5ByOrderByUploadedAtDesc().stream()
                .map(b -> new BannerDTO(
                        b.getId(),
                        prependServerUrl(request, b.getImage().getImageUrl()),
                        b.getLinkUrl()
                ))
                .toList();
    }

    @GetMapping("/news")
    public List<NewsDTO> getNewsImages(HttpServletRequest request) {
        return newsRepository.findTop4ByOrderByUploadedAtDesc().stream()
                .map(n -> new NewsDTO(
                        n.getId(),
                        prependServerUrl(request, n.getImage().getImageUrl()),
                        n.getLinkUrl(),
                        n.getTitle()
                ))
                .toList();
    }

    @GetMapping("/articles")
    public List<ArticleDTO> getArticles(HttpServletRequest request) {
        return articleRepository.findAll().stream()
                .map(article -> new ArticleDTO(
                        article.getId(),
                        article.getTitle(),
                        article.getCategories().stream().map(ArticleCategory::getName).toList(),
                        article.getCategories().stream().map(ArticleCategory::getCode).toList(),
                        article.getBlocks().stream()
                                .map(b -> new BlockDTO(
                                        b.getType().name(),
                                        b.getType().name().equals("IMAGE")
                                                ? prependServerUrl(request, b.getContent())
                                                : b.getContent()
                                ))
                                .toList()
                ))
                .toList();
    }

    @GetMapping("/article-categories")
    public List<ArticleCategoryDTO> getArticleCategories() {
        return articleCategoryRepository.findAll().stream()
                .map(c -> new ArticleCategoryDTO(
                        c.getId(),
                        c.getCode(),
                        c.getName()
                ))
                .toList();
    }


    private String prependServerUrl(HttpServletRequest request, String relativePath) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return baseUrl + relativePath;
    }
}