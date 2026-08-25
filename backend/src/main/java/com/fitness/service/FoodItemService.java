package com.fitness.service;

import com.fitness.domain.FoodCategory;
import com.fitness.domain.FoodItem;
import com.fitness.dto.FoodItemResponse;
import com.fitness.dto.PageResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.FoodItemMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodItemService {
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
        FoodItem item = foodItemMapper.findById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.FOOD_ITEM_NOT_FOUND);
        }
        return toResponse(item);
    }

    private FoodItemResponse toResponse(FoodItem item) {
        return new FoodItemResponse(
                item.getId(), item.getName(), item.getNameEn(), item.getCategory(),
                item.getServingDescription(), item.getServingDescriptionEn(), item.getServingGrams(),
                item.getProteinGrams(), item.getCarbsGrams(), item.getFatGrams(), item.getKcal());
    }
}
