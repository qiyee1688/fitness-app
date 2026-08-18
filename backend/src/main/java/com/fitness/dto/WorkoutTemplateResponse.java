package com.fitness.dto;

import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.WorkoutTemplateStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record WorkoutTemplateResponse(
        String templateId,
        String sourceWorkoutId,
        String name,
        OnDemandBodyPart bodyPart,
        List<String> equipment,
        Map<String, Object> profileSnapshot,
        boolean profileChanged,
        WorkoutTemplateStatus status,
        int version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PlanDetailResponse.PrescriptionDetail> exercises
) {
}
