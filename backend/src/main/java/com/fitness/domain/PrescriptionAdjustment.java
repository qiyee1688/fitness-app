package com.fitness.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PrescriptionAdjustment {
    private String id;
    private String planId;
    private String sourceWorkoutId;
    private String sourceExerciseId;
    private String firstFeedbackId;
    private String secondFeedbackId;
    private String targetWorkoutId;
    private String targetPrescriptionId;
    private Map<String, Object> originalPrescription;
    private Map<String, Object> suggestedPrescription;
    private String suggestedExerciseId;
    private String reason;
    private String reasonEn;
    private PrescriptionAdjustmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
