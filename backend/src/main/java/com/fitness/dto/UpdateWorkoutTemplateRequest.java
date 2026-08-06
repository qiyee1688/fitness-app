package com.fitness.dto;

import com.fitness.domain.LoadType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateWorkoutTemplateRequest(
        @Min(0) int expectedVersion,
        @NotBlank @Size(max = 80) String name,
        @NotEmpty @Valid List<ExercisePrescriptionUpdate> exercises
) {
    public record ExercisePrescriptionUpdate(
            @NotBlank String templateExerciseId,
            @Min(1) int sequence,
            @Min(1) @Max(20) int sets,
            @Min(1) @Max(100) int reps,
            @DecimalMin("0.0") @DecimalMax("999.99") BigDecimal load,
            @NotNull LoadType loadType,
            @DecimalMin("6.0") @DecimalMax("10.0") BigDecimal rpe
    ) {
    }
}
