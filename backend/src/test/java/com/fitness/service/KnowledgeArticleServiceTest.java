package com.fitness.service;

import com.fitness.domain.ArticleReference;
import com.fitness.domain.Editor;
import com.fitness.domain.Exercise;
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

        var response = service.listPublished(1, 20);

        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getItems().getFirst().references()).extracting("exerciseId")
                .containsExactly("push-up");
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
}
