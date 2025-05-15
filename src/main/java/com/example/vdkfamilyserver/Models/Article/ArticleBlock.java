package com.example.vdkfamilyserver.Models.Article;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_blocks")
@Getter
@Setter
@NoArgsConstructor
public class ArticleBlock {

    public enum BlockType {
        TEXT, IMAGE, TITLE, LINK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer position;
}

