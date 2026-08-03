package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.mapper.ExerciseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Exercise Service
 */
@Service
@Transactional
public class ExerciseService {

    private final ExerciseMapper exerciseMapper;

    public ExerciseService(ExerciseMapper exerciseMapper) {
        this.exerciseMapper = exerciseMapper;
    }

    /**
     * 根据 ID 获取 Exercise
     */
    public Exercise getExerciseById(String id) {
        return exerciseMapper.findById(id);
    }

    /**
     * 获取所有 Exercise（分页）
     */
    public List<Exercise> getAllExercises(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return exerciseMapper.findAll(offset, pageSize);
    }

    /**
     * 根据条件筛选 Exercise
     */
    public List<Exercise> getExercisesByConditions(
            String category,
            String bodyPart,
            String equipment,
            String muscleGroup,
            int page,
            int pageSize
    ) {
        int offset = (page - 1) * pageSize;
        return exerciseMapper.findByConditions(
                category,
                parseListFilter(bodyPart),
                equipment,
                muscleGroup,
                offset,
                pageSize
        );
    }

    /**
     * 搜索 Exercise（按名称）
     */
    public List<Exercise> searchExercises(String keyword, int limit) {
        return exerciseMapper.searchByName(keyword, limit);
    }

    /**
     * 创建 Exercise
     */
    public void createExercise(Exercise exercise) {
        exerciseMapper.insert(exercise);
    }

    /**
     * 获取总数
     */
    public int getTotalCount() {
        return exerciseMapper.count();
    }

    /**
     * 获取筛选后的总数
     */
    public int getCountByConditions(String category, String bodyPart, String equipment, String muscleGroup) {
        return exerciseMapper.countByConditions(category, parseListFilter(bodyPart), equipment, muscleGroup);
    }

    private List<String> parseListFilter(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
