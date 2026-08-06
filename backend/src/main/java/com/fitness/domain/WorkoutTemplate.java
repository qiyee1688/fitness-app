package com.fitness.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkoutTemplate {
    private String id;
    private String ownerUserId;
    private String sourceWorkoutId;
    private String name;
    private OnDemandBodyPart bodyPart;
    private List<String> equipmentSnapshot;
    private WorkoutTemplateStatus status;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WorkoutTemplateExercise> exercises;
}
