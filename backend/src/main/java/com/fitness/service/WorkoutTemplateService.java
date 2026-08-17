package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.ExerciseSubstituteReason;
import com.fitness.domain.Prescription;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutStatus;
import com.fitness.domain.WorkoutTemplate;
import com.fitness.domain.WorkoutTemplateExercise;
import com.fitness.domain.WorkoutTemplateRepairReason;
import com.fitness.domain.WorkoutTemplateStatus;
import com.fitness.dto.CreateWorkoutTemplateRequest;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.UpdateWorkoutTemplateRequest;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.mapper.WorkoutMapper;
import com.fitness.mapper.WorkoutTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkoutTemplateService {
    private static final String DEFAULT_TEMPLATE_NAME = "自定训练模板";

    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final WorkoutMapper workoutMapper;
    private final WorkoutTemplateMapper templateMapper;
    private final ExerciseMapper exerciseMapper;

    public WorkoutTemplateService(
            CurrentUserProvider currentUserProvider,
            UserMapper userMapper,
            WorkoutMapper workoutMapper,
            WorkoutTemplateMapper templateMapper,
            ExerciseMapper exerciseMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userMapper = userMapper;
        this.workoutMapper = workoutMapper;
        this.templateMapper = templateMapper;
        this.exerciseMapper = exerciseMapper;
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

        return toResponseAndSynchronizeStatus(template, profile, userId);
    }

    @Transactional
    public List<WorkoutTemplateResponse> list() {
        String userId = currentUserProvider.requireUserId();
        UserProfile profile = userMapper.findProfileByUserId(userId);
        List<WorkoutTemplate> templates = templateMapper.findOwnedByUserId(userId).stream()
                .peek(template -> template.setExercises(templateMapper.findExercisesByTemplateId(template.getId())))
                .toList();
        long statusChangeCount = templates.stream()
                .filter(template -> resolveStatus(template, profile) != template.getStatus())
                .count();
        if (statusChangeCount > 0) {
            System.out.printf(
                    "Confirm synchronizing workout template statuses ownerUserId=%s templateCount=%d statusChangeCount=%d%n",
                    userId,
                    templates.size(),
                    statusChangeCount);
        }
        return templates.stream()
                .map(template -> toResponseAndSynchronizeStatus(template, profile, userId))
                .toList();
    }

    public List<Exercise> listSubstitutes(String templateId, String templateExerciseId) {
        String userId = currentUserProvider.requireUserId();
        WorkoutTemplate template = templateMapper.findOwnedById(templateId, userId);
        if (template == null) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND);
        }
        WorkoutTemplateExercise item = templateMapper.findExercisesByTemplateId(templateId).stream()
                .filter(candidate -> candidate.getId().equals(templateExerciseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND));
        return exerciseMapper.findTemplateSubstitutes(
                item.getExerciseId(),
                currentEquipment(userMapper.findProfileByUserId(userId)),
                ExerciseSubstituteReason.EQUIPMENT_SWAP);
    }

    @Transactional
    public WorkoutTemplateResponse update(String templateId, UpdateWorkoutTemplateRequest request) {
        String userId = currentUserProvider.requireUserId();
        WorkoutTemplate template = templateMapper.findOwnedById(templateId, userId);
        if (template == null) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND);
        }
        if (request.exercises().size() < minimumExerciseCount(template)) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_EDIT_INVALID);
        }

        List<WorkoutTemplateExercise> existingItems = templateMapper.findExercisesByTemplateId(templateId);
        Set<String> existingIds = existingItems.stream()
                .map(WorkoutTemplateExercise::getId)
                .collect(Collectors.toSet());
        Map<String, WorkoutTemplateExercise> existingById = existingItems.stream()
                .collect(Collectors.toMap(WorkoutTemplateExercise::getId, item -> item));
        List<String> retainedIds = request.exercises().stream()
                .map(UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate::templateExerciseId)
                .toList();
        if (retainedIds.stream().distinct().count() != retainedIds.size()
                || !existingIds.containsAll(retainedIds)) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_EDIT_INVALID);
        }
        Set<Integer> sequences = request.exercises().stream()
                .map(UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate::sequence)
                .collect(Collectors.toSet());
        boolean contiguousSequences = sequences.size() == request.exercises().size()
                && sequences.stream().allMatch(sequence -> sequence >= 1
                && sequence <= request.exercises().size());
        long distinctExerciseCount = request.exercises().stream()
                .map(UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate::exerciseId)
                .distinct()
                .count();
        if (!contiguousSequences || distinctExerciseCount != request.exercises().size()) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_EDIT_INVALID);
        }
        UserProfile profile = userMapper.findProfileByUserId(userId);
        List<String> currentEquipment = currentEquipment(profile);
        for (UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate update : request.exercises()) {
            WorkoutTemplateExercise existing = existingById.get(update.templateExerciseId());
            if (!existing.getExerciseId().equals(update.exerciseId())) {
                boolean allowed = exerciseMapper.findTemplateSubstitutes(
                                existing.getExerciseId(),
                                currentEquipment,
                                ExerciseSubstituteReason.EQUIPMENT_SWAP).stream()
                        .anyMatch(substitute -> substitute.getId().equals(update.exerciseId()));
                if (!allowed) {
                    throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_SUBSTITUTE_INVALID);
                }
            }
        }

        System.out.printf(
                "Confirm updating workout template templateId=%s ownerUserId=%s expectedVersion=%d itemCount=%d%n",
                templateId, userId, request.expectedVersion(), request.exercises().size());
        int updated = templateMapper.updateOwnedTemplate(
                templateId, userId, request.expectedVersion(), request.name().trim());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_CONFLICT);
        }
        templateMapper.reserveTemplateExerciseSequences(templateId, userId);
        templateMapper.deleteTemplateExercisesExcept(templateId, userId, retainedIds);
        for (UpdateWorkoutTemplateRequest.ExercisePrescriptionUpdate exercise : request.exercises()) {
            if (templateMapper.updateTemplateExercisePrescription(templateId, userId, exercise) == 0) {
                throw new BusinessException(ErrorCode.WORKOUT_TEMPLATE_NOT_FOUND);
            }
        }

        WorkoutTemplate refreshed = templateMapper.findOwnedById(templateId, userId);
        refreshed.setExercises(templateMapper.findExercisesByTemplateId(templateId));
        return toResponseAndSynchronizeStatus(refreshed, profile, userId);
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

    private int minimumExerciseCount(WorkoutTemplate template) {
        if (template.getBodyPart() == null) {
            return 1;
        }
        return switch (template.getBodyPart()) {
            case CHEST, BACK, SHOULDERS -> 3;
            case LEGS -> 4;
            case WAIST -> 2;
        };
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
                resolveStatus(template, profile),
                repairReasons(template, profile),
                template.getVersion(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                exercises);
    }

    private WorkoutTemplateResponse toResponseAndSynchronizeStatus(
            WorkoutTemplate template,
            UserProfile profile,
            String userId
    ) {
        WorkoutTemplateStatus resolvedStatus = resolveStatus(template, profile);
        if (resolvedStatus != template.getStatus()) {
            templateMapper.updateOwnedStatus(template.getId(), userId, resolvedStatus);
            template.setStatus(resolvedStatus);
        }
        return toResponse(template, profile);
    }

    private WorkoutTemplateStatus resolveStatus(WorkoutTemplate template, UserProfile profile) {
        return repairReasons(template, profile).isEmpty()
                ? WorkoutTemplateStatus.ACTIVE
                : WorkoutTemplateStatus.NEEDS_REPAIR;
    }

    private Map<String, WorkoutTemplateRepairReason> repairReasons(
            WorkoutTemplate template,
            UserProfile profile
    ) {
        if (template.getExercises() == null) {
            return Map.of();
        }
        Set<String> currentEquipment = new LinkedHashSet<>(currentEquipment(profile));
        Map<String, WorkoutTemplateRepairReason> reasons = new LinkedHashMap<>();
        for (WorkoutTemplateExercise item : template.getExercises()) {
            WorkoutTemplateRepairReason reason = repairReason(item, currentEquipment);
            if (reason != null) {
                reasons.put(item.getId(), reason);
            }
        }
        return Map.copyOf(reasons);
    }

    private WorkoutTemplateRepairReason repairReason(
            WorkoutTemplateExercise item,
            Set<String> currentEquipment
    ) {
        if (item.getExercise() == null || !item.getExercise().isActive()) {
            return WorkoutTemplateRepairReason.EXERCISE_UNAVAILABLE;
        }
        if (!hasEquipment(item.getExercise(), currentEquipment)) {
            return WorkoutTemplateRepairReason.EQUIPMENT_UNAVAILABLE;
        }
        if (!isPrescriptionCompatible(item)) {
            return WorkoutTemplateRepairReason.PRESCRIPTION_INCOMPATIBLE;
        }
        return null;
    }

    private boolean hasEquipment(Exercise exercise, Set<String> currentEquipment) {
        return exercise.getEquipment() != null && currentEquipment.stream()
                .anyMatch(equipment -> equipment.equalsIgnoreCase(exercise.getEquipment().trim()));
    }

    private boolean isPrescriptionCompatible(WorkoutTemplateExercise item) {
        if (item.getLoadType() == null || item.getExercise() == null) {
            return false;
        }
        boolean bodyweightExercise = "body weight".equalsIgnoreCase(
                item.getExercise().getEquipment() == null ? "" : item.getExercise().getEquipment().trim());
        return switch (item.getLoadType()) {
            case BODYWEIGHT -> bodyweightExercise;
            case ABSOLUTE_WEIGHT, PERCENT_1RM -> !bodyweightExercise;
            case RPE_ONLY, DURATION -> true;
        };
    }

    private List<String> currentEquipment(UserProfile profile) {
        Set<String> equipment = new LinkedHashSet<>();
        equipment.add("body weight");
        if (profile != null && profile.getAvailableEquipment() != null) {
            equipment.addAll(profile.getAvailableEquipment());
        }
        return List.copyOf(equipment);
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
                exercise.getEquipment(), exercise.getGifUrl(), exercise.getImageUrl(),
                exercise.getCoachCue(), exercise.getCoachCueEn());
        return new PlanDetailResponse.PrescriptionDetail(
                item.getId(), item.getSequence(), item.getSets(), item.getReps(), item.getLoad(),
                item.getLoadType(), item.getRpe(), exerciseSummary);
    }
}
