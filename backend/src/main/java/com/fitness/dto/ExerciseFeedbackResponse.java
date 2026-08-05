package com.fitness.dto;

import com.fitness.domain.FeedbackType;

import java.time.LocalDate;

public record ExerciseFeedbackResponse(
        String feedbackId,
        FeedbackType feedbackType,
        String hurtBodyPart,
        LocalDate filterUntil,
        boolean substituted,
        boolean removedForSafety,
        String replacementExerciseId,
        TodayWorkoutResponse workout
) {
}
