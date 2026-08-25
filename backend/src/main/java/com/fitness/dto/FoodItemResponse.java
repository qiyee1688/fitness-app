package com.fitness.dto;

import com.fitness.domain.FoodCategory;
import com.fitness.domain.FoodItem;

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
    public static FoodItemResponse from(FoodItem item) {
        return new FoodItemResponse(
                item.getId(), item.getName(), item.getNameEn(), item.getCategory(),
                item.getServingDescription(), item.getServingDescriptionEn(), item.getServingGrams(),
                item.getProteinGrams(), item.getCarbsGrams(), item.getFatGrams(), item.getKcal());
    }
}
