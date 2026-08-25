package com.fitness.mapper;

import java.math.BigDecimal;

public record FoodItemConstraintTestRow(
        String id,
        BigDecimal servingGrams,
        BigDecimal proteinGrams,
        BigDecimal carbsGrams,
        BigDecimal fatGrams,
        BigDecimal kcal
) {
}
