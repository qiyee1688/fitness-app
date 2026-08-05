package com.fitness.mapper;

import com.fitness.domain.Plan;
import com.fitness.domain.Prescription;
import com.fitness.domain.Workout;
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

    int completeWorkout(
            @Param("id") String id,
            @Param("completedAt") LocalDateTime completedAt
    );

    int supersedeActive(@Param("id") String id, @Param("version") int version);

    int insertPlan(Plan plan);

    int insertWorkout(Workout workout);

    int insertPrescription(Prescription prescription);
}
