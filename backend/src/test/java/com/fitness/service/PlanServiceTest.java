package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.ExerciseFeedback;
import com.fitness.domain.FeedbackType;
import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;
import com.fitness.domain.NutritionTiming;
import com.fitness.domain.Plan;
import com.fitness.domain.PlanStatus;
import com.fitness.domain.Prescription;
import com.fitness.domain.TrainingDayFocus;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutSource;
import com.fitness.domain.WorkoutStatus;
import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
import com.fitness.domain.WorkoutTemplateStatus;
import com.fitness.domain.User;
import com.fitness.domain.UserProfile;
import com.fitness.dto.GeneratePlanRequest;
import com.fitness.dto.GeneratedPlanResponse;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.PlanLifecycleResponse;
import com.fitness.dto.ReplaceWorkoutWithTemplateRequest;
import com.fitness.dto.ReplaceWorkoutWithTemplateResponse;
import com.fitness.dto.TodayWorkoutResponse;
import com.fitness.dto.NutritionTipResponse;
import com.fitness.dto.ExerciseFeedbackResponse;
import com.fitness.dto.SubmitExerciseFeedbackRequest;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.mapper.PlanMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.mapper.WorkoutTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private ExerciseMapper exerciseMapper;
    @Mock private PlanMapper planMapper;
    @Mock private WorkoutTemplateMapper templateMapper;
    @Mock private WorkoutTemplateService workoutTemplateService;
    @Mock private NutritionService nutritionService;

    private PlanService planService;

    @BeforeEach
    void setUp() {
        planService = new PlanService(
                userMapper,
                exerciseMapper,
                planMapper,
                new PlanGenerator(),
                templateMapper,
                workoutTemplateService,
                nutritionService);
    }

    @Test
    void generatesPersistsAndSupersedesExistingActivePlan() {
        User user = user();
        UserProfile profile = profile();
        Plan active = new Plan();
        active.setId("33333333-3333-3333-3333-333333333333");
        active.setVersion(2);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(userMapper.findProfileByUserId(user.getId())).thenReturn(profile);
        when(exerciseMapper.findGeneratorCandidates(profile.getAvailableEquipment()))
                .thenReturn(List.of(exercise()));
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.supersedeActive(active.getId(), 2)).thenReturn(1);

        GeneratedPlanResponse response = planService.generatePlan(
                new GeneratePlanRequest("demo", LocalDate.of(2026, 8, 5)));

        assertThat(response.workoutCount()).isEqualTo(16);
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 9, 29));
        verify(planMapper).supersedeActive(active.getId(), 2);
        verify(planMapper).insertPlan(any(Plan.class));
        verify(planMapper, times(16)).insertWorkout(any());
        verify(planMapper, times(80)).insertPrescription(any());
        verify(nutritionService, times(16)).generateForWorkout(
                any(Workout.class), org.mockito.ArgumentMatchers.same(profile));
    }

    @Test
    void futurePlanIsScheduledWithoutSupersedingActivePlan() {
        User user = user();
        UserProfile profile = profile();
        Plan active = activePlan();
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(userMapper.findProfileByUserId(user.getId())).thenReturn(profile);
        when(exerciseMapper.findGeneratorCandidates(profile.getAvailableEquipment()))
                .thenReturn(List.of(exercise()));
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);

        GeneratedPlanResponse response = planService.generatePlan(
                new GeneratePlanRequest("demo", LocalDate.now().plusDays(1)));

        assertThat(response.status()).isEqualTo(PlanStatus.SCHEDULED);
        verify(planMapper, never()).supersedeActive(any(), any(Integer.class));
    }

    @Test
    void pausesActivePlanAfterTwoWeeksWithoutCheckIn() {
        User user = user();
        Plan active = activePlan();
        active.setStatus(PlanStatus.ACTIVE);
        active.setVersion(3);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findLatestCompletedAt(active.getId()))
                .thenReturn(LocalDateTime.of(2026, 8, 1, 9, 0));
        when(planMapper.transitionStatus(
                active.getId(), PlanStatus.ACTIVE, PlanStatus.PAUSED, 3,
                LocalDate.of(2026, 8, 15).atStartOfDay())).thenReturn(1);

        PlanLifecycleResponse response = planService.processLifecycle(
                "demo", LocalDate.of(2026, 8, 15));

        assertThat(response.currentStatus()).isEqualTo(PlanStatus.PAUSED);
        assertThat(response.changed()).isTrue();
    }

    @Test
    void cancelsPausedPlanAfterAnotherTwoWeeks() {
        User user = user();
        Plan paused = activePlan();
        paused.setStatus(PlanStatus.PAUSED);
        paused.setVersion(4);
        paused.setStatusChangedAt(LocalDateTime.of(2026, 8, 1, 0, 0));
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(null);
        when(planMapper.findPausedByUserId(user.getId())).thenReturn(paused);
        when(planMapper.transitionStatus(
                paused.getId(), PlanStatus.PAUSED, PlanStatus.CANCELLED, 4,
                LocalDate.of(2026, 8, 15).atStartOfDay())).thenReturn(1);

        PlanLifecycleResponse response = planService.processLifecycle(
                "demo", LocalDate.of(2026, 8, 15));

        assertThat(response.currentStatus()).isEqualTo(PlanStatus.CANCELLED);
    }

    @Test
    void activatesEarliestScheduledPlanWhenNoCurrentPlanExists() {
        User user = user();
        Plan scheduled = activePlan();
        scheduled.setStatus(PlanStatus.SCHEDULED);
        scheduled.setVersion(1);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(null);
        when(planMapper.findPausedByUserId(user.getId())).thenReturn(null);
        when(planMapper.findNextScheduledByUserId(user.getId(), LocalDate.of(2026, 8, 12)))
                .thenReturn(scheduled);
        when(planMapper.transitionStatus(
                scheduled.getId(), PlanStatus.SCHEDULED, PlanStatus.ACTIVE, 1,
                LocalDate.of(2026, 8, 12).atStartOfDay())).thenReturn(1);

        PlanLifecycleResponse response = planService.processLifecycle(
                "demo", LocalDate.of(2026, 8, 12));

        assertThat(response.currentStatus()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    void completesExpiredPlanAndCreatesChildRenewal() {
        User user = user();
        UserProfile profile = profile();
        Plan active = activePlan();
        active.setStatus(PlanStatus.ACTIVE);
        active.setVersion(2);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.transitionStatus(
                active.getId(), PlanStatus.ACTIVE, PlanStatus.COMPLETED, 2,
                LocalDate.of(2026, 10, 7).atStartOfDay())).thenReturn(1);
        when(userMapper.findProfileByUserId(user.getId())).thenReturn(profile);
        when(exerciseMapper.findGeneratorCandidates(profile.getAvailableEquipment()))
                .thenReturn(List.of(exercise()));

        PlanLifecycleResponse response = planService.processLifecycle(
                "demo", LocalDate.of(2026, 10, 7));

        assertThat(response.currentStatus()).isEqualTo(PlanStatus.COMPLETED);
        assertThat(response.childPlanId()).isNotBlank();
        verify(planMapper).insertPlan(org.mockito.ArgumentMatchers.argThat(plan ->
                active.getId().equals(plan.getParentPlanId())
                        && plan.getStatus() == PlanStatus.ACTIVE));
    }

    @Test
    void reportsLifecycleConflictWhenOptimisticTransitionLosesRace() {
        User user = user();
        Plan active = activePlan();
        active.setStatus(PlanStatus.ACTIVE);
        active.setVersion(5);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findLatestCompletedAt(active.getId())).thenReturn(null);
        when(planMapper.transitionStatus(
                active.getId(), PlanStatus.ACTIVE, PlanStatus.PAUSED, 5,
                LocalDate.of(2026, 9, 1).atStartOfDay())).thenReturn(0);

        assertThatThrownBy(() -> planService.processLifecycle("demo", LocalDate.of(2026, 9, 1)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAN_CONFLICT));
    }

    @Test
    void rejectsConcurrentActivePlanChange() {
        User user = user();
        Plan active = new Plan();
        active.setId("33333333-3333-3333-3333-333333333333");
        active.setVersion(2);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(userMapper.findProfileByUserId(user.getId())).thenReturn(profile());
        when(exerciseMapper.findGeneratorCandidates(any())).thenReturn(List.of(exercise()));
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.supersedeActive(active.getId(), 2)).thenReturn(0);

        assertThatThrownBy(() -> planService.generatePlan(new GeneratePlanRequest("demo", null)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAN_CONFLICT));
    }

    @Test
    void returnsActivePlanWithWorkoutDatesAndExerciseDetails() {
        User user = user();
        Plan active = new Plan();
        active.setId("33333333-3333-3333-3333-333333333333");
        active.setStatus(com.fitness.domain.PlanStatus.ACTIVE);
        active.setStartDate(LocalDate.of(2026, 8, 12));
        active.setEndDate(LocalDate.of(2026, 10, 6));
        active.setProfileSnapshot(Map.of("daysPerWeek", 2));
        Workout workout = new Workout();
        workout.setId("44444444-4444-4444-4444-444444444444");
        workout.setDayNumber(8);
        workout.setFocus(TrainingDayFocus.FULL_BODY);
        Prescription prescription = new Prescription();
        prescription.setId("55555555-5555-5555-5555-555555555555");
        prescription.setWorkoutId(workout.getId());
        prescription.setSequence(1);
        prescription.setSets(3);
        prescription.setReps(10);
        prescription.setRpe(new BigDecimal("7.0"));
        prescription.setExercise(exercise());
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findWorkoutsByPlanId(active.getId())).thenReturn(List.of(workout));
        when(planMapper.findPrescriptionsByPlanId(active.getId())).thenReturn(List.of(prescription));
        when(nutritionService.listByPlanId(active.getId()))
                .thenReturn(Map.of(workout.getId(), List.of(tipResponse(workout.getId()))));

        PlanDetailResponse response = planService.getActivePlan("demo");

        assertThat(response.totalWeeks()).isEqualTo(8);
        assertThat(response.workouts()).singleElement().satisfies(detail -> {
            assertThat(detail.weekNumber()).isEqualTo(2);
            assertThat(detail.scheduledDate()).isEqualTo(LocalDate.of(2026, 8, 19));
            assertThat(detail.prescriptions()).singleElement().satisfies(item ->
                    assertThat(item.exercise().id()).isEqualTo("core"));
            assertThat(detail.nutritionTips()).singleElement()
                    .satisfies(tip -> assertThat(tip.timing()).isEqualTo(NutritionTiming.PRE_WORKOUT));
        });
    }

    @Test
    void reportsWhenUserHasNoActivePlan() {
        User user = user();
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(null);

        assertThatThrownBy(() -> planService.getActivePlan("demo"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACTIVE_PLAN_NOT_FOUND));
    }


    @Test
    void returnsWorkoutScheduledForRequestedDate() {
        User user = user();
        Plan active = activePlan();
        Workout workout = workout(8);
        Prescription prescription = prescription(workout.getId());
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByPlanIdAndDayNumber(active.getId(), 8)).thenReturn(workout);
        when(planMapper.findPrescriptionsByWorkoutId(workout.getId())).thenReturn(List.of(prescription));
        when(nutritionService.listOwnedWorkoutTips(workout.getId()))
                .thenReturn(List.of(tipResponse(workout.getId())));

        TodayWorkoutResponse response = planService.getTodayWorkout(
                "demo", LocalDate.of(2026, 8, 19));

        assertThat(response.dayNumber()).isEqualTo(8);
        assertThat(response.scheduledDate()).isEqualTo(LocalDate.of(2026, 8, 19));
        assertThat(response.prescriptions()).singleElement().satisfies(item ->
                assertThat(item.exercise().id()).isEqualTo("core"));
        assertThat(response.nutritionTips()).singleElement()
                .satisfies(tip -> assertThat(tip.tipId()).isEqualTo("tip-id"));
    }

    @Test
    void reportsRestDayWhenNoWorkoutIsScheduled() {
        User user = user();
        Plan active = activePlan();
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByPlanIdAndDayNumber(active.getId(), 5)).thenReturn(null);

        assertThatThrownBy(() -> planService.getTodayWorkout(
                "demo", LocalDate.of(2026, 8, 16)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TODAY_WORKOUT_NOT_FOUND));
    }

    @Test
    void completesWorkoutWithAtomicUpdate() {
        User user = user();
        Plan active = activePlan();
        Workout workout = workout(1);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(workout.getId(), active.getId())).thenReturn(workout);
        when(planMapper.completeWorkout(org.mockito.ArgumentMatchers.eq(workout.getId()), any(LocalDateTime.class)))
                .thenReturn(1);
        when(planMapper.findPrescriptionsByWorkoutId(workout.getId())).thenReturn(List.of());

        TodayWorkoutResponse response = planService.completeWorkout("demo", workout.getId());

        assertThat(response.completedAt()).isNotNull();
        assertThat(response.alreadyCompleted()).isFalse();
        verify(planMapper).completeWorkout(org.mockito.ArgumentMatchers.eq(workout.getId()), any(LocalDateTime.class));
    }

    @Test
    void returnsExistingCompletionIdempotently() {
        User user = user();
        Plan active = activePlan();
        Workout workout = workout(1);
        workout.setCompletedAt(LocalDateTime.of(2026, 8, 12, 9, 30));
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(workout.getId(), active.getId())).thenReturn(workout);
        when(planMapper.findPrescriptionsByWorkoutId(workout.getId())).thenReturn(List.of());

        TodayWorkoutResponse response = planService.completeWorkout("demo", workout.getId());

        assertThat(response.alreadyCompleted()).isTrue();
        assertThat(response.completedAt()).isEqualTo(LocalDateTime.of(2026, 8, 12, 9, 30));
        verify(planMapper, never()).completeWorkout(org.mockito.ArgumentMatchers.anyString(), any(LocalDateTime.class));
    }

    @Test
    void hurtFeedbackReplacesUnsafeExerciseAndRecordsFourWeekEffect() {
        User user = user();
        Plan active = activePlan();
        Workout workout = workout(1);
        Prescription prescription = prescription(workout.getId());
        Exercise replacement = exercise();
        replacement.setId("replacement");
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(workout.getId(), active.getId())).thenReturn(workout);
        when(planMapper.findPrescriptionInWorkout(workout.getId(), "core")).thenReturn(prescription);
        when(planMapper.findSafeSubstitute(user.getId(), workout.getId(), "core", "waist"))
                .thenReturn(replacement);
        when(planMapper.replacePrescriptionExercise(prescription.getId(), "replacement", "core"))
                .thenReturn(1);
        when(planMapper.findPrescriptionsByWorkoutId(workout.getId())).thenReturn(List.of());

        ExerciseFeedbackResponse response = planService.submitExerciseFeedback(
                "demo", workout.getId(), "core",
                new SubmitExerciseFeedbackRequest(FeedbackType.HURT, "Waist"));

        assertThat(response.substituted()).isTrue();
        assertThat(response.removedForSafety()).isFalse();
        assertThat(response.replacementExerciseId()).isEqualTo("replacement");
        assertThat(response.filterUntil()).isAfterOrEqualTo(LocalDate.now().plusWeeks(4));
        verify(planMapper).insertExerciseFeedback(any(ExerciseFeedback.class));
    }

    @Test
    void hurtFeedbackRemovesPrescriptionWhenNoSafeSubstituteExists() {
        User user = user();
        Plan active = activePlan();
        Workout workout = workout(1);
        Prescription prescription = prescription(workout.getId());
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findActiveByUserId(user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(workout.getId(), active.getId())).thenReturn(workout);
        when(planMapper.findPrescriptionInWorkout(workout.getId(), "core")).thenReturn(prescription);
        when(planMapper.findSafeSubstitute(user.getId(), workout.getId(), "core", "waist"))
                .thenReturn(null);
        when(planMapper.removePrescriptionForSafety(
                org.mockito.ArgumentMatchers.eq(prescription.getId()),
                org.mockito.ArgumentMatchers.eq("core"), any(LocalDateTime.class))).thenReturn(1);
        when(planMapper.findPrescriptionsByWorkoutId(workout.getId())).thenReturn(List.of());

        ExerciseFeedbackResponse response = planService.submitExerciseFeedback(
                "demo", workout.getId(), "core",
                new SubmitExerciseFeedbackRequest(FeedbackType.HURT, "waist"));

        assertThat(response.substituted()).isFalse();
        assertThat(response.removedForSafety()).isTrue();
    }

    @Test
    void replacesFuturePlanWorkoutWithTemplateSnapshotAndOptimisticLock() {
        User user = user();
        Plan active = activePlan();
        active.setStatus(PlanStatus.ACTIVE);
        active.setVersion(7);
        Workout original = workout(8);
        original.setStatus(WorkoutStatus.READY);
        WorkoutTemplate template = workoutTemplate();
        WorkoutTemplateExercise templateExercise = templateExercise();
        Prescription replacementPrescription = prescription("replacement-workout");
        UserProfile profile = profile();
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findOwnedPlanById(active.getId(), user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(original.getId(), active.getId())).thenReturn(original);
        when(templateMapper.findOwnedById(template.getId(), user.getId())).thenReturn(template);
        when(templateMapper.findExercisesByTemplateId(template.getId())).thenReturn(List.of(templateExercise));
        when(userMapper.findProfileByUserId(user.getId())).thenReturn(profile);
        when(planMapper.bumpPlanVersion(active.getId(), user.getId(), PlanStatus.ACTIVE, 7)).thenReturn(1);
        when(planMapper.markWorkoutReplaced(original.getId(), active.getId())).thenReturn(1);
        when(nutritionService.generateForWorkout(any(Workout.class), org.mockito.ArgumentMatchers.same(profile)))
                .thenReturn(List.of(tipResponse("replacement-workout")));
        when(planMapper.findPrescriptionsByWorkoutId(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(List.of(replacementPrescription));

        ReplaceWorkoutWithTemplateResponse response = planService.replaceWorkoutWithTemplate(
                "demo",
                active.getId(),
                original.getId(),
                new ReplaceWorkoutWithTemplateRequest(template.getId(), 7),
                LocalDate.of(2026, 8, 12));

        assertThat(response.originalWorkoutId()).isEqualTo(original.getId());
        assertThat(response.replacementWorkoutId()).isNotBlank();
        assertThat(response.dayNumber()).isEqualTo(8);
        assertThat(response.workout().status()).isEqualTo(WorkoutStatus.READY);
        assertThat(response.workout().nutritionTips()).hasSize(1);
        verify(planMapper).bumpPlanVersion(active.getId(), user.getId(), PlanStatus.ACTIVE, 7);
        verify(planMapper).markWorkoutReplaced(original.getId(), active.getId());
        verify(nutritionService).generateForWorkout(any(Workout.class), org.mockito.ArgumentMatchers.same(profile));
        verify(planMapper).insertWorkout(org.mockito.ArgumentMatchers.argThat(workout ->
                workout.getSource() == WorkoutSource.TEMPLATE_REPLACEMENT
                        && workout.getStatus() == WorkoutStatus.READY
                        && workout.getReplacedWorkoutId().equals(original.getId())
                        && workout.getDayNumber() == original.getDayNumber()
                        && workout.getRequestedBodyPart() == template.getBodyPart()
                        && workout.getEquipmentSnapshot().equals(template.getEquipmentSnapshot())));
        verify(planMapper).insertPrescription(org.mockito.ArgumentMatchers.argThat(prescription ->
                prescription.getExerciseId().equals(templateExercise.getExerciseId())
                        && prescription.getSets() == templateExercise.getSets()
                        && prescription.getReps() == templateExercise.getReps()));
    }

    @Test
    void rejectsReplacingPastOrCompletedWorkout() {
        User user = user();
        Plan active = activePlan();
        active.setStatus(PlanStatus.ACTIVE);
        Workout original = workout(1);
        original.setStatus(WorkoutStatus.READY);
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findOwnedPlanById(active.getId(), user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(original.getId(), active.getId())).thenReturn(original);

        assertThatThrownBy(() -> planService.replaceWorkoutWithTemplate(
                "demo",
                active.getId(),
                original.getId(),
                new ReplaceWorkoutWithTemplateRequest("template-id", 0),
                LocalDate.of(2026, 8, 20)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAN_WORKOUT_REPLACEMENT_INVALID));
        verify(planMapper, never()).markWorkoutReplaced(any(), any());
    }

    @Test
    void rejectsTemplateReplacementWhenTemplateNeedsRepair() {
        User user = user();
        Plan active = activePlan();
        active.setStatus(PlanStatus.ACTIVE);
        Workout original = workout(8);
        original.setStatus(WorkoutStatus.READY);
        WorkoutTemplate template = workoutTemplate();
        List<WorkoutTemplateExercise> exercises = List.of(templateExercise());
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findOwnedPlanById(active.getId(), user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(original.getId(), active.getId())).thenReturn(original);
        when(templateMapper.findOwnedById(template.getId(), user.getId())).thenReturn(template);
        when(templateMapper.findExercisesByTemplateId(template.getId())).thenReturn(exercises);
        when(userMapper.findProfileByUserId(user.getId())).thenReturn(profile());
        when(workoutTemplateService.requiresRepair(exercises, profile())).thenReturn(true);

        assertThatThrownBy(() -> planService.replaceWorkoutWithTemplate(
                "demo",
                active.getId(),
                original.getId(),
                new ReplaceWorkoutWithTemplateRequest(template.getId(), 0),
                LocalDate.of(2026, 8, 12)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_TEMPLATE_NEEDS_REPAIR));
        verify(planMapper, never()).bumpPlanVersion(any(), any(), any(), any(Integer.class));
    }

    @Test
    void rejectsTemplateReplacementWhenPlanVersionChanged() {
        User user = user();
        Plan active = activePlan();
        active.setStatus(PlanStatus.ACTIVE);
        active.setVersion(3);
        Workout original = workout(8);
        original.setStatus(WorkoutStatus.READY);
        WorkoutTemplate template = workoutTemplate();
        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(planMapper.findOwnedPlanById(active.getId(), user.getId())).thenReturn(active);
        when(planMapper.findWorkoutByIdAndPlanId(original.getId(), active.getId())).thenReturn(original);
        when(templateMapper.findOwnedById(template.getId(), user.getId())).thenReturn(template);
        when(templateMapper.findExercisesByTemplateId(template.getId())).thenReturn(List.of(templateExercise()));
        when(planMapper.bumpPlanVersion(active.getId(), user.getId(), PlanStatus.ACTIVE, 3)).thenReturn(0);

        assertThatThrownBy(() -> planService.replaceWorkoutWithTemplate(
                "demo",
                active.getId(),
                original.getId(),
                new ReplaceWorkoutWithTemplateRequest(template.getId(), 3),
                LocalDate.of(2026, 8, 12)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAN_CONFLICT));
        verify(planMapper, never()).markWorkoutReplaced(any(), any());
    }

    private Plan activePlan() {
        Plan plan = new Plan();
        plan.setId("33333333-3333-3333-3333-333333333333");
        plan.setStartDate(LocalDate.of(2026, 8, 12));
        plan.setEndDate(LocalDate.of(2026, 10, 6));
        return plan;
    }

    private Workout workout(int dayNumber) {
        Workout workout = new Workout();
        workout.setId("44444444-4444-4444-4444-444444444444");
        workout.setDayNumber(dayNumber);
        workout.setFocus(TrainingDayFocus.FULL_BODY);
        return workout;
    }

    private Prescription prescription(String workoutId) {
        Prescription prescription = new Prescription();
        prescription.setId("55555555-5555-5555-5555-555555555555");
        prescription.setWorkoutId(workoutId);
        prescription.setSequence(1);
        prescription.setSets(3);
        prescription.setReps(10);
        prescription.setRpe(new BigDecimal("7.0"));
        prescription.setExercise(exercise());
        return prescription;
    }

    private NutritionTipResponse tipResponse(String workoutId) {
        return new NutritionTipResponse(
                "tip-id", workoutId, NutritionTiming.PRE_WORKOUT, null,
                "训练前补充能量", "Fuel before training", "rule-id", 1, new BigDecimal("60.0"));
    }

    private WorkoutTemplate workoutTemplate() {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setId("77777777-7777-7777-7777-777777777777");
        template.setBodyPart(com.fitness.domain.OnDemandBodyPart.WAIST);
        template.setEquipmentSnapshot(List.of("body weight"));
        template.setStatus(WorkoutTemplateStatus.ACTIVE);
        return template;
    }

    private WorkoutTemplateExercise templateExercise() {
        WorkoutTemplateExercise item = new WorkoutTemplateExercise();
        item.setId("88888888-8888-8888-8888-888888888888");
        item.setExerciseId("core");
        item.setSequence(1);
        item.setSets(4);
        item.setReps(10);
        item.setRpe(new BigDecimal("8.0"));
        item.setLoadType(com.fitness.domain.LoadType.BODYWEIGHT);
        return item;
    }

    private User user() {
        User user = new User();
        user.setId("11111111-1111-1111-1111-111111111111");
        user.setUsername("demo");
        return user;
    }

    private UserProfile profile() {
        UserProfile profile = new UserProfile();
        profile.setId("22222222-2222-2222-2222-222222222222");
        profile.setUserId("11111111-1111-1111-1111-111111111111");
        profile.setFitnessLevel(FitnessLevel.BEGINNER);
        profile.setGoal(Goal.GENERAL_FITNESS);
        profile.setDaysPerWeek(2);
        profile.setAvailableEquipment(List.of("body weight"));
        return profile;
    }

    private Exercise exercise() {
        Exercise exercise = new Exercise();
        exercise.setId("core");
        exercise.setBodyPart("waist");
        exercise.setTarget("abs");
        exercise.setEquipment("body weight");
        return exercise;
    }
}
