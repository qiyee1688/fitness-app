package com.fitness.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleFoodReferenceSchemaMigrationMapper {
    void apply();
}
