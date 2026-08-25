package com.fitness.mapper;

import com.fitness.domain.ExerciseFeedback;
import com.fitness.domain.Prescription;
import com.fitness.domain.PrescriptionAdjustment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrescriptionAdjustmentMapper {
    List<ExerciseFeedback> findRecentUnconsumedFeedbacks(
            @Param("planId") String planId, @Param("exerciseId") String exerciseId);

    Prescription findNextUnstartedPrescription(
            @Param("planId") String planId,
            @Param("exerciseId") String exerciseId,
            @Param("afterDayNumber") int afterDayNumber);

    String findAllowedHardFeedbackSubstitute(
            @Param("planId") String planId,
            @Param("workoutId") String workoutId,
            @Param("exerciseId") String exerciseId);

    int insert(PrescriptionAdjustment adjustment);
}
