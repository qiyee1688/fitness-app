package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.FitnessLevel;
import com.fitness.domain.LoadType;
import com.fitness.domain.Prescription;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.domain.WorkoutSource;
import com.fitness.domain.WorkoutStatus;
import com.fitness.dto.GenerateOnDemandWorkoutRequest;
import com.fitness.dto.OnDemandWorkoutResponse;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.mapper.WorkoutMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class OnDemandWorkoutService {
    private static final String BODYWEIGHT = "body weight";

    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final ExerciseMapper exerciseMapper;
    private final WorkoutMapper workoutMapper;
    private final Clock clock;

    @Autowired
    public OnDemandWorkoutService(
            CurrentUserProvider currentUserProvider,
            UserMapper userMapper,
            ExerciseMapper exerciseMapper,
            WorkoutMapper workoutMapper
    ) {
        this(currentUserProvider, userMapper, exerciseMapper, workoutMapper, Clock.systemDefaultZone());
    }

    OnDemandWorkoutService(
            CurrentUserProvider currentUserProvider,
            UserMapper userMapper,
            ExerciseMapper exerciseMapper,
            WorkoutMapper workoutMapper,
            Clock clock
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userMapper = userMapper;
        this.exerciseMapper = exerciseMapper;
        this.workoutMapper = workoutMapper;
        this.clock = clock;
    }

    public OnDemandWorkoutResponse generate(GenerateOnDemandWorkoutRequest request) {
        String userId = currentUserProvider.requireUserId();
        UserProfile profile = userMapper.findProfileByUserId(userId);
        FitnessLevel level = profile == null ? FitnessLevel.BEGINNER : profile.getFitnessLevel();
        List<String> equipment = resolveEquipment(profile, request.equipment());
        List<Exercise> candidates = exerciseMapper.findOnDemandCandidates(
                request.bodyPart().datasetValues(), equipment);
        if (candidates.size() < request.bodyPart().exerciseCount()) {
            throw new BusinessException(ErrorCode.ON_DEMAND_GENERATION_FAILED);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Workout workout = new Workout();
        workout.setId(UUID.randomUUID().toString());
        workout.setOwnerUserId(userId);
        workout.setRequestedBodyPart(request.bodyPart());
        workout.setEquipmentSnapshot(equipment);
        workout.setSource(WorkoutSource.ON_DEMAND);
        workout.setStatus(WorkoutStatus.DRAFT);
        workout.setExpiresAt(now.plusHours(24));
        workout.setPrescriptions(createPrescriptions(
                workout.getId(), candidates, request.bodyPart().exerciseCount(), level));

        workoutMapper.insertWorkout(workout);
        workout.getPrescriptions().forEach(workoutMapper::insertPrescription);
        if (request.saveEquipmentToProfile() && profile != null) {
            profile.setAvailableEquipment(equipment);
            userMapper.updateProfile(profile);
        }
        return toResponse(workout);
    }

    public OnDemandWorkoutResponse start(String workoutId) {
        return transition(workoutId, WorkoutStatus.DRAFT, WorkoutStatus.IN_PROGRESS);
    }

    public OnDemandWorkoutResponse complete(String workoutId) {
        return transition(workoutId, WorkoutStatus.IN_PROGRESS, WorkoutStatus.COMPLETED);
    }

    public int cleanupExpiredDrafts() {
        return workoutMapper.deleteExpiredDrafts(LocalDateTime.now(clock));
    }

    private OnDemandWorkoutResponse transition(
            String workoutId,
            WorkoutStatus expected,
            WorkoutStatus next
    ) {
        String userId = currentUserProvider.requireUserId();
        Workout workout = workoutMapper.findOwnedById(workoutId, userId);
        if (workout == null) {
            throw new BusinessException(ErrorCode.ON_DEMAND_WORKOUT_NOT_FOUND);
        }
        LocalDateTime changedAt = LocalDateTime.now(clock);
        if (expected == WorkoutStatus.DRAFT && workout.getExpiresAt() != null
                && !workout.getExpiresAt().isAfter(changedAt)) {
            throw new BusinessException(ErrorCode.WORKOUT_STATE_CONFLICT);
        }
        if (workoutMapper.transitionStatus(workoutId, userId, expected, next, changedAt) != 1) {
            throw new BusinessException(ErrorCode.WORKOUT_STATE_CONFLICT);
        }
        workout.setStatus(next);
        if (next == WorkoutStatus.IN_PROGRESS) {
            workout.setStartedAt(changedAt);
        } else if (next == WorkoutStatus.COMPLETED) {
            workout.setCompletedAt(changedAt);
        }
        workout.setPrescriptions(workoutMapper.findPrescriptionsByWorkoutId(workoutId));
        return toResponse(workout);
    }

    private List<String> resolveEquipment(UserProfile profile, List<String> requested) {
        Set<String> resolved = new LinkedHashSet<>();
        resolved.add(BODYWEIGHT);
        if (profile != null && profile.getAvailableEquipment() != null) {
            profile.getAvailableEquipment().stream().map(this::normalize).forEach(resolved::add);
        }
        if (requested != null) {
            requested.stream().filter(value -> value != null && !value.isBlank())
                    .map(this::normalize).forEach(resolved::add);
        }
        return List.copyOf(resolved);
    }

    private List<Prescription> createPrescriptions(
            String workoutId,
            List<Exercise> candidates,
            int count,
            FitnessLevel level
    ) {
        List<Prescription> prescriptions = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            Exercise exercise = candidates.get(index);
            Prescription prescription = new Prescription();
            prescription.setId(UUID.randomUUID().toString());
            prescription.setWorkoutId(workoutId);
            prescription.setExerciseId(exercise.getId());
            prescription.setSequence(index + 1);
            prescription.setSets(switch (level) {
                case BEGINNER -> 3;
                case INTERMEDIATE -> 4;
                case ADVANCED -> 5;
            });
            prescription.setReps(switch (level) {
                case BEGINNER -> 12;
                case INTERMEDIATE -> 10;
                case ADVANCED -> 5;
            });
            prescription.setLoadType(BODYWEIGHT.equals(normalize(exercise.getEquipment()))
                    ? LoadType.BODYWEIGHT : LoadType.RPE_ONLY);
            prescription.setRpe(switch (level) {
                case BEGINNER -> new BigDecimal("7.5");
                case INTERMEDIATE -> new BigDecimal("8.0");
                case ADVANCED -> new BigDecimal("8.5");
            });
            prescription.setExercise(exercise);
            prescriptions.add(prescription);
        }
        return List.copyOf(prescriptions);
    }

    private OnDemandWorkoutResponse toResponse(Workout workout) {
        List<PlanDetailResponse.PrescriptionDetail> details = workout.getPrescriptions().stream()
                .map(this::toPrescriptionDetail)
                .toList();
        return new OnDemandWorkoutResponse(
                workout.getId(), workout.getRequestedBodyPart(), workout.getEquipmentSnapshot(),
                workout.getSource(), workout.getStatus(), workout.getStartedAt(), workout.getCompletedAt(),
                workout.getExpiresAt(), details);
    }

    private PlanDetailResponse.PrescriptionDetail toPrescriptionDetail(Prescription prescription) {
        Exercise exercise = prescription.getExercise();
        return new PlanDetailResponse.PrescriptionDetail(
                prescription.getId(), prescription.getSequence(), prescription.getSets(),
                prescription.getReps(), prescription.getLoad(), prescription.getLoadType(),
                prescription.getRpe(), new PlanDetailResponse.ExerciseSummary(
                        exercise.getId(), exercise.getName(), exercise.getBodyPart(), exercise.getTarget(),
                        exercise.getEquipment(), exercise.getGifUrl(), exercise.getImageUrl()));
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
