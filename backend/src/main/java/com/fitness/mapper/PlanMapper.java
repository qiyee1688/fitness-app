package com.fitness.mapper;

import com.fitness.domain.Plan;
import com.fitness.domain.Prescription;
import com.fitness.domain.Workout;
import com.fitness.domain.Exercise;
import com.fitness.domain.ExerciseFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PlanMapper {

    Plan findActiveByUserId(@Param("userId") String userId);

    List<Workout> findWorkoutsByPlanId(@Param("planId") String planId);

    List<Prescription> findPrescriptionsByPlanId(@Param("planId") String planId);

    Workout findWorkoutByPlanIdAndDayNumber(
            @Param("planId") String planId,
            @Param("dayNumber") int dayNumber
    );

    Workout findWorkoutByIdAndPlanId(
            @Param("id") String id,
            @Param("planId") String planId
    );

    List<Prescription> findPrescriptionsByWorkoutId(@Param("workoutId") String workoutId);

    Prescription findPrescriptionInWorkout(
            @Param("workoutId") String workoutId,
            @Param("exerciseId") String exerciseId
    );

    Exercise findSafeSubstitute(
            @Param("userId") String userId,
            @Param("workoutId") String workoutId,
            @Param("exerciseId") String exerciseId,
            @Param("hurtBodyPart") String hurtBodyPart
    );

    int insertExerciseFeedback(ExerciseFeedback feedback);

    int replacePrescriptionExercise(
            @Param("prescriptionId") String prescriptionId,
            @Param("exerciseId") String exerciseId,
            @Param("expectedExerciseId") String expectedExerciseId
    );

    int removePrescriptionForSafety(
            @Param("prescriptionId") String prescriptionId,
            @Param("expectedExerciseId") String expectedExerciseId,
            @Param("removedAt") LocalDateTime removedAt
    );

    int completeWorkout(
            @Param("id") String id,
            @Param("completedAt") LocalDateTime completedAt
    );

    int supersedeActive(@Param("id") String id, @Param("version") int version);

    int insertPlan(Plan plan);

    int insertWorkout(Workout workout);

    int insertPrescription(Prescription prescription);
}
