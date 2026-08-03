package com.fitness.dto;

import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserProfileRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotNull FitnessLevel fitnessLevel,
        @NotNull Goal goal,
        @Min(2) @Max(6) int daysPerWeek,
        @NotEmpty List<@NotBlank String> availableEquipment
) {
}
