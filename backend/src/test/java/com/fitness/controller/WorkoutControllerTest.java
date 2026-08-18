package com.fitness.controller;

import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.MacroTarget;
import com.fitness.domain.MacroTargetBasis;
import com.fitness.domain.MacroTargetValue;
import com.fitness.domain.NutritionTiming;
import com.fitness.domain.NutritionUnit;
import com.fitness.domain.WorkoutSource;
import com.fitness.domain.WorkoutStatus;
import com.fitness.dto.OnDemandWorkoutResponse;
import com.fitness.dto.NutritionTipResponse;
import com.fitness.exception.GlobalExceptionHandler;
import com.fitness.service.NutritionService;
import com.fitness.service.OnDemandWorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
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

class WorkoutControllerTest {
    private final OnDemandWorkoutService service = mock(OnDemandWorkoutService.class);
    private final NutritionService nutritionService = mock(NutritionService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                    new WorkoutController(service, nutritionService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void generateDoesNotAcceptUserIdAndReturnsDraft() throws Exception {
        when(service.generate(any())).thenReturn(response(WorkoutStatus.DRAFT));

        mockMvc.perform(post("/workouts/on-demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bodyPart\":\"CHEST\",\"equipment\":[\"dumbbell\"],\"userId\":\"other\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void startAndCompleteUseOnlyWorkoutId() throws Exception {
        when(service.start("workout-id")).thenReturn(response(WorkoutStatus.IN_PROGRESS));
        when(service.complete("workout-id")).thenReturn(response(WorkoutStatus.COMPLETED));

        mockMvc.perform(post("/workouts/workout-id/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
        mockMvc.perform(post("/workouts/workout-id/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        verify(service).start("workout-id");
        verify(service).complete("workout-id");
    }

    @Test
    void rejectsMissingBodyPart() throws Exception {
        mockMvc.perform(post("/workouts/on-demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request"));
    }

    @Test
    void rejectsNegativeVariation() throws Exception {
        mockMvc.perform(post("/workouts/on-demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bodyPart\":\"CHEST\",\"variation\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void returnsBilingualCoachCue() throws Exception {
        when(service.generate(any())).thenReturn(responseWithCoachCue());

        mockMvc.perform(post("/workouts/on-demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bodyPart\":\"CHEST\",\"variation\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prescriptions[0].exercise.coachCue").value("保持核心稳定"))
                .andExpect(jsonPath("$.data.prescriptions[0].exercise.coachCueEn").value("Brace your core"));
    }

    @Test
    void returnsOwnedWorkoutNutritionTips() throws Exception {
        MacroTarget targets = new MacroTarget(
                new MacroTargetValue(new BigDecimal("108.0"), NutritionUnit.GRAMS,
                        MacroTargetBasis.PER_KG_BODYWEIGHT),
                null, null, null);
        when(nutritionService.listOwnedWorkoutTips("workout-id")).thenReturn(List.of(
                new NutritionTipResponse(
                        "tip-id", "workout-id", NutritionTiming.PRE_WORKOUT, targets,
                        "训练前补充能量", "Fuel before training", "rule-id", 2,
                        new BigDecimal("60.0"))));

        mockMvc.perform(get("/workouts/workout-id/nutrition-tips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].timing").value("PRE_WORKOUT"))
                .andExpect(jsonPath("$.data[0].macroTargets.protein.value").value(108.0))
                .andExpect(jsonPath("$.data[0].noteEn").value("Fuel before training"));

        verify(nutritionService).listOwnedWorkoutTips("workout-id");
    }

    private OnDemandWorkoutResponse response(WorkoutStatus status) {
        return new OnDemandWorkoutResponse(
                "workout-id", OnDemandBodyPart.CHEST, List.of("body weight"),
                WorkoutSource.ON_DEMAND, status,
                status == WorkoutStatus.IN_PROGRESS ? LocalDateTime.now() : null,
                status == WorkoutStatus.COMPLETED ? LocalDateTime.now() : null,
                LocalDateTime.now().plusHours(24), List.of());
    }

    private OnDemandWorkoutResponse responseWithCoachCue() {
        var exercise = new com.fitness.dto.PlanDetailResponse.ExerciseSummary(
                "exercise-id", "Push up", "chest", "pectorals", "body weight",
                null, null, "保持核心稳定", "Brace your core");
        var prescription = new com.fitness.dto.PlanDetailResponse.PrescriptionDetail(
                "prescription-id", 1, 3, 12, null,
                com.fitness.domain.LoadType.BODYWEIGHT, new java.math.BigDecimal("7.5"), exercise);
        return new OnDemandWorkoutResponse(
                "workout-id", OnDemandBodyPart.CHEST, List.of("body weight"),
                WorkoutSource.ON_DEMAND, WorkoutStatus.DRAFT, null, null,
                LocalDateTime.now().plusHours(24), List.of(prescription));
    }
}
