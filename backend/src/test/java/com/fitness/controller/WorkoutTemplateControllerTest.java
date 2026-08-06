package com.fitness.controller;

import com.fitness.domain.LoadType;
import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.WorkoutTemplateStatus;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.exception.GlobalExceptionHandler;
import com.fitness.service.WorkoutTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkoutTemplateControllerTest {
    private final WorkoutTemplateService service = mock(WorkoutTemplateService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WorkoutTemplateController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void createReturnsUnifiedTemplateResponse() throws Exception {
        when(service.create(any())).thenReturn(response());

        mockMvc.perform(post("/workout-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceWorkoutId":"workout-id","name":"Chest builder"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.templateId").value("template-id"))
                .andExpect(jsonPath("$.data.exercises[0].exercise.coachCue").value("核心收紧"));
        verify(service).create(any());
    }

    @Test
    void listReturnsUnifiedTemplateResponses() throws Exception {
        when(service.list()).thenReturn(List.of(response()));

        mockMvc.perform(get("/workout-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].templateId").value("template-id"));
    }

    @Test
    void createRejectsBlankSourceWorkoutId() throws Exception {
        mockMvc.perform(post("/workout-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceWorkoutId":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private WorkoutTemplateResponse response() {
        PlanDetailResponse.ExerciseSummary exercise = new PlanDetailResponse.ExerciseSummary(
                "push-up", "Push-Up", "chest", "pectorals", "body weight",
                null, null, "核心收紧", "Brace your core");
        PlanDetailResponse.PrescriptionDetail item = new PlanDetailResponse.PrescriptionDetail(
                "template-exercise-id", 1, 3, 12, null, LoadType.BODYWEIGHT,
                new BigDecimal("7.5"), exercise);
        return new WorkoutTemplateResponse(
                "template-id", "workout-id", "Chest builder", OnDemandBodyPart.CHEST,
                List.of("body weight"), WorkoutTemplateStatus.ACTIVE, 0,
                null, null, List.of(item));
    }
}
