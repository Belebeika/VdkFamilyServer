package com.example.vdkfamilyserver.Repositories.Article;

import com.example.vdkfamilyserver.Models.Article.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleCategoryRepository extends JpaRepository<ArticleCategory, Long> {
}