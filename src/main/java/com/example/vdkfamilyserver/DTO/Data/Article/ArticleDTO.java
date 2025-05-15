package com.example.vdkfamilyserver.DTO.Data.Article;

import java.util.List;

public record ArticleDTO(
        Long id,
        String title,
        List<String> categoryNames,
        List<String> categoryCodes,
        List<BlockDTO> blocks
) {}