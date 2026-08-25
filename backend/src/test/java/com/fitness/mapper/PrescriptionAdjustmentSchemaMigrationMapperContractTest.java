package com.fitness.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;

class PrescriptionAdjustmentSchemaMigrationMapperContractTest {
    @Test
    void parsesTheProductionMigrationMapper() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/PrescriptionAdjustmentSchemaMigrationMapper.xml";

        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }

        assertThat(configuration.hasStatement(
                "com.fitness.mapper.PrescriptionAdjustmentSchemaMigrationMapper.apply")).isTrue();
    }
}
