package com.fitness.service;

import com.fitness.domain.FoodCategory;
import com.fitness.domain.FoodItem;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.FoodItemMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
}
