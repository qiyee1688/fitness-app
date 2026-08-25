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

    List<PrescriptionAdjustment> findOwnedByActivePlan(@Param("userId") String userId);

    PrescriptionAdjustment findOwnedById(@Param("id") String id, @Param("userId") String userId);

    int applySuggestedPrescription(@Param("adjustment") PrescriptionAdjustment adjustment,
                                   @Param("expectedPlanVersion") int expectedPlanVersion);

    int resolvePending(@Param("id") String id, @Param("status") com.fitness.domain.PrescriptionAdjustmentStatus status,
                       @Param("processedAt") java.time.LocalDateTime processedAt);

    int expirePendingForPlan(
            @Param("planId") String planId, @Param("processedAt") java.time.LocalDateTime processedAt);

    int expirePendingForTargetWorkout(
            @Param("targetWorkoutId") String targetWorkoutId,
            @Param("processedAt") java.time.LocalDateTime processedAt);

    int expirePendingForTargetPrescription(
            @Param("targetPrescriptionId") String targetPrescriptionId,
            @Param("processedAt") java.time.LocalDateTime processedAt);
}
