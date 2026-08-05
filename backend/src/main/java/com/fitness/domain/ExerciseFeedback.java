package com.fitness.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExerciseFeedback {
    private String id;
    private String workoutId;
    private String exerciseId;
    private String feedbackType;
    private String hurtBodyPart;
    private LocalDate filterUntil;
    private LocalDateTime createdAt;
}
