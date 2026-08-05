package com.fitness.service;

import com.fitness.domain.Exercise;
import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;
import com.fitness.domain.LoadType;
import com.fitness.domain.Plan;
import com.fitness.domain.TrainingDayFocus;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanGeneratorTest {

    private final PlanGenerator generator = new PlanGenerator();

    @Test
    void generatesEightWeekPlanFromProfileWithoutTemplate() {
        UserProfile profile = profile(4);

        Plan plan = generator.generate(profile, exercises(), LocalDate.of(2026, 8, 10));

        assertThat(plan.getStartDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(plan.getEndDate()).isEqualTo(LocalDate.of(2026, 10, 4));
        assertThat(plan.getProfileSnapshot()).containsEntry("daysPerWeek", 4);
        assertThat(plan.getWorkouts()).hasSize(32);
        assertThat(plan.getWorkouts().stream().limit(4).map(Workout::getFocus))
                .containsExactly(
                        TrainingDayFocus.FULL_BODY,
                        TrainingDayFocus.PUSH,
                        TrainingDayFocus.PULL,
                        TrainingDayFocus.LEGS);
        assertThat(plan.getWorkouts()).allSatisfy(workout ->
                assertThat(workout.getPrescriptions()).isNotEmpty());
    }

    @Test
    void appendsCoreAndUsesRpeFirstPrescriptions() {
        Plan plan = generator.generate(profile(3), exercises(), LocalDate.of(2026, 8, 10));
        Workout legs = plan.getWorkouts().stream()
                .filter(workout -> workout.getFocus() == TrainingDayFocus.LEGS)
                .findFirst()
                .orElseThrow();

        assertThat(legs.getPrescriptions()).hasSize(5);
        assertThat(legs.getPrescriptions().subList(3, 5))
                .allSatisfy(prescription -> assertThat(prescription.getExerciseId()).isEqualTo("core"));
        assertThat(legs.getPrescriptions())
                .allSatisfy(prescription -> {
                    assertThat(prescription.getRpe()).isNotNull();
                    assertThat(prescription.getLoadType())
                            .isIn(LoadType.BODYWEIGHT, LoadType.RPE_ONLY);
                });
    }

    private UserProfile profile(int daysPerWeek) {
        UserProfile profile = new UserProfile();
        profile.setId("profile-id");
        profile.setUserId("user-id");
        profile.setFitnessLevel(FitnessLevel.BEGINNER);
        profile.setGoal(Goal.GENERAL_FITNESS);
        profile.setDaysPerWeek(daysPerWeek);
        profile.setAvailableEquipment(List.of("body weight", "dumbbell"));
        return profile;
    }

    private List<Exercise> exercises() {
        return List.of(
                exercise("push", "chest", "pectorals", "dumbbell"),
                exercise("pull", "back", "lats", "dumbbell"),
                exercise("legs", "upper legs", "quads", "body weight"),
                exercise("core", "waist", "abs", "body weight")
        );
    }

    private Exercise exercise(String id, String bodyPart, String target, String equipment) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setBodyPart(bodyPart);
        exercise.setTarget(target);
        exercise.setEquipment(equipment);
        return exercise;
    }
}
