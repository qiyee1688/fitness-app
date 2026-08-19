package com.fitness.mapper;

import com.fitness.domain.Exercise;
import com.fitness.domain.ExerciseSubstituteReason;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Exercise Mapper 接口
 */
@Mapper
public interface ExerciseMapper {

    /**
     * 根据 ID 查询 Exercise
     */
    Exercise findById(@Param("id") String id);

    /**
     * 查询所有 Exercise（支持分页）
     */
    List<Exercise> findAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 根据条件筛选 Exercise
     */
    List<Exercise> findByConditions(
        @Param("category") String category,
        @Param("bodyParts") List<String> bodyParts,
        @Param("equipment") String equipment,
        @Param("muscleGroup") String muscleGroup,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    /**
     * 搜索 Exercise（按名称模糊匹配）
     */
    List<Exercise> searchByName(@Param("keyword") String keyword, @Param("limit") int limit);

    /**
     * 插入 Exercise
     */
    int insert(Exercise exercise);

    /**
     * 统计总数
     */
    int count();

    /**
     * 统计符合条件的总数
     */
    int countByConditions(
        @Param("category") String category,
        @Param("bodyParts") List<String> bodyParts,
        @Param("equipment") String equipment,
        @Param("muscleGroup") String muscleGroup
    );

    List<Exercise> findGeneratorCandidates(@Param("equipment") List<String> equipment);

    List<Exercise> findOnDemandCandidates(
            @Param("bodyParts") List<String> bodyParts,
            @Param("equipment") List<String> equipment,
            @Param("preferredEquipment") List<String> preferredEquipment,
            @Param("ownerUserId") String ownerUserId
    );

    List<Exercise> findTemplateSubstitutes(
            @Param("fromExerciseId") String fromExerciseId,
            @Param("equipment") List<String> equipment,
            @Param("reason") ExerciseSubstituteReason reason
    );

    int insertTemplateSubstitute(
            @Param("fromExerciseId") String fromExerciseId,
            @Param("toExerciseId") String toExerciseId,
            @Param("reason") ExerciseSubstituteReason reason,
            @Param("priority") int priority
    );
}
