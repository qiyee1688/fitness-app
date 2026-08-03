package com.fitness.dto;

import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
        String userId,
        String username,
        String email,
        String profileId,
        FitnessLevel fitnessLevel,
        Goal goal,
        int daysPerWeek,
        List<String> availableEquipment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
