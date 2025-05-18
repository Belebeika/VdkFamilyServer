package com.example.vdkfamilyserver.DTO.Data.ArticleAndEvent;

import java.time.LocalDate;
import java.util.List;

public record EventDTO(
        Long id,
        String title,
        LocalDate eventDate,
        List<BlockDTO> blocks
) {}
