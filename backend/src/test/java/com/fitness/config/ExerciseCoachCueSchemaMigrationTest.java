package com.fitness.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExerciseCoachCueSchemaMigrationTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final ExerciseCoachCueSchemaMigration migration =
            new ExerciseCoachCueSchemaMigration(jdbcTemplate);

    @Test
    void appliesIdempotentSchemaAndDataMigration() throws Exception {
        migration.run(new DefaultApplicationArguments());

        var migrationSql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).execute(migrationSql.capture());
        assertThat(migrationSql.getValue())
                .contains(
                        "ADD COLUMN IF NOT EXISTS coach_cue",
                        "selection_priority INT NOT NULL DEFAULT 1000",
                        "COALESCE(coach_cue",
                        "COALESCE(coach_cue_en");
    }
}
