package com.example.vdkfamilyserver.Models.Forum;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "forum_post_attachment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumAttachment {

    public enum Type {
        IMAGE, FILE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ForumPost post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private String url; // Путь к файлу или ссылка

    @Column
    private String preview; // Превью для ссылок, если надо
}

