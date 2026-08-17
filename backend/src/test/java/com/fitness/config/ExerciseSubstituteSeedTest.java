package com.fitness.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.domain.ExerciseSubstituteReason;
import com.fitness.mapper.ExerciseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExerciseSubstituteSeedTest {
    @Test
    void importsOnlyConfirmedEquipmentSwapRelations() throws Exception {
        ExerciseMapper exerciseMapper = mock(ExerciseMapper.class);
        ExerciseSubstituteSeed seed = new ExerciseSubstituteSeed(exerciseMapper, new ObjectMapper());

        seed.run(new DefaultApplicationArguments());

        verify(exerciseMapper).insertTemplateSubstitute(
                "0662", "0493", ExerciseSubstituteReason.EQUIPMENT_SWAP, 10);
        verify(exerciseMapper).insertTemplateSubstitute(
                "0025", "0289", ExerciseSubstituteReason.EQUIPMENT_SWAP, 10);
        verify(exerciseMapper).insertTemplateSubstitute(
                "0043", "0413", ExerciseSubstituteReason.EQUIPMENT_SWAP, 10);
    }
}
