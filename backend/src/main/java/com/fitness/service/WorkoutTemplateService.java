package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.Prescription;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutStatus;
import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
import com.fitness.domain.WorkoutTemplateStatus;
import com.fitness.domain.UserProfile;
import com.fitness.dto.CreateWorkoutTemplateRequest;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.UpdateWorkoutTemplateRequest;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.WorkoutMapper;
import com.fitness.mapper.WorkoutTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WorkoutTemplateService {
    private static final String DEFAULT_TEMPLATE_NAME = "自定训练模板";

    private final CurrentUserProvider currentUserProvider;
    private final WorkoutMapper workoutMapper;
    private final WorkoutTemplateMapper templateMapper;

    public WorkoutTemplateService(
            CurrentUserProvider currentUserProvider,
            WorkoutMapper workoutMapper,
            WorkoutTemplateMapper templateMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.workoutMapper = workoutMapper;
        this.templateMapper = templateMapper;
    }

    @Transactional
    public WorkoutTemplateResponse create(CreateWorkoutTemplateRequest request) {
        String userId = currentUserProvider.requireUserId();
        Workout workout = workoutMapper.findOwnedById(request.sourceWorkoutId(), userId);
        if (workout == null) {
            throw new BusinessException(ErrorCode.ON_DEMAND_WORKOUT_NOT_FOUND);
        }
        if (workout.getStatus() != WorkoutStatus.DRAFT) {
            throw new BusinessException(ErrorCode.WORKOUT_STATE_CONFLICT);
        }

        List<Prescription> prescriptions = workoutMapper.findPrescriptionsByWorkoutId(workout.getId());
        if (prescriptions.isEmpty()) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_INVALID);
        }

        WorkoutTemplate template = new WorkoutTemplate();
        template.setId(UUID.randomUUID().toString());
        template.setOwnerUserId(userId);
        template.setSourceWorkoutId(workout.getId());
        template.setName(resolveName(request.name(), workout));
        template.setBodyPart(workout.getRequestedBodyPart());
        template.setEquipmentSnapshot(workout.getEquipmentSnapshot());
        template.setStatus(WorkoutTemplateStatus.ACTIVE);
        templateMapper.insertTemplate(template);

        List<WorkoutTemplateExercise> items = prescriptions.stream()
                .map(prescription -> toTemplateExercise(template.getId(), prescription))
                .toList();
        items.forEach(templateMapper::insertTemplateExercise);
        template.setExercises(items);

        return toResponse(template);
    }

    public List<WorkoutTemplateResponse> list() {
        String userId = currentUserProvider.requireUserId();
        return templateMapper.findOwnedByUserId(userId).stream()
                .peek(template -> template.setExercises(
                        templateMapper.findExercisesByTemplateId(template.getId())))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WorkoutTemplateResponse update(String templateId, UpdateWorkoutTemplateRequest request) {
        String userId = currentUserProvider.requireUserId();
        WorkoutTemplate template = templateMapper.findOwnedById(templateId, userId);
        if (template == null) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND);
        }

        List<WorkoutTemplateExercise> existingItems = templateMapper.findExercisesByTemplateId(templateId);
        validatePrescriptionUpdates(template, existingItems, request.exercises());

        System.out.printf(
                "Confirm updating workout template templateId=%s ownerUserId=%s expectedVersion=%d itemCount=%d%n",
                templateId,
                userId,
                request.expectedVersion(),
                request.exercises().size());
        int updated = templateMapper.updateOwnedTemplate(
                templateId,
                userId,
                request.expectedVersion(),
                request.name().trim());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_CONFLICT);
        }

        for (UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate exercise : request.exercises()) {
            int itemUpdated = templateMapper.updateTemplateExercisePrescription(templateId, userId, exercise);
            if (itemUpdated == 0) {
                throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND);
            }
        }

        WorkoutTemplate refreshed = templateMapper.findOwnedById(templateId, userId);
        refreshed.setExercises(templateMapper.findExercisesByTemplateId(templateId));
        return toResponse(refreshed);
    }

    @Transactional
    public void delete(String templateId) {
        String userId = currentUserProvider.requireUserId();
        System.out.printf(
                "Confirm deleting workout template templateId=%s ownerUserId=%s%n",
                templateId,
                userId);
        int deleted = templateMapper.deleteOwnedById(templateId, userId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND);
        }
    }

    boolean requiresRepair(List<WorkoutTemplateExercise> exercises, UserProfile profile) {
        if (exercises == null || exercises.isEmpty()) {
            return true;
        }
        Set<String> availableEquipment = new LinkedHashSet<>();
        availableEquipment.add("body weight");
        if (profile != null && profile.getAvailableEquipment() != null) {
            profile.getAvailableEquipment().stream()
                    .filter(equipment -> equipment != null && !equipment.isBlank())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(availableEquipment::add);
        }
        return exercises.stream().anyMatch(item -> requiresRepair(item, availableEquipment));
    }

    private boolean requiresRepair(WorkoutTemplateExercise item, Set<String> availableEquipment) {
        Exercise exercise = item.getExercise();
        if (exercise == null || !exercise.isActive()) {
            return true;
        }
        String exerciseEquipment = exercise.getEquipment() == null
                ? ""
                : exercise.getEquipment().trim().toLowerCase();
        if (!availableEquipment.contains(exerciseEquipment)) {
            return true;
        }
        boolean bodyweightExercise = "body weight".equals(exerciseEquipment);
        if (item.getLoadType() == null) {
            return true;
        }
        return switch (item.getLoadType()) {
            case BODYWEIGHT -> !bodyweightExercise;
            case ABSOLUTE_WEIGHT, PERCENT_1RM -> bodyweightExercise;
            case RPE_ONLY, DURATION -> false;
        };
    }

    private void validatePrescriptionUpdates(
            WorkoutTemplate template,
            List<WorkoutTemplateExercise> existingItems,
            List<UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate> updates
    ) {
        int minimumExerciseCount = minimumExerciseCount(template);
        if (updates.size() < minimumExerciseCount || updates.size() != existingItems.size()) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_INVALID);
        }

        Map<String, WorkoutTemplateExercise> existingById = existingItems.stream()
                .collect(Collectors.toMap(WorkoutTemplateExercise::getId, Function.identity()));
        long distinctSequenceCount = updates.stream()
                .map(UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate::sequence)
                .distinct()
                .count();
        if (distinctSequenceCount != updates.size()) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_INVALID);
        }

        for (UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate update : updates) {
            WorkoutTemplateExercise existing = existingById.get(update.templateExerciseId());
            if (existing == null) {
                throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND);
            }
            if (!existing.getLoadType().equals(update.loadType()) && update.load() == null) {
                throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_INVALID);
            }
        }
    }

    private int minimumExerciseCount(WorkoutTemplate template) {
        return switch (template.getBodyPart()) {
            case CHEST, BACK, SHOULDERS -> 3;
            case LEGS -> 4;
            case WAIST -> 2;
        };
    }

    private WorkoutTemplateExercise toTemplateExercise(String templateId, Prescription prescription) {
        WorkoutTemplateExercise item = new WorkoutTemplateExercise();
        item.setId(UUID.randomUUID().toString());
        item.setTemplateId(templateId);
        item.setExerciseId(prescription.getExerciseId());
        item.setSequence(prescription.getSequence());
        item.setSets(prescription.getSets());
        item.setReps(prescription.getReps());
        item.setLoad(prescription.getLoad());
        item.setLoadType(prescription.getLoadType());
        item.setRpe(prescription.getRpe());
        item.setExercise(prescription.getExercise());
        return item;
    }

    private String resolveName(String requestedName, Workout workout) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName.trim();
        }
        if (workout.getRequestedBodyPart() == null) {
            return DEFAULT_TEMPLATE_NAME;
        }
        return DEFAULT_TEMPLATE_NAME + " - " + workout.getRequestedBodyPart().name();
    }

    private WorkoutTemplateResponse toResponse(WorkoutTemplate template) {
        List<PlanDetailResponse.PrescriptionDetail> exercises = template.getExercises() == null
                ? List.of()
                : template.getExercises().stream().map(this::toPrescriptionDetail).toList();
        return new WorkoutTemplateResponse(
                template.getId(),
                template.getSourceWorkoutId(),
                template.getName(),
                template.getBodyPart(),
                template.getEquipmentSnapshot(),
                template.getStatus(),
                template.getVersion(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                exercises);
    }

    private PlanDetailResponse.PrescriptionDetail toPrescriptionDetail(WorkoutTemplateExercise item) {
        Exercise exercise = item.getExercise();
        PlanDetailResponse.ExerciseSummary exerciseSummary = new PlanDetailResponse.ExerciseSummary(
                exercise.getId(), exercise.getName(), exercise.getBodyPart(), exercise.getTarget(),
                exercise.getEquipment(), exercise.getGifUrl(), exercise.getImageUrl(),
                exercise.getCoachCue(), exercise.getCoachCueEn());
        return new PlanDetailResponse.PrescriptionDetail(
                item.getId(), item.getSequence(), item.getSets(), item.getReps(), item.getLoad(),
                item.getLoadType(), item.getRpe(), exerciseSummary);
    }
}
