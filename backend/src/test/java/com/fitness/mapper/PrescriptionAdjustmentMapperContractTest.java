package com.fitness.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PrescriptionAdjustmentMapperContractTest {
    private Configuration configuration;

    @BeforeEach
    void parseMapper() throws Exception {
        configuration = new Configuration();
        String resource = "mapper/PrescriptionAdjustmentMapper.xml";
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }

    @Test
    void candidateQueriesBindThePlanExerciseAndSourceDayWithoutInterpolatingValues() {
        BoundSql feedbackSql = configuration.getMappedStatement(
                "com.fitness.mapper.PrescriptionAdjustmentMapper.findRecentUnconsumedFeedbacks")
                .getBoundSql(Map.of("planId", "33333333-3333-3333-3333-333333333333", "exerciseId", "core"));
        BoundSql targetSql = configuration.getMappedStatement(
                "com.fitness.mapper.PrescriptionAdjustmentMapper.findNextUnstartedPrescription")
                .getBoundSql(Map.of("planId", "33333333-3333-3333-3333-333333333333", "exerciseId", "core", "afterDayNumber", 2));

        assertThat(feedbackSql.getSql()).contains("NOT EXISTS (SELECT 1 FROM prescription_adjustments")
                .contains("w.plan_id = ?::uuid AND ef.exercise_id = ?");
        assertThat(feedbackSql.getParameterMappings()).extracting("property")
                .containsExactly("planId", "exerciseId");
        assertThat(targetSql.getSql()).contains("w.day_number > ?")
                .contains("w.status = 'READY'::workout_status_enum");
        assertThat(targetSql.getParameterMappings()).extracting("property")
                .containsExactly("planId", "exerciseId", "afterDayNumber");
    }
}
