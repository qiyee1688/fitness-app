package com.fitness.service;

import com.fitness.domain.FoodCategory;
import com.fitness.domain.FoodItem;
import com.fitness.domain.MacroTarget;
import com.fitness.domain.MacroTargetBasis;
import com.fitness.domain.MacroTargetValue;
import com.fitness.domain.NutritionUnit;
import com.fitness.dto.FoodItemConversionResponse;
import com.fitness.dto.FoodItemResponse;
import com.fitness.dto.PageResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.FoodItemMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class FoodItemService {
    private static final BigDecimal MAX_SERVINGS = new BigDecimal("100.0");

    private final FoodItemMapper foodItemMapper;

    public FoodItemService(FoodItemMapper foodItemMapper) {
        this.foodItemMapper = foodItemMapper;
    }

    public PageResponse<FoodItemResponse> list(String query, FoodCategory category, int page, int pageSize) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        int offset = (page - 1) * pageSize;
        List<FoodItemResponse> items = foodItemMapper.find(normalizedQuery, category, offset, pageSize).stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(items, page, pageSize, foodItemMapper.count(normalizedQuery, category));
    }

    public FoodItemResponse getById(String id) {
        return toResponse(requireItem(id));
    }

    public FoodItemConversionResponse convert(String id, BigDecimal servings) {
        validateServings(servings);
        FoodItem item = requireItem(id);
        return new FoodItemConversionResponse(
                toResponse(item),
                servings,
                new MacroTarget(
                        convertValue(item.getProteinGrams(), servings, NutritionUnit.GRAMS),
                        convertValue(item.getCarbsGrams(), servings, NutritionUnit.GRAMS),
                        convertValue(item.getFatGrams(), servings, NutritionUnit.GRAMS),
                        convertValue(item.getKcal(), servings, NutritionUnit.KILOCALORIES)));
    }

    private FoodItem requireItem(String id) {
        FoodItem item = foodItemMapper.findById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.FOOD_ITEM_NOT_FOUND);
        }
        return item;
    }

    private void validateServings(BigDecimal servings) {
        if (servings == null || servings.signum() <= 0 || servings.compareTo(MAX_SERVINGS) > 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private MacroTargetValue convertValue(BigDecimal baseValue, BigDecimal servings, NutritionUnit unit) {
        return new MacroTargetValue(
                baseValue.multiply(servings).setScale(1, RoundingMode.HALF_UP),
                unit,
                MacroTargetBasis.ABSOLUTE);
    }

    private FoodItemResponse toResponse(FoodItem item) {
        return FoodItemResponse.from(item);
    }
}
