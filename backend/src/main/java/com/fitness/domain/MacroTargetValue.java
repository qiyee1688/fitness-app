package com.fitness.domain;

import java.math.BigDecimal;

public record MacroTargetValue(
        BigDecimal value,
        NutritionUnit unit,
        MacroTargetBasis basis
) {
}
