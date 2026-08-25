package com.fitness.service;

import com.fitness.domain.ExerciseFeedback;
import com.fitness.domain.Plan;
import com.fitness.domain.Prescription;
import com.fitness.domain.PrescriptionAdjustment;
import com.fitness.domain.PrescriptionAdjustmentStatus;
import com.fitness.domain.PlanStatus;
import com.fitness.domain.User;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.PlanMapper;
import com.fitness.mapper.PrescriptionAdjustmentMapper;
import com.fitness.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrescriptionAdjustmentServiceTest {
    private final PrescriptionAdjustmentMapper mapper = mock(PrescriptionAdjustmentMapper.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final PlanMapper planMapper = mock(PlanMapper.class);
    private final PrescriptionAdjustmentService service = new PrescriptionAdjustmentService(mapper, userMapper, planMapper);

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

    @Test
    void listsOnlyTheCurrentUsersActivePlanAdjustments() {
        when(userMapper.findUserByUsername("demo")).thenReturn(user());
        when(mapper.findOwnedByActivePlan("user-id")).thenReturn(List.of(adjustment()));

        var result = service.list("demo");

        assertThat(result).singleElement().satisfies(value -> {
            assertThat(value.adjustmentId()).isEqualTo("adjustment-id");
            assertThat(value.status()).isEqualTo(PrescriptionAdjustmentStatus.PENDING);
        });
    }

    @Test
    void returnsNotFoundForMissingOrUnownedAdjustment() {
        when(userMapper.findUserByUsername("demo")).thenReturn(user());

        assertThatThrownBy(() -> service.accept("demo", "missing", 3))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.PRESCRIPTION_ADJUSTMENT_NOT_FOUND);
    }

    @Test
    void acceptsPendingAdjustmentAndBumpsThePlanVersion() {
        PrescriptionAdjustment adjustment = adjustment();
        when(userMapper.findUserByUsername("demo")).thenReturn(user());
        when(mapper.findOwnedById("adjustment-id", "user-id")).thenReturn(adjustment);
        when(mapper.applySuggestedPrescription(adjustment, 3)).thenReturn(1);
        when(planMapper.bumpPlanVersion("plan-id", "user-id", PlanStatus.ACTIVE, 3)).thenReturn(1);

        var result = service.accept("demo", "adjustment-id", 3);

        assertThat(result.status()).isEqualTo(PrescriptionAdjustmentStatus.ACCEPTED);
        verify(mapper).resolvePending(
                org.mockito.ArgumentMatchers.eq("adjustment-id"),
                org.mockito.ArgumentMatchers.eq(PrescriptionAdjustmentStatus.ACCEPTED), any(LocalDateTime.class));
    }

    @Test
    void declinesWithoutChangingTheTargetPrescription() {
        PrescriptionAdjustment adjustment = adjustment();
        when(userMapper.findUserByUsername("demo")).thenReturn(user());
        when(mapper.findOwnedById("adjustment-id", "user-id")).thenReturn(adjustment);

        var result = service.decline("demo", "adjustment-id", 3);

        assertThat(result.status()).isEqualTo(PrescriptionAdjustmentStatus.DECLINED);
        verify(mapper, never()).applySuggestedPrescription(any(), any(Integer.class));
        verify(planMapper, never()).bumpPlanVersion(any(), any(), any(), any(Integer.class));
    }

    @Test
    void expiresInsteadOfModifyingAStartedOrReplacedTarget() {
        PrescriptionAdjustment adjustment = adjustment();
        when(userMapper.findUserByUsername("demo")).thenReturn(user());
        when(mapper.findOwnedById("adjustment-id", "user-id")).thenReturn(adjustment);
        when(mapper.applySuggestedPrescription(adjustment, 3)).thenReturn(0);

        var result = service.accept("demo", "adjustment-id", 3);

        assertThat(result.status()).isEqualTo(PrescriptionAdjustmentStatus.EXPIRED);
        verify(planMapper, never()).bumpPlanVersion(any(), any(), any(), any(Integer.class));
    }

    @Test
    void returnsTheSavedResultForADuplicateRequestWithoutApplyingAgain() {
        PrescriptionAdjustment adjustment = adjustment();
        adjustment.setStatus(PrescriptionAdjustmentStatus.ACCEPTED);
        when(userMapper.findUserByUsername("demo")).thenReturn(user());
        when(mapper.findOwnedById("adjustment-id", "user-id")).thenReturn(adjustment);

        var result = service.accept("demo", "adjustment-id", 3);

        assertThat(result.status()).isEqualTo(PrescriptionAdjustmentStatus.ACCEPTED);
        verify(mapper, never()).applySuggestedPrescription(any(), any(Integer.class));
        verify(mapper, never()).resolvePending(any(), any(), any(LocalDateTime.class));
    }

    @Test
    void rollsBackThePrescriptionChangeWhenThePlanVersionIsContended() {
        PrescriptionAdjustment adjustment = adjustment();
        when(userMapper.findUserByUsername("demo")).thenReturn(user());
        when(mapper.findOwnedById("adjustment-id", "user-id")).thenReturn(adjustment);
        when(mapper.applySuggestedPrescription(adjustment, 3)).thenReturn(1);
        when(planMapper.bumpPlanVersion("plan-id", "user-id", PlanStatus.ACTIVE, 3)).thenReturn(0);

        assertThatThrownBy(() -> service.accept("demo", "adjustment-id", 3))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.PLAN_CONFLICT);
        verify(mapper, never()).resolvePending(any(), any(), any(LocalDateTime.class));
    }

    private Plan plan() {
        Plan plan = new Plan();
        plan.setId("33333333-3333-3333-3333-333333333333");
        return plan;
    }

    private User user() {
        User user = new User();
        user.setId("user-id");
        return user;
    }

    private PrescriptionAdjustment adjustment() {
        PrescriptionAdjustment value = new PrescriptionAdjustment();
        value.setId("adjustment-id");
        value.setPlanId("plan-id");
        value.setSourceWorkoutId("source-workout-id");
        value.setSourceExerciseId("core");
        value.setFirstFeedbackId("first-feedback-id");
        value.setSecondFeedbackId("second-feedback-id");
        value.setTargetWorkoutId("target-workout-id");
        value.setTargetPrescriptionId("target-prescription-id");
        value.setOriginalPrescription(Map.of("exerciseId", "core", "sets", 3, "reps", 10, "rpe", 7.0));
        value.setSuggestedPrescription(Map.of("exerciseId", "core", "sets", 3, "reps", 10, "rpe", 7.5));
        value.setStatus(PrescriptionAdjustmentStatus.PENDING);
        value.setCreatedAt(LocalDateTime.now());
        return value;
    }

    private ExerciseFeedback feedback(String id, String type) {
        ExerciseFeedback feedback = new ExerciseFeedback();
        feedback.setId(id);
        feedback.setWorkoutId("44444444-4444-4444-4444-444444444444");
        feedback.setExerciseId("core");
        feedback.setFeedbackType(type);
        feedback.setCreatedAt(LocalDateTime.now());
        return feedback;
    }

    private Prescription target() {
        Prescription prescription = new Prescription();
        prescription.setId("55555555-5555-5555-5555-555555555555");
        prescription.setWorkoutId("66666666-6666-6666-6666-666666666666");
        prescription.setExerciseId("core");
        prescription.setSets(3);
        prescription.setReps(10);
        prescription.setRpe(new BigDecimal("7.0"));
        return prescription;
    }
}
