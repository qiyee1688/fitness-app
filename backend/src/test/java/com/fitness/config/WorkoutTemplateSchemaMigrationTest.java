package com.fitness.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class WorkoutTemplateSchemaMigrationTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final WorkoutTemplateSchemaMigration migration = new WorkoutTemplateSchemaMigration(jdbcTemplate);

    @Test
    void skipsMigrationWhenTemplateTableAlreadyExists() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), org.mockito.ArgumentMatchers.eq(Boolean.class)))
                .thenReturn(true);

        migration.run(new DefaultApplicationArguments());

        var schemaCheck = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(schemaCheck.capture(), org.mockito.ArgumentMatchers.eq(Boolean.class));
        assertThat(schemaCheck.getValue()).contains("workout_templates", "profile_snapshot");
        verifyNoMoreInteractions(jdbcTemplate);
    }

    @Test
    void appliesMigrationWhenTemplateTableIsMissing() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), org.mockito.ArgumentMatchers.eq(Boolean.class)))
                .thenReturn(false);

        migration.run(new DefaultApplicationArguments());

        var migrationSql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).execute(migrationSql.capture());
        assertThat(migrationSql.getValue()).contains("CREATE TABLE IF NOT EXISTS workout_templates");
    }
}
