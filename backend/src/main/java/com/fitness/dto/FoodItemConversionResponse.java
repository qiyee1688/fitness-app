package com.fitness.dto;

import com.fitness.domain.MacroTarget;

import java.math.BigDecimal;

public record FoodItemConversionResponse(
        FoodItemResponse foodItem,
        BigDecimal servings,
        MacroTarget macroTargets
) {
}
