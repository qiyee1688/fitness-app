package com.fitness.dto;

import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.WorkoutSource;
import com.fitness.domain.WorkoutStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OnDemandWorkoutResponse(
        String workoutId,
        OnDemandBodyPart bodyPart,
        List<String> equipment,
        WorkoutSource source,
        WorkoutStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime expiresAt,
        List<PlanDetailResponse.PrescriptionDetail> prescriptions,
        List<NutritionTipResponse> nutritionTips
) {
    public OnDemandWorkoutResponse(
            String workoutId,
            OnDemandBodyPart bodyPart,
            List<String> equipment,
            WorkoutSource source,
            WorkoutStatus status,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime expiresAt,
            List<PlanDetailResponse.PrescriptionDetail> prescriptions
    ) {
        this(workoutId, bodyPart, equipment, source, status, startedAt, completedAt,
                expiresAt, prescriptions, List.of());
    }
}
