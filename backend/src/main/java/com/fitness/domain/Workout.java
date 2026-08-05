package com.fitness.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Workout {
    private String id;
    private String planId;
    private int dayNumber;
    private TrainingDayFocus focus;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Prescription> prescriptions;
}
