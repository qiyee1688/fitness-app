package com.fitness.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.domain.ExerciseSubstituteReason;
import com.fitness.mapper.ExerciseMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ExerciseSubstituteSeed implements ApplicationRunner {
    private static final String CATALOG_RESOURCE = "data/exercise-substitutes-reviewed.json";

    private final ExerciseMapper exerciseMapper;
    private final ObjectMapper objectMapper;

    public ExerciseSubstituteSeed(ExerciseMapper exerciseMapper, ObjectMapper objectMapper) {
        this.exerciseMapper = exerciseMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource(CATALOG_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode relations = objectMapper.readTree(inputStream).path("relations");
            for (JsonNode relation : relations) {
                if (!relation.path("confirmed").asBoolean(false)) {
                    continue;
                }
                exerciseMapper.insertTemplateSubstitute(
                        relation.required("fromExerciseId").asText(),
                        relation.required("toExerciseId").asText(),
                        ExerciseSubstituteReason.valueOf(relation.required("reason").asText()),
                        relation.required("priority").asInt());
            }
        }
    }
}
