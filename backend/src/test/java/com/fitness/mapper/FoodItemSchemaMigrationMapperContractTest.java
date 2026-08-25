package com.fitness.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class FoodItemSchemaMigrationMapperContractTest {
    @Test
    void definesIdempotentCatalogConstraintsSeedsAndTrigramSearchIndexes() throws Exception {
        String migration = new ClassPathResource("mapper/FoodItemSchemaMigrationMapper.xml")
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertThat(migration)
                .contains("CREATE EXTENSION IF NOT EXISTS pg_trgm")
                .contains("CREATE TABLE IF NOT EXISTS food_items")
                .contains("chk_food_item_serving_grams_positive")
                .contains("chk_food_item_macros_non_negative")
                .contains("idx_food_items_category")
                .contains("USING GIN (name gin_trgm_ops)")
                .contains("USING GIN (name_en gin_trgm_ops)")
                .contains("'egg-whole'")
                .contains("'rice-cooked'")
                .contains("'chicken-breast-cooked'")
                .contains("'milk-low-fat'")
                .contains("'banana-medium'")
                .contains("ON CONFLICT (id) DO UPDATE");
    }
}
