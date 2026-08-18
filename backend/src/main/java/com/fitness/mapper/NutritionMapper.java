package com.fitness.mapper;

import com.fitness.domain.Goal;
import com.fitness.domain.NutritionRule;
import com.fitness.domain.NutritionTiming;
import com.fitness.domain.NutritionTip;
import com.fitness.domain.TrainingDayFocus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NutritionMapper {
    NutritionRule findMatchingRule(
            @Param("goal") Goal goal,
            @Param("focus") TrainingDayFocus focus,
            @Param("timing") NutritionTiming timing
    );

    int insertTip(NutritionTip tip);

    List<NutritionTip> findByWorkoutId(@Param("workoutId") String workoutId);

    List<NutritionTip> findByPlanId(@Param("planId") String planId);
}
