package com.example.vdkfamilyserver.DTO.Data.ArticleAndEvent;

import java.util.List;

public record ArticleDTO(
        Long id,
        String title,
        List<String> categoryNames,
        List<String> categoryCodes,
        List<BlockDTO> blocks
) {}