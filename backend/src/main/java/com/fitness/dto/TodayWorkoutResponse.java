package com.fitness.dto;

import com.fitness.domain.TrainingDayFocus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TodayWorkoutResponse(
        String planId,
        String workoutId,
        int dayNumber,
        LocalDate scheduledDate,
        TrainingDayFocus focus,
        LocalDateTime completedAt,
        boolean alreadyCompleted,
        List<PlanDetailResponse.PrescriptionDetail> prescriptions,
        List<NutritionTipResponse> nutritionTips
) {
    public TodayWorkoutResponse(
            String planId,
            String workoutId,
            int dayNumber,
            LocalDate scheduledDate,
            TrainingDayFocus focus,
            LocalDateTime completedAt,
            boolean alreadyCompleted,
            List<PlanDetailResponse.PrescriptionDetail> prescriptions
    ) {
        this(planId, workoutId, dayNumber, scheduledDate, focus, completedAt,
                alreadyCompleted, prescriptions, List.of());
    }
}
