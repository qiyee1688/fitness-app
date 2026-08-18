package com.fitness.dto;

import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UserProfileRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotNull FitnessLevel fitnessLevel,
        @NotNull Goal goal,
        @Min(2) @Max(6) int daysPerWeek,
        @NotEmpty List<@NotBlank String> availableEquipment,
        @DecimalMin("30.0") @DecimalMax("300.0") @Digits(integer = 3, fraction = 1) BigDecimal weightKg
) {
    public UserProfileRequest(
            String username,
            String email,
            FitnessLevel fitnessLevel,
            Goal goal,
            int daysPerWeek,
            List<String> availableEquipment
    ) {
        this(username, email, fitnessLevel, goal, daysPerWeek, availableEquipment, null);
    }
}
