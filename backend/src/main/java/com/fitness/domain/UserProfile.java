package com.fitness.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfile {
    private String id;
    private String userId;
    private FitnessLevel fitnessLevel;
    private Goal goal;
    private int daysPerWeek;
    private List<String> availableEquipment;
    private BigDecimal weightKg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
