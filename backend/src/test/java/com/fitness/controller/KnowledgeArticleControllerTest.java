package com.fitness.controller;

import com.fitness.domain.KnowledgeArticleStatus;
import com.fitness.domain.FoodCategory;
import com.fitness.dto.FoodItemResponse;
import com.fitness.dto.KnowledgeArticleResponse;
import com.fitness.dto.PageResponse;
import com.fitness.service.KnowledgeArticleService;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = KnowledgeArticleController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Mapper.class)
)
class KnowledgeArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeArticleService articleService;

    @Test
    void listReturnsPagedUnifiedSuccessResponse() throws Exception {
        KnowledgeArticleResponse article = article("article-id", "training-basics");
        when(articleService.listPublished(2, 10))
                .thenReturn(new PageResponse<>(List.of(article), 2, 10, 11));

        mockMvc.perform(get("/knowledge-articles").param("page", "2").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.total").value(11))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.items[0].slug").value("training-basics"));

        verify(articleService).listPublished(2, 10);
    }

    @Test
    void getReturnsArticleBySlugInUnifiedSuccessResponse() throws Exception {
        when(articleService.getPublishedBySlug("training-basics"))
                .thenReturn(article("article-id", "training-basics"));

        mockMvc.perform(get("/knowledge-articles/training-basics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.articleId").value("article-id"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.references[0].exerciseId").value("push-up"))
                .andExpect(jsonPath("$.data.references[0].exercise.name").value("Push-Up"))
                .andExpect(jsonPath("$.data.foodItems[0].id").value("egg-whole"))
                .andExpect(jsonPath("$.data.foodItems[0].nameEn").value("Whole egg"));

        verify(articleService).getPublishedBySlug("training-basics");
    }

    @Test
    void listByExerciseReturnsPublishedArticleSummaries() throws Exception {
        when(articleService.listPublishedByExerciseId("push-up")).thenReturn(List.of(
                new com.fitness.dto.KnowledgeArticleSummary(
                        "article-id", "training-basics", "训练基础", "Training Basics",
                        "摘要", "Summary", null, LocalDateTime.of(2026, 8, 19, 10, 0))));

        mockMvc.perform(get("/knowledge-articles/by-exercise/push-up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].articleId").value("article-id"))
                .andExpect(jsonPath("$.data[0].titleEn").value("Training Basics"));

        verify(articleService).listPublishedByExerciseId("push-up");
    }

    @Test
    void listRejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/knowledge-articles").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/knowledge-articles").param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private KnowledgeArticleResponse article(String articleId, String slug) {
        return new KnowledgeArticleResponse(
                articleId,
                slug,
                "训练基础",
                "Training Basics",
                "从动作开始建立训练习惯。",
                "Build a training habit from movement basics.",
                "正文",
                "Body",
                "https://example.com/articles/training-basics.jpg",
                KnowledgeArticleStatus.PUBLISHED,
                LocalDateTime.of(2026, 8, 19, 10, 0),
                "编辑",
                "Editor",
                List.of(new KnowledgeArticleResponse.ExerciseReferenceResponse(
                        "push-up",
                        1,
                        new com.fitness.dto.PlanDetailResponse.ExerciseSummary(
                                "push-up",
                                "Push-Up",
                                "chest",
                                "pectorals",
                                "body weight",
                                null,
                                null,
                                null,
                                null))),
                List.of(new FoodItemResponse(
                        "egg-whole", "鸡蛋", "Whole egg", FoodCategory.PROTEIN,
                        "1 个大鸡蛋", "1 large egg", new java.math.BigDecimal("50"),
                        new java.math.BigDecimal("6.3"), new java.math.BigDecimal("0.4"),
                        new java.math.BigDecimal("5.0"), new java.math.BigDecimal("72"))));
    }
}
