package com.fitness.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Applies the template migration to databases created before workout templates existed. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkoutTemplateSchemaMigration implements ApplicationRunner {
    private static final String TEMPLATE_TABLE_CHECK =
            "SELECT to_regclass('public.workout_templates') IS NOT NULL";
    private static final String MIGRATION_RESOURCE =
            "migration/20260806_add_workout_templates.sql";

    private final JdbcTemplate jdbcTemplate;

    public WorkoutTemplateSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (Boolean.TRUE.equals(jdbcTemplate.queryForObject(TEMPLATE_TABLE_CHECK, Boolean.class))) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(MIGRATION_RESOURCE);
        String migration;
        try (InputStream inputStream = resource.getInputStream()) {
            migration = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        jdbcTemplate.execute(migration);
        System.out.println("Workout template schema migration applied.");
    }
}
