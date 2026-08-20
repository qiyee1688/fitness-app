package com.fitness.controller;

import com.fitness.domain.Exercise;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.KnowledgeArticleSummary;
import com.fitness.dto.PageResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.service.ExerciseService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exercise REST Controller
 */
@RestController
@RequestMapping("/exercises")
@CrossOrigin(origins = "*")
@Validated
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    /**
     * GET /exercises/{id} - 获取单个 Exercise
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Exercise>> getExerciseById(@PathVariable @NotBlank String id) {
        Exercise exercise = exerciseService.getExerciseById(id);
        if (exercise == null) {
            throw new BusinessException(ErrorCode.EXERCISE_NOT_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.success(exercise));
    }

    @GetMapping("/{id}/articles")
    public ResponseEntity<ApiResponse<List<KnowledgeArticleSummary>>> getRelatedArticles(
            @PathVariable @NotBlank String id
    ) {
        if (exerciseService.getExerciseById(id) == null) {
            throw new BusinessException(ErrorCode.EXERCISE_NOT_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.success(exerciseService.getPublishedArticles(id)));
    }

    /**
     * GET /exercises - 获取所有 Exercise（分页 + 筛选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Exercise>>> getExercises(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String bodyPart,
            @RequestParam(required = false) String equipment,
            @RequestParam(required = false) String muscleGroup
    ) {
        List<Exercise> exercises;
        int total;

        if (category != null || bodyPart != null || equipment != null || muscleGroup != null) {
            exercises = exerciseService.getExercisesByConditions(
                    category, bodyPart, equipment, muscleGroup, page, pageSize
            );
            total = exerciseService.getCountByConditions(category, bodyPart, equipment, muscleGroup);
        } else {
            exercises = exerciseService.getAllExercises(page, pageSize);
            total = exerciseService.getTotalCount();
        }

        PageResponse<Exercise> response = new PageResponse<>(exercises, page, pageSize, total);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /exercises/search - 搜索 Exercise
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Exercise>>> searchExercises(
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        List<Exercise> exercises = exerciseService.searchExercises(keyword, limit);
        return ResponseEntity.ok(ApiResponse.success(exercises));
    }
}
