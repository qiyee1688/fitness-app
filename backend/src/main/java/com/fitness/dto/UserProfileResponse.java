package com.fitness.dto;

import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;

import java.math.BigDecimal;
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
        BigDecimal weightKg,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserProfileResponse(
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
        this(userId, username, email, profileId, fitnessLevel, goal, daysPerWeek,
                availableEquipment, null, createdAt, updatedAt);
    }
}
