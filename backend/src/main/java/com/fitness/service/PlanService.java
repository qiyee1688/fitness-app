package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.Plan;
import com.fitness.domain.Prescription;
import com.fitness.domain.User;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.dto.GeneratePlanRequest;
import com.fitness.dto.GeneratedPlanResponse;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.TodayWorkoutResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.ExerciseMapper;
import com.fitness.mapper.PlanMapper;
import com.fitness.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanService {

    private static final int PLAN_WEEKS = 8;

    private final UserMapper userMapper;
    private final ExerciseMapper exerciseMapper;
    private final PlanMapper planMapper;
    private final PlanGenerator planGenerator;

    public PlanService(
            UserMapper userMapper,
            ExerciseMapper exerciseMapper,
            PlanMapper planMapper,
            PlanGenerator planGenerator
    ) {
        this.userMapper = userMapper;
        this.exerciseMapper = exerciseMapper;
        this.planMapper = planMapper;
        this.planGenerator = planGenerator;
    }

    public GeneratedPlanResponse generatePlan(GeneratePlanRequest request) {
        User user = userMapper.findUserByUsername(request.username());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserProfile profile = userMapper.findProfileByUserId(user.getId());
        if (profile == null) {
            throw new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND);
        }

        List<String> equipment = profile.getAvailableEquipment();
        List<Exercise> candidates = exerciseMapper.findGeneratorCandidates(equipment);
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.PLAN_GENERATION_FAILED);
        }

        Plan activePlan = planMapper.findActiveByUserId(user.getId());
        if (activePlan != null && planMapper.supersedeActive(activePlan.getId(), activePlan.getVersion()) != 1) {
            throw new BusinessException(ErrorCode.PLAN_CONFLICT);
        }

        LocalDate startDate = request.startDate() == null ? LocalDate.now() : request.startDate();
        Plan plan = planGenerator.generate(profile, candidates, startDate);
        planMapper.insertPlan(plan);
        for (Workout workout : plan.getWorkouts()) {
            planMapper.insertWorkout(workout);
            workout.getPrescriptions().forEach(planMapper::insertPrescription);
        }
        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public PlanDetailResponse getActivePlan(String username) {
        User user = userMapper.findUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Plan plan = planMapper.findActiveByUserId(user.getId());
        if (plan == null) {
            throw new BusinessException(ErrorCode.ACTIVE_PLAN_NOT_FOUND);
        }

        List<Workout> workouts = planMapper.findWorkoutsByPlanId(plan.getId());
        Map<String, List<Prescription>> prescriptionsByWorkout =
                planMapper.findPrescriptionsByPlanId(plan.getId()).stream()
                        .collect(Collectors.groupingBy(Prescription::getWorkoutId));
        List<PlanDetailResponse.WorkoutDetail> workoutDetails = workouts.stream()
                .map(workout -> toWorkoutDetail(
                        plan, workout, prescriptionsByWorkout.getOrDefault(workout.getId(), List.of())))
                .toList();
        return new PlanDetailResponse(
                plan.getId(), plan.getStatus(), plan.getStartDate(), plan.getEndDate(),
                PLAN_WEEKS, plan.getProfileSnapshot(), workoutDetails);
    }


    @Transactional(readOnly = true)
    public TodayWorkoutResponse getTodayWorkout(String username, LocalDate date) {
        User user = findUser(username);
        Plan plan = findActivePlan(user.getId());
        LocalDate scheduledDate = date == null ? LocalDate.now() : date;
        int dayNumber = Math.toIntExact(ChronoUnit.DAYS.between(plan.getStartDate(), scheduledDate)) + 1;
        if (dayNumber < 1 || scheduledDate.isAfter(plan.getEndDate())) {
            throw new BusinessException(ErrorCode.TODAY_WORKOUT_NOT_FOUND);
        }

        Workout workout = planMapper.findWorkoutByPlanIdAndDayNumber(plan.getId(), dayNumber);
        if (workout == null) {
            throw new BusinessException(ErrorCode.TODAY_WORKOUT_NOT_FOUND);
        }
        return toTodayWorkoutResponse(plan, workout, scheduledDate, workout.getCompletedAt() != null);
    }

    public TodayWorkoutResponse completeWorkout(String username, String workoutId) {
        User user = findUser(username);
        Plan plan = findActivePlan(user.getId());
        Workout workout = planMapper.findWorkoutByIdAndPlanId(workoutId, plan.getId());
        if (workout == null) {
            throw new BusinessException(ErrorCode.WORKOUT_NOT_FOUND);
        }

        boolean alreadyCompleted = workout.getCompletedAt() != null;
        if (!alreadyCompleted) {
            LocalDateTime completedAt = LocalDateTime.now();
            if (planMapper.completeWorkout(workoutId, completedAt) == 1) {
                workout.setCompletedAt(completedAt);
            } else {
                workout = planMapper.findWorkoutByIdAndPlanId(workoutId, plan.getId());
                if (workout == null || workout.getCompletedAt() == null) {
                    throw new BusinessException(ErrorCode.PLAN_CONFLICT);
                }
                alreadyCompleted = true;
            }
        }

        LocalDate scheduledDate = plan.getStartDate().plusDays(workout.getDayNumber() - 1L);
        return toTodayWorkoutResponse(plan, workout, scheduledDate, alreadyCompleted);
    }

    private User findUser(String username) {
        User user = userMapper.findUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Plan findActivePlan(String userId) {
        Plan plan = planMapper.findActiveByUserId(userId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.ACTIVE_PLAN_NOT_FOUND);
        }
        return plan;
    }

    private TodayWorkoutResponse toTodayWorkoutResponse(
            Plan plan,
            Workout workout,
            LocalDate scheduledDate,
            boolean alreadyCompleted
    ) {
        List<PlanDetailResponse.PrescriptionDetail> prescriptions =
                planMapper.findPrescriptionsByWorkoutId(workout.getId()).stream()
                        .map(this::toPrescriptionDetail)
                        .toList();
        return new TodayWorkoutResponse(
                plan.getId(), workout.getId(), workout.getDayNumber(), scheduledDate,
                workout.getFocus(), workout.getCompletedAt(), alreadyCompleted, prescriptions);
    }

    private PlanDetailResponse.WorkoutDetail toWorkoutDetail(
            Plan plan,
            Workout workout,
            List<Prescription> prescriptions
    ) {
        List<PlanDetailResponse.PrescriptionDetail> details = prescriptions.stream()
                .map(prescription -> toPrescriptionDetail(prescription))
                .toList();
        return new PlanDetailResponse.WorkoutDetail(
                workout.getId(),
                workout.getDayNumber(),
                (workout.getDayNumber() - 1) / 7 + 1,
                plan.getStartDate().plusDays(workout.getDayNumber() - 1L),
                workout.getFocus(),
                details);
    }

    private PlanDetailResponse.PrescriptionDetail toPrescriptionDetail(Prescription prescription) {
        Exercise exercise = prescription.getExercise();
        PlanDetailResponse.ExerciseSummary exerciseSummary = new PlanDetailResponse.ExerciseSummary(
                exercise.getId(), exercise.getName(), exercise.getBodyPart(), exercise.getTarget(),
                exercise.getEquipment(), exercise.getGifUrl(), exercise.getImageUrl());
        return new PlanDetailResponse.PrescriptionDetail(
                prescription.getId(), prescription.getSequence(), prescription.getSets(),
                prescription.getReps(), prescription.getLoad(), prescription.getLoadType(),
                prescription.getRpe(), exerciseSummary);
    }

    private GeneratedPlanResponse toResponse(Plan plan) {
        List<GeneratedPlanResponse.WorkoutSummary> workouts = plan.getWorkouts().stream()
                .map(workout -> new GeneratedPlanResponse.WorkoutSummary(
                        workout.getId(),
                        workout.getDayNumber(),
                        workout.getFocus(),
                        workout.getPrescriptions().size()))
                .toList();
        return new GeneratedPlanResponse(
                plan.getId(), plan.getStatus(), plan.getStartDate(), plan.getEndDate(),
                workouts.size(), workouts);
    }
}
