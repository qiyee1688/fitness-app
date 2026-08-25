package com.fitness.config;

import com.fitness.mapper.FoodItemSchemaMigrationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FoodItemSchemaMigrationTest {
    private final FoodItemSchemaMigrationMapper migrationMapper = mock(FoodItemSchemaMigrationMapper.class);
    private final FoodItemSchemaMigration migration = new FoodItemSchemaMigration(migrationMapper);

    @Test
    void appliesIdempotentFoodItemCatalogThroughMyBatis() {
        migration.run(new DefaultApplicationArguments());

        verify(migrationMapper).apply();
    }
}
