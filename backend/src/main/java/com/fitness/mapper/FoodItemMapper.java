package com.fitness.mapper;

import com.fitness.domain.FoodCategory;
import com.fitness.domain.FoodItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FoodItemMapper {
    List<FoodItem> find(
            @Param("query") String query,
            @Param("category") FoodCategory category,
            @Param("offset") int offset,
            @Param("limit") int limit);

    int count(@Param("query") String query, @Param("category") FoodCategory category);

    FoodItem findById(@Param("id") String id);
}
