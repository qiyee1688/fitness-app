package com.fitness.dto;

import java.time.LocalDateTime;

public record KnowledgeArticleSummary(
        String articleId,
        String slug,
        String title,
        String titleEn,
        String summary,
        String summaryEn,
        String coverImageUrl,
        LocalDateTime publishedAt
) {
}
