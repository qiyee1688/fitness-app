package com.fitness.service;

import com.fitness.domain.FoodCategory;
import com.fitness.domain.FoodItem;
import com.fitness.domain.MacroTargetBasis;
import com.fitness.domain.NutritionUnit;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.FoodItemMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class FoodItemServiceTest {
    private final FoodItemMapper foodItemMapper = mock(FoodItemMapper.class);
    private final FoodItemService service = new FoodItemService(foodItemMapper);

    @Test
    void returnsPagedFoodItemsForNameAndCategorySearch() {
        FoodItem egg = egg();
        when(foodItemMapper.find("egg", FoodCategory.PROTEIN, 20, 20)).thenReturn(List.of(egg));
        when(foodItemMapper.count("egg", FoodCategory.PROTEIN)).thenReturn(21);

        var response = service.list("egg", FoodCategory.PROTEIN, 2, 20);

        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getTotal()).isEqualTo(21);
        assertThat(response.getItems()).extracting("id").containsExactly("egg-whole");
    }

    @Test
    void rejectsUnknownFoodItemWithUnifiedNotFoundErrorCode() {
        when(foodItemMapper.findById("missing-item")).thenReturn(null);

        assertThatThrownBy(() -> service.getById("missing-item"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FOOD_ITEM_NOT_FOUND));
    }

    @Test
    void returnsAnEmptyPageWhenNoFoodItemsMatchTheSearch() {
        when(foodItemMapper.find("seaweed", null, 0, 20)).thenReturn(List.of());
        when(foodItemMapper.count("seaweed", null)).thenReturn(0);

        var response = service.list("seaweed", null, 1, 20);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotal()).isZero();
    }

    @Test
    void convertsStandardFoodServingsIntoExplainableMacroTargets() {
        when(foodItemMapper.findById("egg-whole")).thenReturn(egg());

        var conversion = service.convert("egg-whole", new BigDecimal("2"));

        assertThat(conversion.foodItem().id()).isEqualTo("egg-whole");
        assertThat(conversion.foodItem().servingDescription()).isEqualTo("1 个大鸡蛋");
        assertThat(conversion.foodItem().servingDescriptionEn()).isEqualTo("1 large egg");
        assertThat(conversion.servings()).isEqualByComparingTo("2");
        assertThat(conversion.macroTargets().protein().value()).isEqualByComparingTo("12.6");
        assertThat(conversion.macroTargets().protein().unit()).isEqualTo(NutritionUnit.GRAMS);
        assertThat(conversion.macroTargets().protein().basis()).isEqualTo(MacroTargetBasis.ABSOLUTE);
        assertThat(conversion.macroTargets().kcal().value()).isEqualByComparingTo("144.0");
        assertThat(conversion.macroTargets().kcal().unit()).isEqualTo(NutritionUnit.KILOCALORIES);
    }

    @Test
    void roundsFractionalServingsAndRejectsAmountsOutsideTheSupportedRange() {
        when(foodItemMapper.findById("milk-low-fat")).thenReturn(milk());

        var conversion = service.convert("milk-low-fat", new BigDecimal("1.25"));

        assertThat(conversion.macroTargets().protein().value()).isEqualByComparingTo("10.6");
        assertThat(conversion.macroTargets().carbs().value()).isEqualByComparingTo("15.0");
        assertThat(conversion.macroTargets().kcal().value()).isEqualByComparingTo("150.0");
        verify(foodItemMapper).findById("milk-low-fat");

        assertThatThrownBy(() -> service.convert("milk-low-fat", BigDecimal.ZERO))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
        assertThatThrownBy(() -> service.convert("milk-low-fat", new BigDecimal("100.01")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
        verifyNoMoreInteractions(foodItemMapper);
    }

    @Test
    void rejectsConversionsForUnknownFoodItemsWithTheUnifiedNotFoundErrorCode() {
        when(foodItemMapper.findById("missing-item")).thenReturn(null);

        assertThatThrownBy(() -> service.convert("missing-item", BigDecimal.ONE))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FOOD_ITEM_NOT_FOUND));
    }

    private FoodItem egg() {
        FoodItem item = new FoodItem();
        item.setId("egg-whole");
        item.setName("鸡蛋");
        item.setNameEn("Whole egg");
        item.setCategory(FoodCategory.PROTEIN);
        item.setServingDescription("1 个大鸡蛋");
        item.setServingDescriptionEn("1 large egg");
        item.setServingGrams(new BigDecimal("50"));
        item.setProteinGrams(new BigDecimal("6.3"));
        item.setCarbsGrams(new BigDecimal("0.4"));
        item.setFatGrams(new BigDecimal("5.0"));
        item.setKcal(new BigDecimal("72"));
        return item;
    }

    private FoodItem milk() {
        FoodItem item = new FoodItem();
        item.setId("milk-low-fat");
        item.setName("低脂牛奶");
        item.setNameEn("Low-fat milk");
        item.setCategory(FoodCategory.DAIRY);
        item.setServingDescription("1 杯");
        item.setServingDescriptionEn("1 cup");
        item.setServingGrams(new BigDecimal("250"));
        item.setProteinGrams(new BigDecimal("8.5"));
        item.setCarbsGrams(new BigDecimal("12.0"));
        item.setFatGrams(new BigDecimal("2.5"));
        item.setKcal(new BigDecimal("120"));
        return item;
    }
}
