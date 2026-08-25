package com.fitness.config;

import com.fitness.mapper.FoodItemSchemaMigrationMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class FoodItemSchemaMigration implements ApplicationRunner {
    private final FoodItemSchemaMigrationMapper migrationMapper;

    public FoodItemSchemaMigration(FoodItemSchemaMigrationMapper migrationMapper) {
        this.migrationMapper = migrationMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Confirm applying idempotent FoodItem schema and reviewed catalog seed upsert.");
        migrationMapper.apply();
        System.out.println("Food item schema migration applied.");
    }
}
