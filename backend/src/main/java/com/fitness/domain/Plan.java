package com.fitness.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class Plan {
    private String id;
    private String userId;
    private Map<String, Object> profileSnapshot;
    private PlanStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String parentPlanId;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Workout> workouts;
}
