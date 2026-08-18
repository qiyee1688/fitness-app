package com.fitness.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NutritionTip {
    private String id;
    private String workoutId;
    private NutritionTiming timing;
    private Map<String, Object> macroTargets;
    private String note;
    private String noteEn;
    private String ruleId;
    private int ruleVersion;
    private BigDecimal weightKgSnapshot;
    private LocalDateTime createdAt;
}
