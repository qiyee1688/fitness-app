package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.FitnessLevel;
import com.fitness.domain.LoadType;
import com.fitness.domain.Plan;
import com.fitness.domain.PlanStatus;
import com.fitness.domain.Prescription;
import com.fitness.domain.TrainingDayFocus;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Component
public class PlanGenerator {

    private static final int PLAN_WEEKS = 8;

    public Plan generate(UserProfile profile, List<Exercise> candidates, LocalDate startDate) {
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Exercise candidates must not be empty");
        }

        Plan plan = new Plan();
        plan.setId(UUID.randomUUID().toString());
        plan.setUserId(profile.getUserId());
        plan.setProfileSnapshot(snapshot(profile));
        plan.setStatus(PlanStatus.ACTIVE);
        plan.setStartDate(startDate);
        plan.setEndDate(startDate.plusWeeks(PLAN_WEEKS).minusDays(1));
        plan.setVersion(0);
        plan.setWorkouts(generateWorkouts(plan.getId(), profile, candidates));
        return plan;
    }

    private List<Workout> generateWorkouts(String planId, UserProfile profile, List<Exercise> candidates) {
        List<TrainingDayFocus> weeklyFocuses = weeklyFocuses(profile.getDaysPerWeek());
        Map<TrainingDayFocus, Integer> offsets = new EnumMap<>(TrainingDayFocus.class);
        List<Workout> workouts = new ArrayList<>();

        for (int week = 0; week < PLAN_WEEKS; week++) {
            for (int day = 0; day < weeklyFocuses.size(); day++) {
                TrainingDayFocus focus = weeklyFocuses.get(day);
                Workout workout = new Workout();
                workout.setId(UUID.randomUUID().toString());
                workout.setPlanId(planId);
                workout.setDayNumber(week * 7 + day + 1);
                workout.setFocus(focus);
                workout.setPrescriptions(createPrescriptions(
                        workout.getId(), focus, profile.getFitnessLevel(), candidates, offsets));
                workouts.add(workout);
            }
        }
        return List.copyOf(workouts);
    }

    private List<Prescription> createPrescriptions(
            String workoutId,
            TrainingDayFocus focus,
            FitnessLevel fitnessLevel,
            List<Exercise> candidates,
            Map<TrainingDayFocus, Integer> offsets
    ) {
        List<Exercise> focusCandidates = candidates.stream().filter(matches(focus)).toList();
        if (focusCandidates.isEmpty()) {
            focusCandidates = candidates;
        }

        int mainCount = focus == TrainingDayFocus.FULL_BODY ? 4 : 3;
        int coreCount = focus == TrainingDayFocus.LEGS ? 2 : 1;
        List<Exercise> coreCandidates = candidates.stream()
                .filter(exercise -> "waist".equals(normalize(exercise.getBodyPart())))
                .toList();
        if (coreCandidates.isEmpty()) {
            coreCandidates = focusCandidates;
        }

        int offset = offsets.getOrDefault(focus, 0);
        List<Prescription> prescriptions = new ArrayList<>();
        addPrescriptions(prescriptions, workoutId, focusCandidates, mainCount, offset, fitnessLevel);
        addPrescriptions(prescriptions, workoutId, coreCandidates, coreCount, offset, fitnessLevel);
        offsets.put(focus, offset + mainCount + coreCount);
        return List.copyOf(prescriptions);
    }

    private void addPrescriptions(
            List<Prescription> prescriptions,
            String workoutId,
            List<Exercise> exercises,
            int count,
            int offset,
            FitnessLevel fitnessLevel
    ) {
        for (int index = 0; index < count; index++) {
            Exercise exercise = exercises.get(Math.floorMod(offset + index, exercises.size()));
            Prescription prescription = new Prescription();
            prescription.setId(UUID.randomUUID().toString());
            prescription.setWorkoutId(workoutId);
            prescription.setExerciseId(exercise.getId());
            prescription.setSequence(prescriptions.size() + 1);
            prescription.setSets(setsFor(fitnessLevel));
            prescription.setReps(repsFor(fitnessLevel));
            prescription.setLoadType(isBodyweight(exercise) ? LoadType.BODYWEIGHT : LoadType.RPE_ONLY);
            prescription.setRpe(rpeFor(fitnessLevel));
            prescriptions.add(prescription);
        }
    }

    private Predicate<Exercise> matches(TrainingDayFocus focus) {
        return exercise -> {
            String bodyPart = normalize(exercise.getBodyPart());
            String target = normalize(exercise.getTarget());
            return switch (focus) {
                case PUSH -> bodyPart.equals("chest") || bodyPart.equals("shoulders")
                        || target.contains("triceps");
                case PULL -> bodyPart.equals("back") || target.contains("biceps")
                        || target.contains("lats");
                case LEGS -> bodyPart.equals("upper legs") || bodyPart.equals("lower legs");
                case FULL_BODY -> !bodyPart.equals("waist");
            };
        };
    }

    private List<TrainingDayFocus> weeklyFocuses(int daysPerWeek) {
        return switch (daysPerWeek) {
            case 2 -> List.of(TrainingDayFocus.FULL_BODY, TrainingDayFocus.FULL_BODY);
            case 3 -> List.of(TrainingDayFocus.PUSH, TrainingDayFocus.PULL, TrainingDayFocus.LEGS);
            case 4 -> List.of(TrainingDayFocus.FULL_BODY, TrainingDayFocus.PUSH,
                    TrainingDayFocus.PULL, TrainingDayFocus.LEGS);
            case 5 -> List.of(TrainingDayFocus.PUSH, TrainingDayFocus.PULL,
                    TrainingDayFocus.LEGS, TrainingDayFocus.PUSH, TrainingDayFocus.PULL);
            case 6 -> List.of(TrainingDayFocus.PUSH, TrainingDayFocus.PULL,
                    TrainingDayFocus.LEGS, TrainingDayFocus.PUSH,
                    TrainingDayFocus.PULL, TrainingDayFocus.LEGS);
            default -> throw new IllegalArgumentException("daysPerWeek must be between 2 and 6");
        };
    }

    private Map<String, Object> snapshot(UserProfile profile) {
        return Map.of(
                "profileId", profile.getId(),
                "fitnessLevel", profile.getFitnessLevel().name(),
                "goal", profile.getGoal().name(),
                "daysPerWeek", profile.getDaysPerWeek(),
                "availableEquipment", List.copyOf(profile.getAvailableEquipment())
        );
    }

    private int setsFor(FitnessLevel level) {
        return level == FitnessLevel.BEGINNER ? 3 : 4;
    }

    private int repsFor(FitnessLevel level) {
        return level == FitnessLevel.ADVANCED ? 8 : 10;
    }

    private BigDecimal rpeFor(FitnessLevel level) {
        return switch (level) {
            case BEGINNER -> new BigDecimal("7.0");
            case INTERMEDIATE -> new BigDecimal("8.0");
            case ADVANCED -> new BigDecimal("8.5");
        };
    }

    private boolean isBodyweight(Exercise exercise) {
        return normalize(exercise.getEquipment()).contains("body weight");
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
