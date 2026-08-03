package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.mapper.ExerciseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseMapper exerciseMapper;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    void getExercisesByConditionsSplitsCombinedBodyPartFilter() {
        when(exerciseMapper.findByConditions(null, List.of("upper legs", "lower legs"), null, null, 20, 20))
                .thenReturn(List.of(new Exercise()));

        exerciseService.getExercisesByConditions(null, "upper legs,lower legs", null, null, 2, 20);

        verify(exerciseMapper).findByConditions(null, List.of("upper legs", "lower legs"), null, null, 20, 20);
    }

    @Test
    void getCountByConditionsSplitsCombinedBodyPartFilter() {
        when(exerciseMapper.countByConditions(null, List.of("upper legs", "lower legs"), null, null))
                .thenReturn(12);

        exerciseService.getCountByConditions(null, "upper legs, lower legs", null, null);

        verify(exerciseMapper).countByConditions(null, List.of("upper legs", "lower legs"), null, null);
    }
}
