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

/**
 * Applies the idempotent nutrition schema and six core rule seeds.
 *
 * <p>This is a DDL/bootstrap exception to the mapper-only DML boundary: PostgreSQL enum,
 * table, index, and seed-version statements must run before MyBatis can serve the feature.
 * Runtime nutrition reads and writes remain parameterized in {@code NutritionMapper}.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class NutritionSchemaMigration implements ApplicationRunner {
    private static final String MIGRATION_RESOURCE = "migration/20260818_add_nutrition_tips.sql";

    private final JdbcTemplate jdbcTemplate;

    public NutritionSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        ClassPathResource resource = new ClassPathResource(MIGRATION_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            jdbcTemplate.execute(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
        System.out.println("Nutrition schema migration and rule seed applied.");
    }
}
