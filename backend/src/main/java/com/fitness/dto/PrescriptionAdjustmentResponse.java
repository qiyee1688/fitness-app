package com.fitness.dto;

import com.fitness.domain.PrescriptionAdjustmentStatus;
import java.time.LocalDateTime;
import java.util.Map;

public record PrescriptionAdjustmentResponse(
        String adjustmentId, String planId, String sourceWorkoutId, String sourceExerciseId,
        String firstFeedbackId, String secondFeedbackId, String targetWorkoutId, String targetPrescriptionId,
        Map<String, Object> originalPrescription, Map<String, Object> suggestedPrescription,
        String suggestedExerciseId, String reason, String reasonEn, PrescriptionAdjustmentStatus status,
        LocalDateTime createdAt, LocalDateTime processedAt
) {}
