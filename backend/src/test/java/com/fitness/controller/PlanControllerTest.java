package com.fitness.controller;

import com.fitness.domain.PlanStatus;
import com.fitness.dto.GeneratePlanRequest;
import com.fitness.dto.ExerciseFeedbackResponse;
import com.fitness.domain.FeedbackType;
import com.fitness.dto.GeneratedPlanResponse;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.PlanLifecycleResponse;
import com.fitness.dto.ReplaceWorkoutWithTemplateResponse;
import com.fitness.dto.TodayWorkoutResponse;
import com.fitness.service.PlanService;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PlanController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Mapper.class)
)
class PlanControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PlanService planService;

    @Test
    void generateReturnsUnifiedResponse() throws Exception {
        GeneratePlanRequest request = new GeneratePlanRequest("demo", LocalDate.of(2026, 8, 10));
        when(planService.generatePlan(request)).thenReturn(new GeneratedPlanResponse(
                "plan-id", PlanStatus.ACTIVE, request.startDate(),
                LocalDate.of(2026, 10, 4), 24, List.of()));

        mockMvc.perform(post("/plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"startDate\":\"2026-08-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.workoutCount").value(24));
    }

    @Test
    void currentReturnsPlanDetail() throws Exception {
        PlanDetailResponse response = new PlanDetailResponse(
                "plan-id", PlanStatus.ACTIVE, LocalDate.of(2026, 8, 12),
                LocalDate.of(2026, 10, 6), 8, Map.of("daysPerWeek", 4), List.of());
        when(planService.getActivePlan("demo")).thenReturn(response);

        mockMvc.perform(get("/plans/current").param("username", "demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.planId").value("plan-id"))
                .andExpect(jsonPath("$.data.totalWeeks").value(8));
    }

    @Test
    void lifecycleProcessReturnsUnifiedTransitionResponse() throws Exception {
        when(planService.processLifecycle("demo", LocalDate.of(2026, 8, 15)))
                .thenReturn(new PlanLifecycleResponse(
                        "plan-id", PlanStatus.ACTIVE, PlanStatus.PAUSED, null, true));

        mockMvc.perform(post("/plans/lifecycle/process")
                        .param("username", "demo")
                        .param("date", "2026-08-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.previousStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.currentStatus").value("PAUSED"))
                .andExpect(jsonPath("$.data.changed").value(true));
    }


    @Test
    void todayReturnsScheduledWorkout() throws Exception {
        TodayWorkoutResponse response = new TodayWorkoutResponse(
                "plan-id", "workout-id", 1, LocalDate.of(2026, 8, 12),
                com.fitness.domain.TrainingDayFocus.FULL_BODY, null, false, List.of());
        when(planService.getTodayWorkout("demo", LocalDate.of(2026, 8, 12))).thenReturn(response);

        mockMvc.perform(get("/plans/today")
                        .param("username", "demo")
                        .param("date", "2026-08-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.workoutId").value("workout-id"))
                .andExpect(jsonPath("$.data.focus").value("FULL_BODY"));
    }

    @Test
    void completeReturnsUpdatedWorkout() throws Exception {
        TodayWorkoutResponse response = new TodayWorkoutResponse(
                "plan-id", "workout-id", 1, LocalDate.of(2026, 8, 12),
                com.fitness.domain.TrainingDayFocus.FULL_BODY,
                LocalDateTime.of(2026, 8, 12, 9, 30), false, List.of());
        when(planService.completeWorkout("demo", "workout-id")).thenReturn(response);

        mockMvc.perform(post("/plans/workouts/workout-id/complete")
                        .param("username", "demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.completedAt").exists());
    }

    @Test
    void feedbackReturnsUnifiedEffectResponse() throws Exception {
        TodayWorkoutResponse workout = new TodayWorkoutResponse(
                "plan-id", "workout-id", 1, LocalDate.of(2026, 8, 12),
                com.fitness.domain.TrainingDayFocus.FULL_BODY, null, false, List.of());
        when(planService.submitExerciseFeedback(
                org.mockito.ArgumentMatchers.eq("demo"),
                org.mockito.ArgumentMatchers.eq("workout-id"),
                org.mockito.ArgumentMatchers.eq("exercise-id"),
                org.mockito.ArgumentMatchers.any())).thenReturn(new ExerciseFeedbackResponse(
                        "feedback-id", FeedbackType.HURT, "waist",
                        LocalDate.of(2026, 9, 2), true, false, "replacement-id", workout));

        mockMvc.perform(post("/plans/workouts/workout-id/exercises/exercise-id/feedback")
                        .param("username", "demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feedbackType\":\"HURT\",\"hurtBodyPart\":\"waist\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.substituted").value(true))
                .andExpect(jsonPath("$.data.filterUntil").value("2026-09-02"));
    }

    @Test
    void replaceWorkoutReturnsUnifiedReplacementResponse() throws Exception {
        PlanDetailResponse.WorkoutDetail workout = new PlanDetailResponse.WorkoutDetail(
                "replacement-id", 8, 2, LocalDate.of(2026, 8, 19),
                com.fitness.domain.TrainingDayFocus.FULL_BODY, List.of());
        when(planService.replaceWorkoutWithTemplate(
                org.mockito.ArgumentMatchers.eq("demo"),
                org.mockito.ArgumentMatchers.eq("plan-id"),
                org.mockito.ArgumentMatchers.eq("workout-id"),
                org.mockito.ArgumentMatchers.any())).thenReturn(new ReplaceWorkoutWithTemplateResponse(
                        "plan-id", "workout-id", "replacement-id", 8, workout));

        mockMvc.perform(post("/plans/plan-id/workouts/workout-id/replace")
                        .param("username", "demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":\"template-id\",\"expectedPlanVersion\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.originalWorkoutId").value("workout-id"))
                .andExpect(jsonPath("$.data.replacementWorkoutId").value("replacement-id"));
    }

    @Test
    void generateRejectsBlankUsername() throws Exception {
        mockMvc.perform(post("/plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
