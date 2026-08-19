package com.fitness.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NutritionSchemaMigrationTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final NutritionSchemaMigration migration = new NutritionSchemaMigration(jdbcTemplate);

    @Test
    void appliesIdempotentSchemaAndVersionAwareCoreRuleSeed() throws Exception {
        migration.run(new DefaultApplicationArguments());

        var sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).execute(sqlCaptor.capture());
        assertThat(sqlCaptor.getValue())
                .contains("CREATE TABLE IF NOT EXISTS nutrition_rules")
                .contains("CREATE TABLE IF NOT EXISTS nutrition_tips")
                .contains("uq_nutrition_rules_enabled_no_focus")
                .contains("uq_nutrition_rules_enabled_with_focus")
                .doesNotContain("COALESCE(focus::text, '*')")
                .contains("MUSCLE_GAIN_PRE_WORKOUT")
                .contains("FAT_LOSS_DAILY")
                .contains("WHERE nutrition_rules.version < EXCLUDED.version");
    }
}
