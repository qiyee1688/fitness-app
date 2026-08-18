package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.LoadType;
import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.Prescription;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutStatus;
import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
import com.fitness.domain.WorkoutTemplateStatus;
import com.fitness.dto.CreateWorkoutTemplateRequest;
import com.fitness.dto.UpdateWorkoutTemplateRequest;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.WorkoutMapper;
import com.fitness.mapper.WorkoutTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkoutTemplateServiceTest {
    private CurrentUserProvider currentUserProvider;
    private WorkoutMapper workoutMapper;
    private WorkoutTemplateMapper templateMapper;
    private WorkoutTemplateService service;

    @BeforeEach
    void setUp() {
        currentUserProvider = mock(CurrentUserProvider.class);
        workoutMapper = mock(WorkoutMapper.class);
        templateMapper = mock(WorkoutTemplateMapper.class);
        service = new WorkoutTemplateService(currentUserProvider, workoutMapper, templateMapper);
        when(currentUserProvider.requireUserId()).thenReturn("user-id");
    }

    @Test
    void createsTemplateByCopyingOnDemandDraftPrescriptionSnapshot() {
        Workout workout = workout(WorkoutStatus.DRAFT);
        Prescription prescription = prescription();
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
        assertThat(response.exercises().getFirst().exercise().coachCue()).isEqualTo("核心收紧");
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

    @Test
    void updatesTemplateNameAndPrescriptionSnapshotWithOptimisticLock() {
        WorkoutTemplate template = template();
        List<WorkoutTemplateExercise> existingItems = List.of(
                templateExercise("item-1", 1),
                templateExercise("item-2", 2),
                templateExercise("item-3", 3));
        when(templateMapper.findOwnedById("template-id", "user-id"))
                .thenReturn(template)
                .thenReturn(template);
        when(templateMapper.findExercisesByTemplateId("template-id"))
                .thenReturn(existingItems)
                .thenReturn(existingItems);
        when(templateMapper.updateOwnedTemplate("template-id", "user-id", 0, "Edited template"))
                .thenReturn(1);
        when(templateMapper.updateTemplateExercisePrescription(any(), any(), any()))
                .thenReturn(1);

        WorkoutTemplateResponse response = service.update("template-id", updateRequest());

        verify(templateMapper).updateOwnedTemplate("template-id", "user-id", 0, "Edited template");
        verify(templateMapper).updateTemplateExercisePrescription(
                "template-id", "user-id", updateRequest().exercises().getFirst());
        assertThat(response.templateId()).isEqualTo("template-id");
        assertThat(response.exercises()).hasSize(3);
    }

    @Test
    void rejectsTemplateUpdateWhenVersionChanged() {
        WorkoutTemplate template = template();
        when(templateMapper.findOwnedById("template-id", "user-id")).thenReturn(template);
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(
                templateExercise("item-1", 1),
                templateExercise("item-2", 2),
                templateExercise("item-3", 3)));
        when(templateMapper.updateOwnedTemplate("template-id", "user-id", 0, "Edited template"))
                .thenReturn(0);

        assertThatThrownBy(() -> service.update("template-id", updateRequest()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_TEMPLATE_CONFLICT));
    }

    @Test
    void rejectsTemplateUpdateForUnknownTemplateExercise() {
        WorkoutTemplate template = template();
        when(templateMapper.findOwnedById("template-id", "user-id")).thenReturn(template);
        when(templateMapper.findExercisesByTemplateId("template-id")).thenReturn(List.of(
                templateExercise("other-1", 1),
                templateExercise("other-2", 2),
                templateExercise("other-3", 3)));

        assertThatThrownBy(() -> service.update("template-id", updateRequest()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND));
        verify(templateMapper, never()).updateOwnedTemplate(any(), any(), anyInt(), any());
    }

    @Test
    void acceptsActiveBodyweightTemplateWithMatchingProfileEquipment() {
        WorkoutTemplateExercise item = templateExercise();
        item.getExercise().setActive(true);
        UserProfile profile = new UserProfile();
        profile.setAvailableEquipment(List.of("body weight"));

        assertThat(service.requiresRepair(List.of(item), profile)).isFalse();
    }

    private UpdateWorkoutTemplateRequest updateRequest() {
        return new UpdateWorkoutTemplateRequest(0, " Edited template ", List.of(
                updateItem("item-1", 1),
                updateItem("item-2", 2),
                updateItem("item-3", 3)));
    }

    private UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate updateItem(String id, int sequence) {
        return new UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate(
                id,
                sequence,
                4,
                10,
                null,
                LoadType.BODYWEIGHT,
                new BigDecimal("8.0"));
    }

    private WorkoutTemplate template() {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setId("template-id");
        template.setOwnerUserId("user-id");
        template.setSourceWorkoutId("workout-id");
        template.setName("Saved chest");
        template.setBodyPart(OnDemandBodyPart.CHEST);
        template.setEquipmentSnapshot(List.of("body weight"));
        template.setStatus(WorkoutTemplateStatus.ACTIVE);
        template.setVersion(0);
        return template;
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
        return templateExercise("template-exercise-id", 1);
    }

    private WorkoutTemplateExercise templateExercise(String id, int sequence) {
        WorkoutTemplateExercise item = new WorkoutTemplateExercise();
        item.setId(id);
        item.setTemplateId("template-id");
        item.setExerciseId("push-up");
        item.setSequence(sequence);
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
        exercise.setCoachCue("核心收紧");
        exercise.setCoachCueEn("Brace your core");
        return exercise;
    }
}
