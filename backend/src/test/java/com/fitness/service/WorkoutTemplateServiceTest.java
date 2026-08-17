package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.ExerciseSubstituteReason;
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
import com.fitness.dto.UpdateWorkoutTemplateRequest;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.ExerciseMapper;
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
    private ExerciseMapper exerciseMapper;
    private WorkoutTemplateService service;

    @BeforeEach
    void setUp() {
        currentUserProvider = mock(CurrentUserProvider.class);
        userMapper = mock(UserMapper.class);
        workoutMapper = mock(WorkoutMapper.class);
        templateMapper = mock(WorkoutTemplateMapper.class);
        exerciseMapper = mock(ExerciseMapper.class);
        service = new WorkoutTemplateService(
                currentUserProvider, userMapper, workoutMapper, templateMapper, exerciseMapper);
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
    void updatesOwnedTemplateNameOrderAndPrescriptionUsingExpectedVersion() {
        WorkoutTemplate template = template();
        List<WorkoutTemplateExercise> items = List.of(
                templateExercise("item-2", 1), templateExercise("item-1", 2), templateExercise("item-3", 3));
        when(templateMapper.findOwnedById("template-id", "user-id"))
                .thenReturn(template)
                .thenAnswer(invocation -> {
                    template.setName("Edited template");
                    template.setVersion(3);
                    return template;
                });
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(items);
        when(templateMapper.updateOwnedTemplate("template-id", "user-id", 2, "Edited template")).thenReturn(1);
        when(templateMapper.updateTemplateExercisePrescription(any(), any(), any())).thenReturn(1);

        WorkoutTemplateResponse response = service.update("template-id", updateRequest());

        assertThat(response.name()).isEqualTo("Edited template");
        assertThat(response.version()).isEqualTo(3);
        verify(templateMapper).reserveTemplateExerciseSequences("template-id", "user-id");
        verify(templateMapper).deleteTemplateExercisesExcept(
                "template-id", "user-id", List.of("item-2", "item-1", "item-3"));
    }

    @Test
    void rejectsTemplateUpdateWhenExpectedVersionIsStale() {
        when(templateMapper.findOwnedById("template-id", "user-id")).thenReturn(template());
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(
                templateExercise("item-1", 1), templateExercise("item-2", 2), templateExercise("item-3", 3)));
        when(templateMapper.updateOwnedTemplate("template-id", "user-id", 2, "Edited template")).thenReturn(0);

        assertThatThrownBy(() -> service.update("template-id", updateRequest()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_TEMPLATE_CONFLICT));
        verify(templateMapper, never()).updateTemplateExercisePrescription(any(), any(), any());
    }

    @Test
    void rejectsChestTemplateEditBelowMinimumExerciseCount() {
        when(templateMapper.findOwnedById("template-id", "user-id")).thenReturn(template());
        UpdateWorkoutTemplateRequest request = new UpdateWorkoutTemplateRequest(
                2, "Edited template", List.of(updateItem("item-1", 1), updateItem("item-2", 2)));

        assertThatThrownBy(() -> service.update("template-id", request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_TEMPLATE_EDIT_INVALID));
        verify(templateMapper, never()).updateOwnedTemplate(any(), any(), any(Integer.class), any());
    }

    @Test
    void deletesOmittedExerciseWhenTemplateStillMeetsMinimumCount() {
        WorkoutTemplate template = template();
        List<WorkoutTemplateExercise> existing = List.of(
                templateExercise("item-1", 1), templateExercise("item-2", 2),
                templateExercise("item-3", 3), templateExercise("item-4", 4));
        List<WorkoutTemplateExercise> refreshed = List.of(
                templateExercise("item-2", 1), templateExercise("item-3", 2), templateExercise("item-4", 3));
        when(templateMapper.findOwnedById("template-id", "user-id"))
                .thenReturn(template)
                .thenAnswer(invocation -> { template.setVersion(3); return template; });
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(existing).thenReturn(refreshed);
        when(templateMapper.updateOwnedTemplate("template-id", "user-id", 2, "Edited template")).thenReturn(1);
        when(templateMapper.updateTemplateExercisePrescription(any(), any(), any())).thenReturn(1);
        UpdateWorkoutTemplateRequest request = new UpdateWorkoutTemplateRequest(
                2, "Edited template", List.of(
                updateItem("item-2", 1), updateItem("item-3", 2), updateItem("item-4", 3)));

        WorkoutTemplateResponse response = service.update("template-id", request);

        assertThat(response.exercises()).extracting("prescriptionId")
                .containsExactly("item-2", "item-3", "item-4");
    }

    @Test
    void listsOnlySystemSubstitutesCompatibleWithTemplateEquipment() {
        WorkoutTemplateExercise item = templateExercise("item-1", 1);
        UserProfile currentProfile = profile(FitnessLevel.BEGINNER);
        currentProfile.setAvailableEquipment(List.of("dumbbell"));
        when(userMapper.findProfileByUserId("user-id")).thenReturn(currentProfile);
        when(templateMapper.findOwnedById("template-id", "user-id")).thenReturn(template());
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(item));
        when(exerciseMapper.findTemplateSubstitutes(
                "push-up", List.of("body weight", "dumbbell"), ExerciseSubstituteReason.EQUIPMENT_SWAP))
                .thenReturn(List.of(exercise("incline-push-up", "body weight")));

        assertThat(service.listSubstitutes("template-id", "item-1"))
                .extracting(Exercise::getId).containsExactly("incline-push-up");
    }

    @Test
    void replacesExerciseOnlyWithSystemProvidedSubstitute() {
        WorkoutTemplate template = template();
        List<WorkoutTemplateExercise> existing = List.of(
                templateExercise("item-1", 1), templateExercise("item-2", 2), templateExercise("item-3", 3));
        WorkoutTemplateExercise replacement = templateExercise("item-1", "incline-push-up", 1);
        when(templateMapper.findOwnedById("template-id", "user-id"))
                .thenReturn(template)
                .thenAnswer(invocation -> { template.setVersion(3); return template; });
        when(templateMapper.findExercisesByTemplateId("template-id"))
                .thenReturn(existing)
                .thenReturn(List.of(replacement, templateExercise("item-2", 2), templateExercise("item-3", 3)));
        when(exerciseMapper.findTemplateSubstitutes(
                "push-up", List.of("body weight"), ExerciseSubstituteReason.EQUIPMENT_SWAP))
                .thenReturn(List.of(exercise("incline-push-up", "body weight")));
        when(templateMapper.updateOwnedTemplate("template-id", "user-id", 2, "Edited template")).thenReturn(1);
        when(templateMapper.updateTemplateExercisePrescription(any(), any(), any())).thenReturn(1);
        UpdateWorkoutTemplateRequest request = new UpdateWorkoutTemplateRequest(
                2, "Edited template", List.of(
                updateItem("item-1", "incline-push-up", 1), updateItem("item-2", 2), updateItem("item-3", 3)));

        assertThat(service.update("template-id", request).exercises().getFirst().exercise().id())
                .isEqualTo("incline-push-up");
    }

    @Test
    void reportsTemplateAsNeedsRepairWhenItReferencesInactiveExercise() {
        WorkoutTemplateExercise inactive = templateExercise("item-1", 1);
        inactive.getExercise().setActive(false);
        when(templateMapper.findOwnedByUserId("user-id")).thenReturn(List.of(template()));
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(inactive));

        assertThat(service.list().getFirst().status()).isEqualTo(WorkoutTemplateStatus.NEEDS_REPAIR);
        verify(templateMapper).updateOwnedStatus(
                "template-id", "user-id", WorkoutTemplateStatus.NEEDS_REPAIR);
    }

    @Test
    void reportsTemplateAsNeedsRepairWhenPrescriptionIsIncompatibleWithExercise() {
        WorkoutTemplateExercise incompatible = templateExercise("item-1", "dumbbell-press", 1);
        incompatible.setExercise(exercise("dumbbell-press", "dumbbell"));
        incompatible.setLoadType(LoadType.BODYWEIGHT);
        when(templateMapper.findOwnedByUserId("user-id")).thenReturn(List.of(template()));
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(incompatible));

        assertThat(service.list().getFirst().status()).isEqualTo(WorkoutTemplateStatus.NEEDS_REPAIR);
    }

    @Test
    void reportsTemplateAsNeedsRepairWhenCurrentEquipmentIsUnavailable() {
        WorkoutTemplateExercise unavailable = templateExercise("item-1", "dumbbell-press", 1);
        unavailable.setExercise(exercise("dumbbell-press", "dumbbell"));
        unavailable.setLoadType(LoadType.ABSOLUTE_WEIGHT);
        when(templateMapper.findOwnedByUserId("user-id")).thenReturn(List.of(template()));
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(unavailable));

        WorkoutTemplateResponse response = service.list().getFirst();

        assertThat(response.status()).isEqualTo(WorkoutTemplateStatus.NEEDS_REPAIR);
        assertThat(response.repairReasons()).containsEntry(
                "item-1", com.fitness.domain.WorkoutTemplateRepairReason.EQUIPMENT_UNAVAILABLE);
    }

    @Test
    void restoresActiveStatusAfterTemplateIsRepaired() {
        WorkoutTemplate template = template();
        template.setStatus(WorkoutTemplateStatus.NEEDS_REPAIR);
        when(templateMapper.findOwnedByUserId("user-id")).thenReturn(List.of(template));
        when(templateMapper.findExercisesByTemplateId("template-id"))
                .thenReturn(List.of(templateExercise("item-1", 1)));

        assertThat(service.list().getFirst().status()).isEqualTo(WorkoutTemplateStatus.ACTIVE);
        verify(templateMapper).updateOwnedStatus("template-id", "user-id", WorkoutTemplateStatus.ACTIVE);
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
        return templateExercise("template-exercise-id", "push-up", 1);
    }

    private WorkoutTemplateExercise templateExercise(String id, int sequence) {
        return templateExercise(id, exerciseIdFor(id), sequence);
    }

    private WorkoutTemplateExercise templateExercise(String id, String exerciseId, int sequence) {
        WorkoutTemplateExercise item = new WorkoutTemplateExercise();
        item.setId(id);
        item.setTemplateId("template-id");
        item.setExerciseId(exerciseId);
        item.setSequence(sequence);
        item.setSets(id.startsWith("item-") ? 4 : 3);
        item.setReps(id.startsWith("item-") ? 10 : 12);
        item.setLoadType(LoadType.BODYWEIGHT);
        item.setRpe(new BigDecimal(id.startsWith("item-") ? "8.0" : "7.5"));
        item.setExercise(exercise(exerciseId, "body weight"));
        return item;
    }

    private Exercise exercise() {
        return exercise("push-up", "body weight");
    }

    private Exercise exercise(String id, String equipment) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setName("Push-Up");
        exercise.setBodyPart("chest");
        exercise.setTarget("pectorals");
        exercise.setEquipment(equipment);
        exercise.setActive(true);
        return exercise;
    }

    private UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate updateItem(String id, int sequence) {
        return updateItem(id, exerciseIdFor(id), sequence);
    }

    private UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate updateItem(
            String id, String exerciseId, int sequence
    ) {
        return new UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate(
                id, exerciseId, sequence, 4, 10, null,
                LoadType.BODYWEIGHT, new BigDecimal("8.0"));
    }

    private UpdateWorkoutTemplateRequest updateRequest() {
        return new UpdateWorkoutTemplateRequest(2, " Edited template ", List.of(
                updateItem("item-2", 1), updateItem("item-1", 2), updateItem("item-3", 3)));
    }

    private WorkoutTemplate template() {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setId("template-id");
        template.setOwnerUserId("user-id");
        template.setSourceWorkoutId("workout-id");
        template.setName("Saved chest");
        template.setBodyPart(OnDemandBodyPart.CHEST);
        template.setEquipmentSnapshot(List.of("body weight"));
        template.setProfileSnapshot(Map.of());
        template.setStatus(WorkoutTemplateStatus.ACTIVE);
        template.setVersion(2);
        return template;
    }

    private String exerciseIdFor(String templateExerciseId) {
        return switch (templateExerciseId) {
            case "item-2" -> "wide-push-up";
            case "item-3" -> "diamond-push-up";
            case "item-4" -> "decline-push-up";
            default -> "push-up";
        };
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
