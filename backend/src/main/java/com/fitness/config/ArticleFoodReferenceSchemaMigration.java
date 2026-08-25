package com.fitness.config;

import com.fitness.mapper.ArticleFoodReferenceSchemaMigrationMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class ArticleFoodReferenceSchemaMigration implements ApplicationRunner {
    private final ArticleFoodReferenceSchemaMigrationMapper migrationMapper;

    public ArticleFoodReferenceSchemaMigration(ArticleFoodReferenceSchemaMigrationMapper migrationMapper) {
        this.migrationMapper = migrationMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Confirm applying idempotent article food reference schema and seed upsert.");
        migrationMapper.apply();
        System.out.println("Article food reference schema migration applied.");
    }
}
