package com.fitness.config;

import com.fitness.mapper.PrescriptionAdjustmentSchemaMigrationMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 60)
public class PrescriptionAdjustmentSchemaMigration implements ApplicationRunner {
    private final PrescriptionAdjustmentSchemaMigrationMapper migrationMapper;

    public PrescriptionAdjustmentSchemaMigration(PrescriptionAdjustmentSchemaMigrationMapper migrationMapper) {
        this.migrationMapper = migrationMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Confirm applying idempotent prescription adjustment schema.");
        migrationMapper.apply();
        System.out.println("Prescription adjustment schema migration applied.");
    }
}
