package com.fitness.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NutritionRule {
    private String id;
    private String businessKey;
    private Goal goal;
    private TrainingDayFocus focus;
    private NutritionTiming timing;
    private Map<String, Object> formula;
    private String note;
    private String noteEn;
    private int version;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
