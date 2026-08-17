package com.fitness.dto;

import com.fitness.domain.OnDemandBodyPart;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record GenerateOnDemandWorkoutRequest(
        @NotNull OnDemandBodyPart bodyPart,
        List<@NotBlank String> equipment,
        boolean saveEquipmentToProfile
) {
}
