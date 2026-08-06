package com.fitness.mapper;

import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
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
}
