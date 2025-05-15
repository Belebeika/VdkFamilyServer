package com.example.vdkfamilyserver.Models.Article;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "article_categories")
@Getter
@Setter
@NoArgsConstructor
public class ArticleCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // INTERNAL (e.g. "HEALTH", "FAMILY")

    @Column(nullable = false)
    private String name; // отображаемое имя (e.g. "Здоровье", "Семья")

    @ManyToMany(mappedBy = "categories")
    private List<Article> articles = new ArrayList<>();
}
