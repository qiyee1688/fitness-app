package com.fitness.mapper;

import com.fitness.domain.Prescription;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WorkoutMapper {
    int insertWorkout(Workout workout);

    int insertPrescription(Prescription prescription);

    Workout findOwnedById(@Param("id") String id, @Param("ownerUserId") String ownerUserId);

    List<Prescription> findPrescriptionsByWorkoutId(@Param("workoutId") String workoutId);

    int transitionStatus(
            @Param("id") String id,
            @Param("ownerUserId") String ownerUserId,
            @Param("expectedStatus") WorkoutStatus expectedStatus,
            @Param("newStatus") WorkoutStatus newStatus,
            @Param("changedAt") LocalDateTime changedAt
    );

    int deleteExpiredDrafts(@Param("expiredBefore") LocalDateTime expiredBefore);
}
