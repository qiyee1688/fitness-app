package com.fitness.dto;

import com.fitness.domain.FeedbackType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record SubmitExerciseFeedbackRequest(
        @NotNull FeedbackType feedbackType,
        String hurtBodyPart
) {
    @AssertTrue(message = "hurtBodyPart is required only for HURT feedback")
    public boolean isHurtBodyPartValid() {
        boolean present = hurtBodyPart != null && !hurtBodyPart.isBlank();
        return feedbackType == FeedbackType.HURT ? present : !present;
    }
}
