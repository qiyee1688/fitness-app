package com.fitness.controller;

import com.fitness.domain.Exercise;
import com.fitness.dto.KnowledgeArticleSummary;
import com.fitness.service.ExerciseService;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ExerciseController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Mapper.class)
)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseService exerciseService;

    @Test
    void getRelatedArticlesReturnsOnlyPublishedArticleSummaries() throws Exception {
        when(exerciseService.getExerciseById("0001")).thenReturn(createExercise("0001", "3/4 sit-up"));
        when(exerciseService.getPublishedArticles("0001")).thenReturn(List.of(new KnowledgeArticleSummary(
                "article-id", "training-basics", "训练基础", "Training Basics", "摘要", "Summary",
                null, null)));

        mockMvc.perform(get("/exercises/0001/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].slug").value("training-basics"))
                .andExpect(jsonPath("$.data[0].title").value("训练基础"));
    }

    @Test
    void getExerciseByIdReturnsUnifiedSuccessResponse() throws Exception {
        Exercise exercise = createExercise("0001", "3/4 sit-up");
        when(exerciseService.getExerciseById("0001")).thenReturn(exercise);

        mockMvc.perform(get("/exercises/0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value("0001"))
                .andExpect(jsonPath("$.data.name").value("3/4 sit-up"))
                .andExpect(jsonPath("$.data.instructionSteps.zh[0]").value("平躺，膝盖弯曲，双脚平放在地上。"));
    }

    @Test
    void getExerciseByIdReturnsUnifiedNotFoundResponse() throws Exception {
        when(exerciseService.getExerciseById("missing")).thenReturn(null);

        mockMvc.perform(get("/exercises/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.message").value("Exercise not found"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getExercisesReturnsPagedUnifiedResponse() throws Exception {
        when(exerciseService.getAllExercises(1, 2))
                .thenReturn(List.of(createExercise("0001", "3/4 sit-up"), createExercise("0002", "45 degree side bend")));
        when(exerciseService.getTotalCount()).thenReturn(1324);

        mockMvc.perform(get("/exercises").param("page", "1").param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.total").value(1324))
                .andExpect(jsonPath("$.data.totalPages").value(662))
                .andExpect(jsonPath("$.data.items[0].id").value("0001"));
    }

    @Test
    void getExercisesUsesFilterPathWhenAnyFilterIsPresent() throws Exception {
        when(exerciseService.getExercisesByConditions("waist", null, null, null, 1, 20))
                .thenReturn(List.of(createExercise("0001", "3/4 sit-up")));
        when(exerciseService.getCountByConditions("waist", null, null, null)).thenReturn(1);

        mockMvc.perform(get("/exercises").param("category", "waist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].category").value("waist"));

        verify(exerciseService).getExercisesByConditions("waist", null, null, null, 1, 20);
    }

    @Test
    void searchExercisesReturnsUnifiedResponse() throws Exception {
        when(exerciseService.searchExercises("sit", 10)).thenReturn(List.of(createExercise("0001", "3/4 sit-up")));

        mockMvc.perform(get("/exercises/search").param("keyword", "sit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("3/4 sit-up"));

        verify(exerciseService).searchExercises("sit", 10);
    }

    @Test
    void getExercisesRejectsInvalidPageSize() throws Exception {
        mockMvc.perform(get("/exercises").param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private Exercise createExercise(String id, String name) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setName(name);
        exercise.setCategory("waist");
        exercise.setBodyPart("waist");
        exercise.setEquipment("body weight");
        exercise.setTarget("abs");
        exercise.setMuscleGroup("hip flexors");
        exercise.setSecondaryMuscles(List.of("hip flexors", "lower back"));
        exercise.setInstructionSteps(Map.of(
                "zh", List.of("平躺，膝盖弯曲，双脚平放在地上。"),
                "en", List.of("Lie flat on your back with your knees bent and feet flat on the ground.")
        ));
        exercise.setGifUrl("https://example.com/videos/" + id + ".gif");
        exercise.setImageUrl("https://example.com/images/" + id + ".jpg");
        return exercise;
    }
}
