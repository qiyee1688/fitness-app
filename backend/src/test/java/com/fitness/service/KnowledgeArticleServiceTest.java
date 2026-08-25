package com.fitness.service;

import com.fitness.domain.ArticleReference;
import com.fitness.domain.Editor;
import com.fitness.domain.Exercise;
import com.fitness.domain.FoodCategory;
import com.fitness.domain.FoodItem;
import com.fitness.domain.KnowledgeArticle;
import com.fitness.domain.KnowledgeArticleStatus;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.KnowledgeArticleMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeArticleServiceTest {
    private final KnowledgeArticleMapper articleMapper = mock(KnowledgeArticleMapper.class);
    private final KnowledgeArticleService service = new KnowledgeArticleService(articleMapper);

    @Test
    void returnsPublishedArticlesWithReferencesInMapperOrder() {
        KnowledgeArticle article = article("article-id", "training-basics");
        ArticleReference reference = new ArticleReference();
        reference.setArticleId("article-id");
        reference.setExerciseId("push-up");
        reference.setDisplayOrder(1);
        reference.setExercise(exercise());
        when(articleMapper.findPublished(0, 20)).thenReturn(List.of(article));
        when(articleMapper.countPublished()).thenReturn(1);
        when(articleMapper.findPublishedReferences("article-id")).thenReturn(List.of(reference));
        when(articleMapper.findPublishedFoodItems("article-id")).thenReturn(List.of(foodItem()));

        var response = service.listPublished(1, 20);

        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getItems().getFirst().references()).extracting("exerciseId")
                .containsExactly("push-up");
        assertThat(response.getItems().getFirst().foodItems()).extracting("id")
                .containsExactly("egg-whole");
    }

    @Test
    void rejectsUnknownOrUnpublishedSlugAsNotFound() {
        when(articleMapper.findPublishedBySlug("draft-article")).thenReturn(null);

        assertThatThrownBy(() -> service.getPublishedBySlug("draft-article"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.KNOWLEDGE_ARTICLE_NOT_FOUND));
    }

    private KnowledgeArticle article(String id, String slug) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setId(id);
        article.setSlug(slug);
        article.setTitle("训练基础");
        article.setTitleEn("Training Basics");
        article.setSummary("摘要");
        article.setSummaryEn("Summary");
        article.setBody("正文");
        article.setBodyEn("Body");
        article.setStatus(KnowledgeArticleStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        Editor editor = new Editor();
        editor.setDisplayName("编辑");
        editor.setDisplayNameEn("Editor");
        article.setEditor(editor);
        return article;
    }

    private Exercise exercise() {
        Exercise exercise = new Exercise();
        exercise.setId("push-up");
        exercise.setName("Push-Up");
        exercise.setBodyPart("chest");
        exercise.setTarget("pectorals");
        exercise.setEquipment("body weight");
        return exercise;
    }

    private FoodItem foodItem() {
        FoodItem item = new FoodItem();
        item.setId("egg-whole");
        item.setName("鸡蛋");
        item.setNameEn("Whole egg");
        item.setCategory(FoodCategory.PROTEIN);
        item.setServingDescription("1 个大鸡蛋");
        item.setServingDescriptionEn("1 large egg");
        item.setServingGrams(new java.math.BigDecimal("50"));
        item.setProteinGrams(new java.math.BigDecimal("6.3"));
        item.setCarbsGrams(new java.math.BigDecimal("0.4"));
        item.setFatGrams(new java.math.BigDecimal("5.0"));
        item.setKcal(new java.math.BigDecimal("72"));
        return item;
    }
}
