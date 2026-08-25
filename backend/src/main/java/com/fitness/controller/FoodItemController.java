package com.fitness.controller;

import com.fitness.domain.FoodCategory;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.FoodItemResponse;
import com.fitness.dto.PageResponse;
import com.fitness.service.FoodItemService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/food-items")
@CrossOrigin(origins = "*")
public class FoodItemController {
    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FoodItemResponse>>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) FoodCategory category,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize
    ) {
        return ResponseEntity.ok(ApiResponse.success(foodItemService.list(query, category, page, pageSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodItemResponse>> get(@PathVariable @NotBlank String id) {
        return ResponseEntity.ok(ApiResponse.success(foodItemService.getById(id)));
    }
}
