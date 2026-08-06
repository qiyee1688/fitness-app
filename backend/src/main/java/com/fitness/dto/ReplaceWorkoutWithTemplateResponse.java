package com.fitness.dto;

public record ReplaceWorkoutWithTemplateResponse(
        String planId,
        String originalWorkoutId,
        String replacementWorkoutId,
        int dayNumber,
        PlanDetailResponse.WorkoutDetail workout
) {
}
