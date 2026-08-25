package com.fitness.service;

import com.fitness.domain.ExerciseFeedback;
import com.fitness.domain.Plan;
import com.fitness.domain.Prescription;
import com.fitness.domain.PrescriptionAdjustment;
import com.fitness.domain.PrescriptionAdjustmentStatus;
import com.fitness.mapper.PrescriptionAdjustmentMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrescriptionAdjustmentServiceTest {
    private final PrescriptionAdjustmentMapper mapper = mock(PrescriptionAdjustmentMapper.class);
    private final PrescriptionAdjustmentService service = new PrescriptionAdjustmentService(mapper);

    @Test
    void createsOnePendingRpeAdjustmentForTwoUnconsumedMatchingFeedbacks() {
        Plan plan = plan(); ExerciseFeedback newest = feedback("second", "TOO_EASY");
        when(mapper.findRecentUnconsumedFeedbacks(plan.getId(), "core"))
                .thenReturn(List.of(newest, feedback("first", "TOO_EASY")));
        when(mapper.findNextUnstartedPrescription(plan.getId(), "core", 2)).thenReturn(target());

        service.createCandidateIfTriggered(plan, newest, 2);

        verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(adjustment ->
                adjustment.getStatus() == PrescriptionAdjustmentStatus.PENDING
                        && adjustment.getFirstFeedbackId().equals("first")
                        && adjustment.getSecondFeedbackId().equals("second")
                        && new BigDecimal("7.5").compareTo((BigDecimal) adjustment.getSuggestedPrescription().get("rpe")) == 0
                        && adjustment.getReason().contains("偏轻松") && adjustment.getReasonEn().contains("too easy")));
    }

    @Test
    void ignoresMixedJustRightAndAlreadyConsumedFeedbackWindows() {
        Plan plan = plan(); ExerciseFeedback newest = feedback("second", "TOO_HARD");
        when(mapper.findRecentUnconsumedFeedbacks(plan.getId(), "core"))
                .thenReturn(List.of(newest, feedback("first", "JUST_RIGHT")));

        service.createCandidateIfTriggered(plan, newest, 2);

        verify(mapper, never()).findNextUnstartedPrescription(any(), any(), any(Integer.class));
        verify(mapper, never()).insert(any(PrescriptionAdjustment.class));
    }

    @Test
    void doesNotCreateAnotherCandidateWhenTheMapperExcludesAnAlreadyConsumedFeedback() {
        Plan plan = plan(); ExerciseFeedback newest = feedback("new-feedback", "TOO_EASY");
        when(mapper.findRecentUnconsumedFeedbacks(plan.getId(), "core")).thenReturn(List.of(newest));

        service.createCandidateIfTriggered(plan, newest, 2);

        verify(mapper, never()).findNextUnstartedPrescription(any(), any(), any(Integer.class));
        verify(mapper, never()).insert(any(PrescriptionAdjustment.class));
    }

    @Test
    void consumesMatchingWindowAsExpiredWhenNoNextUnstartedTargetExists() {
        Plan plan = plan(); ExerciseFeedback newest = feedback("second", "TOO_HARD");
        when(mapper.findRecentUnconsumedFeedbacks(plan.getId(), "core"))
                .thenReturn(List.of(newest, feedback("first", "TOO_HARD")));
        when(mapper.findNextUnstartedPrescription(plan.getId(), "core", 2)).thenReturn(null);

        service.createCandidateIfTriggered(plan, newest, 2);

        verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(adjustment ->
                adjustment.getStatus() == PrescriptionAdjustmentStatus.EXPIRED
                        && adjustment.getProcessedAt() != null
                        && adjustment.getReasonEn().contains("no next unstarted workout")));
    }

    @Test
    void usesAnAllowedSubstituteForTooHardFeedbackWithoutChangingAnotherPrescriptionDimension() {
        Plan plan = plan(); ExerciseFeedback newest = feedback("second", "TOO_HARD");
        when(mapper.findRecentUnconsumedFeedbacks(plan.getId(), "core"))
                .thenReturn(List.of(newest, feedback("first", "TOO_HARD")));
        when(mapper.findNextUnstartedPrescription(plan.getId(), "core", 2)).thenReturn(target());
        when(mapper.findAllowedHardFeedbackSubstitute(plan.getId(), "66666666-6666-6666-6666-666666666666", "core"))
                .thenReturn("safer-core");

        service.createCandidateIfTriggered(plan, newest, 2);

        verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(adjustment ->
                "safer-core".equals(adjustment.getSuggestedExerciseId())
                        && "safer-core".equals(adjustment.getSuggestedPrescription().get("exerciseId"))
                        && new BigDecimal("7.0").compareTo((BigDecimal) adjustment.getSuggestedPrescription().get("rpe")) == 0));
    }

    @Test
    void boundsBeginnerRpeAdjustmentAtThePlanSafetyLimit() {
        Plan plan = plan(); plan.setProfileSnapshot(Map.of("fitnessLevel", "BEGINNER"));
        ExerciseFeedback newest = feedback("second", "TOO_EASY"); Prescription highRpeTarget = target(); highRpeTarget.setRpe(new BigDecimal("7.5"));
        when(mapper.findRecentUnconsumedFeedbacks(plan.getId(), "core"))
                .thenReturn(List.of(newest, feedback("first", "TOO_EASY")));
        when(mapper.findNextUnstartedPrescription(plan.getId(), "core", 2)).thenReturn(highRpeTarget);

        service.createCandidateIfTriggered(plan, newest, 2);

        verify(mapper).insert(org.mockito.ArgumentMatchers.argThat(adjustment ->
                new BigDecimal("7.5").compareTo((BigDecimal) adjustment.getSuggestedPrescription().get("rpe")) == 0));
    }

    private Plan plan() { Plan plan = new Plan(); plan.setId("33333333-3333-3333-3333-333333333333"); return plan; }
    private ExerciseFeedback feedback(String id, String type) { ExerciseFeedback feedback = new ExerciseFeedback(); feedback.setId(id); feedback.setWorkoutId("44444444-4444-4444-4444-444444444444"); feedback.setExerciseId("core"); feedback.setFeedbackType(type); feedback.setCreatedAt(LocalDateTime.now()); return feedback; }
    private Prescription target() { Prescription p = new Prescription(); p.setId("55555555-5555-5555-5555-555555555555"); p.setWorkoutId("66666666-6666-6666-6666-666666666666"); p.setExerciseId("core"); p.setSets(3); p.setReps(10); p.setRpe(new BigDecimal("7.0")); return p; }
}
