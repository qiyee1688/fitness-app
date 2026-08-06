package com.fitness.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReplaceWorkoutWithTemplateRequest(
        @NotBlank String templateId,
        @Min(0) int expectedPlanVersion
) {
}
