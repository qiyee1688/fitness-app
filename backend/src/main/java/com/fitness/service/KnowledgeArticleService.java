package com.fitness.service;

import com.fitness.domain.ArticleReference;
import com.fitness.domain.Exercise;
import com.fitness.domain.KnowledgeArticle;
import com.fitness.dto.KnowledgeArticleResponse;
import com.fitness.dto.KnowledgeArticleSummary;
import com.fitness.dto.FoodItemResponse;
import com.fitness.dto.PageResponse;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.KnowledgeArticleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeArticleService {
    private final KnowledgeArticleMapper articleMapper;

    public KnowledgeArticleService(KnowledgeArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    public PageResponse<KnowledgeArticleResponse> listPublished(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<KnowledgeArticleResponse> articles = articleMapper.findPublished(offset, pageSize).stream()
                .map(this::toResponseWithReferences)
                .toList();
        return new PageResponse<>(articles, page, pageSize, articleMapper.countPublished());
    }

    public KnowledgeArticleResponse getPublishedBySlug(String slug) {
        KnowledgeArticle article = articleMapper.findPublishedBySlug(slug);
        if (article == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_ARTICLE_NOT_FOUND);
        }
        return toResponseWithReferences(article);
    }

    public List<KnowledgeArticleSummary> listPublishedByExerciseId(String exerciseId) {
        return articleMapper.findPublishedByExerciseId(exerciseId);
    }

    private KnowledgeArticleResponse toResponseWithReferences(KnowledgeArticle article) {
        List<ArticleReference> references = articleMapper.findPublishedReferences(article.getId());
        List<FoodItemResponse> foodItems = articleMapper.findPublishedFoodItems(article.getId()).stream()
                .map(FoodItemResponse::from)
                .toList();
        return new KnowledgeArticleResponse(
                article.getId(),
                article.getSlug(),
                article.getTitle(),
                article.getTitleEn(),
                article.getSummary(),
                article.getSummaryEn(),
                article.getBody(),
                article.getBodyEn(),
                article.getCoverImageUrl(),
                article.getStatus(),
                article.getPublishedAt(),
                article.getEditor() == null ? null : article.getEditor().getDisplayName(),
                article.getEditor() == null ? null : article.getEditor().getDisplayNameEn(),
                references.stream().map(this::toReferenceResponse).toList(),
                foodItems);
    }

    private KnowledgeArticleResponse.ExerciseReferenceResponse toReferenceResponse(ArticleReference reference) {
        Exercise exercise = reference.getExercise();
        PlanDetailResponse.ExerciseSummary summary = exercise == null ? null : new PlanDetailResponse.ExerciseSummary(
                exercise.getId(),
                exercise.getName(),
                exercise.getBodyPart(),
                exercise.getTarget(),
                exercise.getEquipment(),
                exercise.getGifUrl(),
                exercise.getImageUrl(),
                exercise.getCoachCue(),
                exercise.getCoachCueEn());
        return new KnowledgeArticleResponse.ExerciseReferenceResponse(
                reference.getExerciseId(), reference.getDisplayOrder(), summary);
    }
}
