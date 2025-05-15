package com.example.vdkfamilyserver.Repositories.Article;

import com.example.vdkfamilyserver.Models.Article.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}