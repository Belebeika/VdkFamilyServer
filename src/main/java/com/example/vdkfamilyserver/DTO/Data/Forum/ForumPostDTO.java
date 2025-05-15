package com.example.vdkfamilyserver.DTO.Data.Forum;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ForumPostDTO {
    private Long id;
    private String author;
    private String content;
    private String createdAt;
    private String editedAt;
    private Long parentId;
    private List<ForumAttachmentDTO> attachments = new ArrayList<>();
}

