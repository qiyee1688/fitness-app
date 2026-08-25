package com.fitness.dto;

import com.fitness.domain.FoodCategory;

import java.math.BigDecimal;

public record FoodItemResponse(
        String id,
        String name,
        String nameEn,
        FoodCategory category,
        String servingDescription,
        String servingDescriptionEn,
        BigDecimal servingGrams,
        BigDecimal proteinGrams,
        BigDecimal carbsGrams,
        BigDecimal fatGrams,
        BigDecimal kcal
) {
}
