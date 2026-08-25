package com.fitness.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FoodItem {
    private String id;
    private String name;
    private String nameEn;
    private FoodCategory category;
    private String servingDescription;
    private String servingDescriptionEn;
    private BigDecimal servingGrams;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
    private BigDecimal kcal;
}
