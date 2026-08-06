package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.FitnessLevel;
import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutStatus;
import com.fitness.dto.GenerateOnDemandWorkoutRequest;
import com.fitness.dto.OnDemandWorkoutResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.mapper.WorkoutMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnDemandWorkoutServiceTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-06T08:00:00Z"), ZoneOffset.UTC);

    private CurrentUserProvider currentUserProvider;
    private UserMapper userMapper;
    private ExerciseMapper exerciseMapper;
    private WorkoutMapper workoutMapper;
    private OnDemandWorkoutService service;

    @BeforeEach
    void setUp() {
        currentUserProvider = mock(CurrentUserProvider.class);
        userMapper = mock(UserMapper.class);
        exerciseMapper = mock(ExerciseMapper.class);
        workoutMapper = mock(WorkoutMapper.class);
        service = new OnDemandWorkoutService(
                currentUserProvider, userMapper, exerciseMapper, workoutMapper, CLOCK);
        when(currentUserProvider.requireUserId()).thenReturn("user-id");
    }

    @ParameterizedTest
    @EnumSource(OnDemandBodyPart.class)
    void generatesRequiredExerciseCountForEveryBodyPart(OnDemandBodyPart bodyPart) {
        UserProfile profile = profile(FitnessLevel.BEGINNER);
        when(userMapper.findProfileByUserId("user-id")).thenReturn(profile);
        when(exerciseMapper.findOnDemandCandidates(any(), any()))
                .thenReturn(exercises(bodyPart.exerciseCount(), "dumbbell"));

        OnDemandWorkoutResponse response = service.generate(
                new GenerateOnDemandWorkoutRequest(bodyPart, List.of("dumbbell"), false));

        assertThat(response.prescriptions()).hasSize(bodyPart.exerciseCount());
        assertThat(response.status()).isEqualTo(WorkoutStatus.DRAFT);
        assertThat(response.equipment()).containsExactly("body weight", "dumbbell");
        assertThat(response.expiresAt()).isEqualTo(LocalDateTime.of(2026, 8, 7, 8, 0));
        verify(workoutMapper).insertWorkout(any(Workout.class));
    }

    @ParameterizedTest
    @EnumSource(FitnessLevel.class)
    void appliesPrescriptionForFitnessLevel(FitnessLevel level) {
        when(userMapper.findProfileByUserId("user-id")).thenReturn(profile(level));
        when(exerciseMapper.findOnDemandCandidates(any(), any())).thenReturn(exercises(4, "dumbbell"));

        var prescription = service.generate(new GenerateOnDemandWorkoutRequest(
                OnDemandBodyPart.CHEST, List.of(), false)).prescriptions().getFirst();

        switch (level) {
            case BEGINNER -> assertThat(prescription).extracting("sets", "reps", "rpe")
                    .containsExactly(3, 12, new java.math.BigDecimal("7.5"));
            case INTERMEDIATE -> assertThat(prescription).extracting("sets", "reps", "rpe")
                    .containsExactly(4, 10, new java.math.BigDecimal("8.0"));
            case ADVANCED -> assertThat(prescription).extracting("sets", "reps", "rpe")
                    .containsExactly(5, 5, new java.math.BigDecimal("8.5"));
        }
    }

    @Test
    void usesBeginnerAndBodyweightWithoutProfile() {
        when(exerciseMapper.findOnDemandCandidates(any(), any())).thenReturn(exercises(3, "body weight"));

        OnDemandWorkoutResponse response = service.generate(new GenerateOnDemandWorkoutRequest(
                OnDemandBodyPart.WAIST, null, false));

        assertThat(response.equipment()).containsExactly("body weight");
        assertThat(response.prescriptions()).allSatisfy(prescription -> {
            assertThat(prescription.sets()).isEqualTo(3);
            assertThat(prescription.reps()).isEqualTo(12);
        });
    }

    @Test
    void rejectsIncompleteWorkoutBeforePersistence() {
        when(userMapper.findProfileByUserId("user-id")).thenReturn(profile(FitnessLevel.BEGINNER));
        when(exerciseMapper.findOnDemandCandidates(any(), any())).thenReturn(exercises(3, "dumbbell"));

        assertThatThrownBy(() -> service.generate(new GenerateOnDemandWorkoutRequest(
                OnDemandBodyPart.CHEST, List.of("dumbbell"), false)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ON_DEMAND_GENERATION_FAILED));
        verify(workoutMapper, never()).insertWorkout(any());
    }

    @Test
    void startsOwnedDraftAndCompletesInProgressWorkout() {
        Workout workout = workout(WorkoutStatus.DRAFT);
        when(workoutMapper.findOwnedById("workout-id", "user-id")).thenReturn(workout);
        when(workoutMapper.transitionStatus(any(), any(), any(), any(), any())).thenReturn(1);
        when(workoutMapper.findPrescriptionsByWorkoutId("workout-id")).thenReturn(List.of());

        OnDemandWorkoutResponse started = service.start("workout-id");
        workout.setStatus(WorkoutStatus.IN_PROGRESS);
        OnDemandWorkoutResponse completed = service.complete("workout-id");

        assertThat(started.status()).isEqualTo(WorkoutStatus.IN_PROGRESS);
        assertThat(completed.status()).isEqualTo(WorkoutStatus.COMPLETED);
        assertThat(completed.completedAt()).isNotNull();
    }

    @Test
    void rejectsConcurrentStateChange() {
        when(workoutMapper.findOwnedById("workout-id", "user-id"))
                .thenReturn(workout(WorkoutStatus.DRAFT));

        assertThatThrownBy(() -> service.start("workout-id"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_STATE_CONFLICT));
    }

    @Test
    void cleansOnlyDraftsExpiredAtCurrentTime() {
        when(workoutMapper.deleteExpiredDrafts(LocalDateTime.of(2026, 8, 6, 8, 0))).thenReturn(2);

        assertThat(service.cleanupExpiredDrafts()).isEqualTo(2);
    }

    private UserProfile profile(FitnessLevel level) {
        UserProfile profile = new UserProfile();
        profile.setUserId("user-id");
        profile.setFitnessLevel(level);
        profile.setAvailableEquipment(List.of("body weight", "dumbbell"));
        return profile;
    }

    private Workout workout(WorkoutStatus status) {
        Workout workout = new Workout();
        workout.setId("workout-id");
        workout.setOwnerUserId("user-id");
        workout.setRequestedBodyPart(OnDemandBodyPart.CHEST);
        workout.setEquipmentSnapshot(List.of("body weight"));
        workout.setSource(com.fitness.domain.WorkoutSource.ON_DEMAND);
        workout.setStatus(status);
        workout.setPrescriptions(List.of());
        return workout;
    }

    private List<Exercise> exercises(int count, String equipment) {
        List<Exercise> exercises = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            Exercise exercise = new Exercise();
            exercise.setId("exercise-" + index);
            exercise.setName("Exercise " + index);
            exercise.setBodyPart("chest");
            exercise.setTarget("target");
            exercise.setEquipment(equipment);
            exercises.add(exercise);
        }
        return exercises;
    }
}
