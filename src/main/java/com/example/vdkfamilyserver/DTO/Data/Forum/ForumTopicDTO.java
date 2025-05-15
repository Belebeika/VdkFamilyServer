package com.example.vdkfamilyserver.DTO.Data.Forum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumTopicDTO {
    private Long id;
    private String title;
    private String author;
    private String createdAt;
    private int postCount;
}

