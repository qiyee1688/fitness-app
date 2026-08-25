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

    @Test
    void resolutionQueriesLockTheAdjustmentAndGuardTheOnlyMutableTarget() {
        BoundSql ownedSql = configuration.getMappedStatement(
                "com.fitness.mapper.PrescriptionAdjustmentMapper.findOwnedById")
                .getBoundSql(Map.of("id", "33333333-3333-3333-3333-333333333333", "userId", "user-id"));
        String mapperXml = readMapperXml();

        assertThat(ownedSql.getSql()).contains("p.status = 'ACTIVE'::plan_status_enum")
                .contains("FOR UPDATE OF pa");
        assertThat(mapperXml).contains("AND target.removed_at IS NULL")
                .contains("id=\"expirePendingForTargetWorkout\"")
                .contains("WHERE target_workout_id = #{targetWorkoutId}::uuid");
    }

    private String readMapperXml() {
        try (Reader reader = Resources.getResourceAsReader("mapper/PrescriptionAdjustmentMapper.xml")) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
