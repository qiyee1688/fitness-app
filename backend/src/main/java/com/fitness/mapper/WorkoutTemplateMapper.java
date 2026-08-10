package com.fitness.mapper;

import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
import com.fitness.dto.UpdateWorkoutTemplateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkoutTemplateMapper {
    int insertTemplate(WorkoutTemplate template);

    int insertTemplateExercise(WorkoutTemplateExercise exercise);

    WorkoutTemplate findOwnedById(@Param("id") String id, @Param("ownerUserId") String ownerUserId);

    List<WorkoutTemplate> findOwnedByUserId(@Param("ownerUserId") String ownerUserId);

    List<WorkoutTemplateExercise> findExercisesByTemplateId(@Param("templateId") String templateId);

    int deleteOwnedById(@Param("id") String id, @Param("ownerUserId") String ownerUserId);

    int updateOwnedTemplate(
            @Param("id") String id,
            @Param("ownerUserId") String ownerUserId,
            @Param("expectedVersion") int expectedVersion,
            @Param("name") String name
    );

    int updateTemplateExercisePrescription(
            @Param("templateId") String templateId,
            @Param("ownerUserId") String ownerUserId,
            @Param("exercise") UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate exercise
    );
}
