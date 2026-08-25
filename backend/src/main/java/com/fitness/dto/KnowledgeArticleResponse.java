package com.fitness.dto;

import com.fitness.domain.KnowledgeArticleStatus;

import java.time.LocalDateTime;
import java.util.List;

public record KnowledgeArticleResponse(
        String articleId,
        String slug,
        String title,
        String titleEn,
        String summary,
        String summaryEn,
        String body,
        String bodyEn,
        String coverImageUrl,
        KnowledgeArticleStatus status,
        LocalDateTime publishedAt,
        String editorName,
        String editorNameEn,
        List<ExerciseReferenceResponse> references,
        List<FoodItemResponse> foodItems
) {
    public record ExerciseReferenceResponse(
            String exerciseId,
            int displayOrder,
            PlanDetailResponse.ExerciseSummary exercise
    ) {
    }
}
