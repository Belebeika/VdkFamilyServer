package com.example.vdkfamilyserver.DTO.Data.Forum;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumAttachmentDTO {
    private Long id;
    private String type;
    private String url;
    private String preview;
}
