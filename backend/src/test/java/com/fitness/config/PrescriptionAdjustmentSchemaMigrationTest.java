package com.fitness.config;

import com.fitness.mapper.PrescriptionAdjustmentSchemaMigrationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PrescriptionAdjustmentSchemaMigrationTest {
    @Test
    void appliesAdjustmentAuditSchemaThroughMyBatis() {
        PrescriptionAdjustmentSchemaMigrationMapper mapper = mock(PrescriptionAdjustmentSchemaMigrationMapper.class);
        new PrescriptionAdjustmentSchemaMigration(mapper).run(new DefaultApplicationArguments());
        verify(mapper).apply();
    }
}
