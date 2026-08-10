package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.Prescription;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutStatus;
import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
import com.fitness.domain.WorkoutTemplateStatus;
import com.fitness.dto.CreateWorkoutTemplateRequest;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMapper;
import com.fitness.mapper.WorkoutMapper;
import com.fitness.mapper.WorkoutTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class WorkoutTemplateService {
    private static final String DEFAULT_TEMPLATE_NAME = "自定训练模板";

    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final WorkoutMapper workoutMapper;
    private final WorkoutTemplateMapper templateMapper;

    public WorkoutTemplateService(
            CurrentUserProvider currentUserProvider,
            UserMapper userMapper,
            WorkoutMapper workoutMapper,
            WorkoutTemplateMapper templateMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userMapper = userMapper;
        this.workoutMapper = workoutMapper;
        this.templateMapper = templateMapper;
    }

    @Transactional
    public WorkoutTemplateResponse create(CreateWorkoutTemplateRequest request) {
        String userId = currentUserProvider.requireUserId();
        UserProfile profile = userMapper.findProfileByUserId(userId);
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
        template.setProfileSnapshot(snapshot(profile));
        template.setStatus(WorkoutTemplateStatus.ACTIVE);
        templateMapper.insertTemplate(template);

        List<WorkoutTemplateExercise> items = prescriptions.stream()
                .map(prescription -> toTemplateExercise(template.getId(), prescription))
                .toList();
        items.forEach(templateMapper::insertTemplateExercise);
        template.setExercises(items);

        return toResponse(template, profile);
    }

    public List<WorkoutTemplateResponse> list() {
        String userId = currentUserProvider.requireUserId();
        UserProfile profile = userMapper.findProfileByUserId(userId);
        return templateMapper.findOwnedByUserId(userId).stream()
                .peek(template -> template.setExercises(
                        templateMapper.findExercisesByTemplateId(template.getId())))
                .map(template -> toResponse(template, profile))
                .toList();
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

    private WorkoutTemplateResponse toResponse(WorkoutTemplate template, UserProfile profile) {
        List<PlanDetailResponse.PrescriptionDetail> exercises = template.getExercises() == null
                ? List.of()
                : template.getExercises().stream().map(this::toPrescriptionDetail).toList();
        return new WorkoutTemplateResponse(
                template.getId(),
                template.getSourceWorkoutId(),
                template.getName(),
                template.getBodyPart(),
                template.getEquipmentSnapshot(),
                template.getProfileSnapshot(),
                profileChanged(template, profile),
                template.getStatus(),
                template.getVersion(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                exercises);
    }

    private Map<String, Object> snapshot(UserProfile profile) {
        if (profile == null) {
            return Map.of();
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("profileId", profile.getId());
        snapshot.put("fitnessLevel", profile.getFitnessLevel() == null
                ? null : profile.getFitnessLevel().name());
        snapshot.put("goal", profile.getGoal() == null ? null : profile.getGoal().name());
        snapshot.put("daysPerWeek", profile.getDaysPerWeek());
        snapshot.put("availableEquipment", profile.getAvailableEquipment() == null
                ? List.of() : List.copyOf(profile.getAvailableEquipment()));
        return snapshot;
    }

    private boolean profileChanged(WorkoutTemplate template, UserProfile profile) {
        Map<String, Object> saved = template.getProfileSnapshot();
        return saved != null && !saved.isEmpty() && !Objects.equals(saved, snapshot(profile));
    }

    private PlanDetailResponse.PrescriptionDetail toPrescriptionDetail(WorkoutTemplateExercise item) {
        Exercise exercise = item.getExercise();
        PlanDetailResponse.ExerciseSummary exerciseSummary = new PlanDetailResponse.ExerciseSummary(
                exercise.getId(), exercise.getName(), exercise.getBodyPart(), exercise.getTarget(),
                exercise.getEquipment(), exercise.getGifUrl(), exercise.getImageUrl());
        return new PlanDetailResponse.PrescriptionDetail(
                item.getId(), item.getSequence(), item.getSets(), item.getReps(), item.getLoad(),
                item.getLoadType(), item.getRpe(), exerciseSummary);
    }
}
