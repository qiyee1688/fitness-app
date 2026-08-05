package com.fitness.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Prescription {
    private String id;
    private String workoutId;
    private String exerciseId;
    private int sequence;
    private int sets;
    private int reps;
    private BigDecimal load;
    private LoadType loadType;
    private BigDecimal rpe;
    private LocalDateTime createdAt;
    private Exercise exercise;
}
