package com.fitness.dto;

import com.fitness.domain.PlanStatus;
import com.fitness.domain.TrainingDayFocus;

import java.time.LocalDate;
import java.util.List;

public record GeneratedPlanResponse(
        String planId,
        PlanStatus status,
        LocalDate startDate,
        LocalDate endDate,
        int workoutCount,
        List<WorkoutSummary> workouts
) {
    public record WorkoutSummary(
            String workoutId,
            int dayNumber,
            TrainingDayFocus focus,
            int prescriptionCount
    ) {
    }
}
