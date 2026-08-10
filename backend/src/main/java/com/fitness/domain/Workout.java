package com.fitness.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Workout {
    private String id;
    private String ownerUserId;
    private String planId;
    private String replacedWorkoutId;
    private int dayNumber;
    private TrainingDayFocus focus;
    private OnDemandBodyPart requestedBodyPart;
    private List<String> equipmentSnapshot;
    private WorkoutSource source;
    private WorkoutStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Prescription> prescriptions;
}
