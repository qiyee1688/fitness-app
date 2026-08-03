package com.fitness.domain;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Exercise 实体类
 * 对应 exercises 表
 */
@Data
public class Exercise {
    private String id;
    private String name;
    private String category;
    private String bodyPart;
    private String equipment;
    private String target;
    private String muscleGroup;
    private List<String> secondaryMuscles;
    private Map<String, List<String>> instructionSteps;  // {"en": [...], "zh": [...]}
    private String gifUrl;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
