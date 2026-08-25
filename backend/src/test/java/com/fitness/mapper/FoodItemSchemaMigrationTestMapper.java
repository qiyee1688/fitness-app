package com.fitness.mapper;

public interface FoodItemSchemaMigrationTestMapper {
    void createUpdatedAtFunction();

    int countFoodItems();

    void insertFoodItem(FoodItemConstraintTestRow row);
}
