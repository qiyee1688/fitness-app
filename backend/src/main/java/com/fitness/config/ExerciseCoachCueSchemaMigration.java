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

/** Applies the idempotent CoachCue schema and data migration after optional Exercise seeding. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExerciseCoachCueSchemaMigration implements ApplicationRunner {
    private static final String MIGRATION_RESOURCE =
            "migration/20260806_add_coach_cues.sql";

    private final JdbcTemplate jdbcTemplate;

    public ExerciseCoachCueSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        ClassPathResource resource = new ClassPathResource(MIGRATION_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            jdbcTemplate.execute(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
