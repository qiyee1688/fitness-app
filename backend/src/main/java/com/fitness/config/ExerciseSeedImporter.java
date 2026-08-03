package com.fitness.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.domain.Exercise;
import com.fitness.service.ExerciseService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "fitness.seed.exercises.enabled", havingValue = "true")
public class ExerciseSeedImporter implements ApplicationRunner {

    private static final String DATASET_BASE_URL =
            "https://raw.githubusercontent.com/hasaneyldrm/exercises-dataset/main/";

    private final ExerciseService exerciseService;
    private final ObjectMapper objectMapper;

    public ExerciseSeedImporter(ExerciseService exerciseService, ObjectMapper objectMapper) {
        this.exerciseService = exerciseService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int existingCount = exerciseService.getTotalCount();
        if (existingCount > 0) {
            System.out.printf("Exercise seed skipped: exercises table already has %d rows.%n", existingCount);
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/exercises-dataset/exercises.json");
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            int importedCount = 0;
            for (JsonNode node : root) {
                exerciseService.createExercise(toExercise(node));
                importedCount++;
            }
            System.out.printf("Exercise seed completed: imported %d exercises.%n", importedCount);
        }
    }

    private Exercise toExercise(JsonNode node) {
        Exercise exercise = new Exercise();
        exercise.setId(requiredText(node, "id"));
        exercise.setName(requiredText(node, "name"));
        exercise.setCategory(requiredText(node, "category"));
        exercise.setBodyPart(requiredText(node, "body_part"));
        exercise.setEquipment(requiredText(node, "equipment"));
        exercise.setTarget(requiredText(node, "target"));
        exercise.setMuscleGroup(optionalText(node, "muscle_group"));
        exercise.setSecondaryMuscles(convert(node.get("secondary_muscles"), new TypeReference<>() {}));
        exercise.setInstructionSteps(convert(node.get("instruction_steps"), new TypeReference<>() {}));
        exercise.setImageUrl(toDatasetUrl(optionalText(node, "image")));
        exercise.setGifUrl(toDatasetUrl(optionalText(node, "gif_url")));
        return exercise;
    }

    private String requiredText(JsonNode node, String fieldName) {
        String value = optionalText(node, fieldName);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required exercise field: " + fieldName);
        }
        return value;
    }

    private String optionalText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private <T> T convert(JsonNode node, TypeReference<T> typeReference) {
        return node == null || node.isNull() ? null : objectMapper.convertValue(node, typeReference);
    }

    private String toDatasetUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        return DATASET_BASE_URL + path;
    }
}
