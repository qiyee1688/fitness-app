package com.fitness.dto;

import com.fitness.domain.MacroTarget;
import com.fitness.domain.NutritionTiming;

import java.math.BigDecimal;

public record NutritionTipResponse(
        String tipId,
        String workoutId,
        NutritionTiming timing,
        MacroTarget macroTargets,
        String note,
        String noteEn,
        String ruleId,
        int ruleVersion,
        BigDecimal weightKgSnapshot
) {
}
