package com.example.vdkfamilyserver.Models.Event;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_blocks")
@Getter
@Setter
@NoArgsConstructor
public class EventBlock {

    public enum BlockType {
        TEXT, IMAGE, TITLE, LINK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer position;
}
