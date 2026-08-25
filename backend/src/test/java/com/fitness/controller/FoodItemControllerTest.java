package com.fitness.controller;

import com.fitness.domain.FoodCategory;
import com.fitness.domain.MacroTarget;
import com.fitness.domain.MacroTargetBasis;
import com.fitness.domain.MacroTargetValue;
import com.fitness.domain.NutritionUnit;
import com.fitness.dto.FoodItemConversionResponse;
import com.fitness.dto.FoodItemResponse;
import com.fitness.dto.PageResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.service.FoodItemService;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FoodItemController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Mapper.class)
)
class FoodItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FoodItemService foodItemService;

    @Test
    void listReturnsPagedUnifiedResponseForSearchAndCategory() throws Exception {
        when(foodItemService.list("egg", FoodCategory.PROTEIN, 2, 10))
                .thenReturn(new PageResponse<>(List.of(egg()), 2, 10, 11));

        mockMvc.perform(get("/food-items")
                        .param("query", "egg")
                        .param("category", "PROTEIN")
                        .param("page", "2")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(11))
                .andExpect(jsonPath("$.data.items[0].id").value("egg-whole"))
                .andExpect(jsonPath("$.data.items[0].proteinGrams").value(6.3));

        verify(foodItemService).list("egg", FoodCategory.PROTEIN, 2, 10);
    }

    @Test
    void getReturnsFoodItemInUnifiedResponse() throws Exception {
        when(foodItemService.getById("egg-whole")).thenReturn(egg());

        mockMvc.perform(get("/food-items/egg-whole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("鸡蛋"))
                .andExpect(jsonPath("$.data.servingGrams").value(50));

        verify(foodItemService).getById("egg-whole");
    }

    @Test
    void convertReturnsBaseFoodServingAndScaledMacrosInUnifiedResponse() throws Exception {
        when(foodItemService.convert("egg-whole", new BigDecimal("1.5"))).thenReturn(conversion());

        mockMvc.perform(get("/food-items/egg-whole/conversion").param("servings", "1.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.foodItem.name").value("鸡蛋"))
                .andExpect(jsonPath("$.data.foodItem.servingDescriptionEn").value("1 large egg"))
                .andExpect(jsonPath("$.data.servings").value(1.5))
                .andExpect(jsonPath("$.data.macroTargets.protein.value").value(9.5))
                .andExpect(jsonPath("$.data.macroTargets.kcal.value").value(108));

        verify(foodItemService).convert("egg-whole", new BigDecimal("1.5"));
    }

    @Test
    void listReturnsAnEmptyUnifiedResponseWhenNoFoodItemsMatch() throws Exception {
        when(foodItemService.list("seaweed", null, 1, 20))
                .thenReturn(new PageResponse<>(List.of(), 1, 20, 0));

        mockMvc.perform(get("/food-items").param("query", "seaweed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void getReturnsUnifiedNotFoundResponseForUnknownFoodItem() throws Exception {
        when(foodItemService.getById("missing-item"))
                .thenThrow(new BusinessException(ErrorCode.FOOD_ITEM_NOT_FOUND));

        mockMvc.perform(get("/food-items/missing-item"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40412));
    }

    @Test
    void listRejectsInvalidPaginationAndCategory() throws Exception {
        mockMvc.perform(get("/food-items").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/food-items").param("category", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void conversionRejectsMissingOrOutOfRangeServingsWithUnifiedValidationError() throws Exception {
        mockMvc.perform(get("/food-items/egg-whole/conversion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/food-items/egg-whole/conversion").param("servings", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/food-items/egg-whole/conversion").param("servings", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        mockMvc.perform(get("/food-items/egg-whole/conversion").param("servings", "100.01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void conversionReturnsUnifiedNotFoundResponseForUnknownFoodItems() throws Exception {
        when(foodItemService.convert("missing-item", BigDecimal.ONE))
                .thenThrow(new BusinessException(ErrorCode.FOOD_ITEM_NOT_FOUND));

        mockMvc.perform(get("/food-items/missing-item/conversion").param("servings", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40412));
    }

    private FoodItemResponse egg() {
        return new FoodItemResponse(
                "egg-whole", "鸡蛋", "Whole egg", FoodCategory.PROTEIN,
                "1 个大鸡蛋", "1 large egg", new BigDecimal("50"),
                new BigDecimal("6.3"), new BigDecimal("0.4"),
                new BigDecimal("5.0"), new BigDecimal("72"));
    }

    private FoodItemConversionResponse conversion() {
        return new FoodItemConversionResponse(
                egg(),
                new BigDecimal("1.5"),
                new MacroTarget(
                        new MacroTargetValue(new BigDecimal("9.5"), NutritionUnit.GRAMS, MacroTargetBasis.ABSOLUTE),
                        new MacroTargetValue(new BigDecimal("0.6"), NutritionUnit.GRAMS, MacroTargetBasis.ABSOLUTE),
                        new MacroTargetValue(new BigDecimal("7.5"), NutritionUnit.GRAMS, MacroTargetBasis.ABSOLUTE),
                        new MacroTargetValue(new BigDecimal("108.0"), NutritionUnit.KILOCALORIES, MacroTargetBasis.ABSOLUTE)));
    }
}
