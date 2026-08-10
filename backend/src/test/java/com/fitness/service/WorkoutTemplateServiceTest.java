package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;
import com.fitness.domain.LoadType;
import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.Prescription;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutStatus;
import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
import com.fitness.domain.WorkoutTemplateStatus;
import com.fitness.domain.UserProfile;
import com.fitness.dto.CreateWorkoutTemplateRequest;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.WorkoutMapper;
import com.fitness.mapper.WorkoutTemplateMapper;
import com.fitness.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkoutTemplateServiceTest {
    private CurrentUserProvider currentUserProvider;
    private UserMapper userMapper;
    private WorkoutMapper workoutMapper;
    private WorkoutTemplateMapper templateMapper;
    private WorkoutTemplateService service;

    @BeforeEach
    void setUp() {
        currentUserProvider = mock(CurrentUserProvider.class);
        userMapper = mock(UserMapper.class);
        workoutMapper = mock(WorkoutMapper.class);
        templateMapper = mock(WorkoutTemplateMapper.class);
        service = new WorkoutTemplateService(currentUserProvider, userMapper, workoutMapper, templateMapper);
        when(currentUserProvider.requireUserId()).thenReturn("user-id");
        when(userMapper.findProfileByUserId("user-id")).thenReturn(null);
    }

    @Test
    void createsTemplateByCopyingOnDemandDraftPrescriptionSnapshot() {
        Workout workout = workout(WorkoutStatus.DRAFT);
        Prescription prescription = prescription();
        when(userMapper.findProfileByUserId("user-id")).thenReturn(profile(FitnessLevel.BEGINNER));
        when(workoutMapper.findOwnedById("workout-id", "user-id")).thenReturn(workout);
        when(workoutMapper.findPrescriptionsByWorkoutId("workout-id")).thenReturn(List.of(prescription));

        WorkoutTemplateResponse response = service.create(new CreateWorkoutTemplateRequest(
                "workout-id", "Chest builder"));

        ArgumentCaptor<WorkoutTemplate> templateCaptor = ArgumentCaptor.forClass(WorkoutTemplate.class);
        ArgumentCaptor<WorkoutTemplateExercise> exerciseCaptor = ArgumentCaptor.forClass(WorkoutTemplateExercise.class);
        verify(templateMapper).insertTemplate(templateCaptor.capture());
        verify(templateMapper).insertTemplateExercise(exerciseCaptor.capture());

        WorkoutTemplate savedTemplate = templateCaptor.getValue();
        assertThat(savedTemplate.getOwnerUserId()).isEqualTo("user-id");
        assertThat(savedTemplate.getSourceWorkoutId()).isEqualTo("workout-id");
        assertThat(savedTemplate.getName()).isEqualTo("Chest builder");
        assertThat(savedTemplate.getBodyPart()).isEqualTo(OnDemandBodyPart.CHEST);
        assertThat(savedTemplate.getEquipmentSnapshot()).containsExactly("body weight", "dumbbell");
        assertThat(savedTemplate.getProfileSnapshot()).containsEntry("fitnessLevel", "BEGINNER");
        assertThat(savedTemplate.getStatus()).isEqualTo(WorkoutTemplateStatus.ACTIVE);

        WorkoutTemplateExercise savedExercise = exerciseCaptor.getValue();
        assertThat(savedExercise.getExerciseId()).isEqualTo("push-up");
        assertThat(savedExercise.getSequence()).isEqualTo(1);
        assertThat(savedExercise.getSets()).isEqualTo(3);
        assertThat(savedExercise.getReps()).isEqualTo(12);
        assertThat(savedExercise.getLoadType()).isEqualTo(LoadType.BODYWEIGHT);
        assertThat(savedExercise.getRpe()).isEqualByComparingTo("7.5");

        assertThat(response.templateId()).isEqualTo(savedTemplate.getId());
        assertThat(response.exercises()).hasSize(1);
        assertThat(response.exercises().getFirst().exercise().name()).isEqualTo("Push-Up");
    }

    @Test
    void rejectsMissingSourceWorkout() {
        when(workoutMapper.findOwnedById("missing", "user-id")).thenReturn(null);

        assertThatThrownBy(() -> service.create(new CreateWorkoutTemplateRequest("missing", "Template")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ON_DEMAND_WORKOUT_NOT_FOUND));
        verify(templateMapper, never()).insertTemplate(any());
    }

    @Test
    void rejectsAlreadyStartedWorkout() {
        when(workoutMapper.findOwnedById("workout-id", "user-id"))
                .thenReturn(workout(WorkoutStatus.IN_PROGRESS));

        assertThatThrownBy(() -> service.create(new CreateWorkoutTemplateRequest("workout-id", "Template")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_STATE_CONFLICT));
        verify(templateMapper, never()).insertTemplate(any());
    }

    @Test
    void listsOwnedTemplatesWithLiveExerciseDetails() {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setId("template-id");
        template.setOwnerUserId("user-id");
        template.setSourceWorkoutId("workout-id");
        template.setName("Saved chest");
        template.setBodyPart(OnDemandBodyPart.CHEST);
        template.setEquipmentSnapshot(List.of("body weight"));
        template.setStatus(WorkoutTemplateStatus.ACTIVE);
        when(templateMapper.findOwnedByUserId("user-id")).thenReturn(List.of(template));
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(templateExercise()));

        List<WorkoutTemplateResponse> responses = service.list();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().templateId()).isEqualTo("template-id");
        assertThat(responses.getFirst().exercises().getFirst().exercise().id()).isEqualTo("push-up");
    }

    @Test
    void reportsProfileChangeWithoutRecalculatingSavedPrescription() {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setId("template-id");
        template.setOwnerUserId("user-id");
        template.setSourceWorkoutId("workout-id");
        template.setName("Saved chest");
        template.setBodyPart(OnDemandBodyPart.CHEST);
        template.setEquipmentSnapshot(List.of("body weight"));
        template.setProfileSnapshot(Map.of(
                "profileId", "profile-id",
                "fitnessLevel", "BEGINNER",
                "goal", "MUSCLE_GAIN",
                "daysPerWeek", 3,
                "availableEquipment", List.of("body weight")));
        template.setStatus(WorkoutTemplateStatus.ACTIVE);
        when(userMapper.findProfileByUserId("user-id")).thenReturn(profile(FitnessLevel.ADVANCED));
        when(templateMapper.findOwnedByUserId("user-id")).thenReturn(List.of(template));
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(templateExercise()));

        WorkoutTemplateResponse response = service.list().getFirst();

        assertThat(response.profileChanged()).isTrue();
        assertThat(response.exercises().getFirst())
                .extracting("sets", "reps", "rpe")
                .containsExactly(3, 12, new BigDecimal("7.5"));
    }

    @Test
    void deletesOwnedTemplate() {
        when(templateMapper.deleteOwnedById("template-id", "user-id")).thenReturn(1);

        service.delete("template-id");

        verify(templateMapper).deleteOwnedById("template-id", "user-id");
    }

    @Test
    void deleteHidesMissingOrForeignTemplateAsNotFound() {
        when(templateMapper.deleteOwnedById("template-id", "user-id")).thenReturn(0);

        assertThatThrownBy(() -> service.delete("template-id"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND));
    }

    private Workout workout(WorkoutStatus status) {
        Workout workout = new Workout();
        workout.setId("workout-id");
        workout.setOwnerUserId("user-id");
        workout.setRequestedBodyPart(OnDemandBodyPart.CHEST);
        workout.setEquipmentSnapshot(List.of("body weight", "dumbbell"));
        workout.setStatus(status);
        return workout;
    }

    private Prescription prescription() {
        Prescription prescription = new Prescription();
        prescription.setId("prescription-id");
        prescription.setWorkoutId("workout-id");
        prescription.setExerciseId("push-up");
        prescription.setSequence(1);
        prescription.setSets(3);
        prescription.setReps(12);
        prescription.setLoadType(LoadType.BODYWEIGHT);
        prescription.setRpe(new BigDecimal("7.5"));
        prescription.setExercise(exercise());
        return prescription;
    }

    private WorkoutTemplateExercise templateExercise() {
        WorkoutTemplateExercise item = new WorkoutTemplateExercise();
        item.setId("template-exercise-id");
        item.setTemplateId("template-id");
        item.setExerciseId("push-up");
        item.setSequence(1);
        item.setSets(3);
        item.setReps(12);
        item.setLoadType(LoadType.BODYWEIGHT);
        item.setRpe(new BigDecimal("7.5"));
        item.setExercise(exercise());
        return item;
    }

    private Exercise exercise() {
        Exercise exercise = new Exercise();
        exercise.setId("push-up");
        exercise.setName("Push-Up");
        exercise.setBodyPart("chest");
        exercise.setTarget("pectorals");
        exercise.setEquipment("body weight");
        return exercise;
    }

    private UserProfile profile(FitnessLevel fitnessLevel) {
        UserProfile profile = new UserProfile();
        profile.setId("profile-id");
        profile.setUserId("user-id");
        profile.setFitnessLevel(fitnessLevel);
        profile.setGoal(Goal.MUSCLE_GAIN);
        profile.setDaysPerWeek(3);
        profile.setAvailableEquipment(List.of("body weight"));
        return profile;
    }
}
