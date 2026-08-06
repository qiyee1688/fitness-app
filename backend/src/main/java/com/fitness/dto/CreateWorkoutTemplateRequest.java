package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkoutTemplateRequest(
        @NotBlank String sourceWorkoutId,
        @Size(max = 80) String name
) {
}
