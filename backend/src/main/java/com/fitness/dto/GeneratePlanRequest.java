package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record GeneratePlanRequest(
        @NotBlank String username,
        LocalDate startDate
) {
}
