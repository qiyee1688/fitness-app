package com.fitness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.domain.Goal;
import com.fitness.domain.MacroTargetBasis;
import com.fitness.domain.NutritionRule;
import com.fitness.domain.NutritionTiming;
import com.fitness.domain.NutritionTip;
import com.fitness.domain.NutritionUnit;
import com.fitness.domain.TrainingDayFocus;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.dto.NutritionTipResponse;
import com.fitness.mapper.NutritionMapper;
import com.fitness.mapper.WorkoutMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {
    private static final String WORKOUT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String RULE_ID = "22222222-2222-2222-2222-222222222222";

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private NutritionMapper nutritionMapper;

    @Mock
    private WorkoutMapper workoutMapper;

    private NutritionService nutritionService;

    @BeforeEach
    void setUp() {
        nutritionService = new NutritionService(
                currentUserProvider, nutritionMapper, workoutMapper, new ObjectMapper());
    }

    @Test
    void generatesPerKgTargetsAndSnapshotsRuleInputsWhenWeightIsAvailable() {
        NutritionRule rule = rule(Map.of(
                "protein", target(1.8, 120, "GRAMS"),
                "kcal", target(35, 2400, "KILOCALORIES")));
        when(nutritionMapper.findMatchingRule(
                org.mockito.ArgumentMatchers.eq(Goal.MUSCLE_GAIN),
                org.mockito.ArgumentMatchers.eq(TrainingDayFocus.PUSH),
                org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(2) == NutritionTiming.PRE_WORKOUT
                        ? rule : null);
        when(nutritionMapper.insertTip(any())).thenReturn(1);

        List<NutritionTipResponse> result = nutritionService.generateForWorkout(
                workout(), profile(new BigDecimal("60.0")));

        assertThat(result).hasSize(1);
        NutritionTipResponse response = result.getFirst();
        assertThat(response.macroTargets().protein().value()).isEqualByComparingTo("108.0");
        assertThat(response.macroTargets().protein().unit()).isEqualTo(NutritionUnit.GRAMS);
        assertThat(response.macroTargets().protein().basis())
                .isEqualTo(MacroTargetBasis.PER_KG_BODYWEIGHT);
        assertThat(response.macroTargets().kcal().value()).isEqualByComparingTo("2100.0");
        assertThat(response.note()).isEqualTo("训练前补充能量");
        assertThat(response.noteEn()).isEqualTo("Fuel before training");
        assertThat(response.ruleId()).isEqualTo(RULE_ID);
        assertThat(response.ruleVersion()).isEqualTo(3);
        assertThat(response.weightKgSnapshot()).isEqualByComparingTo("60.0");

        ArgumentCaptor<NutritionTip> tipCaptor = ArgumentCaptor.forClass(NutritionTip.class);
        verify(nutritionMapper).insertTip(tipCaptor.capture());
        assertThat(tipCaptor.getValue().getMacroTargets()).containsKeys("protein", "kcal");
    }

    @Test
    void fallsBackToAbsoluteTargetsWhenWeightIsMissing() {
        NutritionRule rule = rule(Map.of(
                "protein", target(1.8, 120, "GRAMS"),
                "kcal", target(35, 2400, "KILOCALORIES")));
        when(nutritionMapper.findMatchingRule(
                org.mockito.ArgumentMatchers.eq(Goal.MUSCLE_GAIN),
                org.mockito.ArgumentMatchers.eq(TrainingDayFocus.PUSH),
                org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(2) == NutritionTiming.DAILY
                        ? rule : null);
        when(nutritionMapper.insertTip(any())).thenReturn(1);

        List<NutritionTipResponse> result = nutritionService.generateForWorkout(
                workout(), profile(null));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().macroTargets().protein().value()).isEqualByComparingTo("120.0");
        assertThat(result.getFirst().macroTargets().protein().basis())
                .isEqualTo(MacroTargetBasis.ABSOLUTE);
        assertThat(result.getFirst().macroTargets().kcal().value()).isEqualByComparingTo("2400.0");
        assertThat(result.getFirst().weightKgSnapshot()).isNull();
    }

    @Test
    void returnsNoTipsWhenNoRuleMatches() {
        List<NutritionTipResponse> result = nutritionService.generateForWorkout(
                workout(), profile(new BigDecimal("60.0")));

        assertThat(result).isEmpty();
        verify(nutritionMapper, never()).insertTip(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void skipsRuleWhenNoTargetCanBeCalculated() {
        NutritionRule rule = rule(Map.of(
                "protein", Map.of("perKg", 1.8, "unit", "GRAMS")));
        when(nutritionMapper.findMatchingRule(
                org.mockito.ArgumentMatchers.eq(Goal.MUSCLE_GAIN),
                org.mockito.ArgumentMatchers.eq(TrainingDayFocus.PUSH),
                org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(2) == NutritionTiming.POST_WORKOUT
                        ? rule : null);

        List<NutritionTipResponse> result = nutritionService.generateForWorkout(workout(), profile(null));

        assertThat(result).isEmpty();
        verify(nutritionMapper, never()).insertTip(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void groupsPersistedTipsByWorkoutForPlanResponses() {
        NutritionTip first = persistedTip("workout-a", NutritionTiming.PRE_WORKOUT);
        NutritionTip second = persistedTip("workout-a", NutritionTiming.DAILY);
        NutritionTip third = persistedTip("workout-b", NutritionTiming.POST_WORKOUT);
        when(nutritionMapper.findByPlanId("plan-id")).thenReturn(List.of(first, second, third));

        Map<String, List<NutritionTipResponse>> result = nutritionService.listByPlanId("plan-id");

        assertThat(result).containsOnlyKeys("workout-a", "workout-b");
        assertThat(result.get("workout-a")).extracting(NutritionTipResponse::timing)
                .containsExactly(NutritionTiming.PRE_WORKOUT, NutritionTiming.DAILY);
    }

    @Test
    void rejectsMalformedWorkoutIdsBeforeQueryingPostgres() {
        assertThatThrownBy(() -> nutritionService.listOwnedWorkoutTips("not-a-uuid"))
                .isInstanceOf(com.fitness.exception.BusinessException.class)
                .extracting(exception -> ((com.fitness.exception.BusinessException) exception).getErrorCode())
                .isEqualTo(com.fitness.exception.ErrorCode.WORKOUT_RESOURCE_NOT_FOUND);
        verify(workoutMapper, never()).findAnyOwnedById(any(), any());
    }

    @Test
    void returnsPersistedSnapshotWhenInsertConflictsWithExistingTip() {
        NutritionRule rule = rule(Map.of("protein", target(1.8, 120, "GRAMS")));
        NutritionTip persisted = persistedTip(WORKOUT_ID, NutritionTiming.PRE_WORKOUT);
        persisted.setId("persisted-tip-id");
        persisted.setWeightKgSnapshot(new BigDecimal("55.0"));
        when(nutritionMapper.findMatchingRule(
                org.mockito.ArgumentMatchers.eq(Goal.MUSCLE_GAIN),
                org.mockito.ArgumentMatchers.eq(TrainingDayFocus.PUSH),
                org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(2) == NutritionTiming.PRE_WORKOUT
                        ? rule : null);
        when(nutritionMapper.insertTip(any())).thenReturn(0);
        when(nutritionMapper.findByWorkoutId(WORKOUT_ID)).thenReturn(List.of(persisted));

        List<NutritionTipResponse> result = nutritionService.generateForWorkout(
                workout(), profile(new BigDecimal("60.0")));

        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.tipId()).isEqualTo("persisted-tip-id");
            assertThat(response.weightKgSnapshot()).isEqualByComparingTo("55.0");
        });
    }

    private Workout workout() {
        Workout workout = new Workout();
        workout.setId(WORKOUT_ID);
        workout.setFocus(TrainingDayFocus.PUSH);
        return workout;
    }

    private UserProfile profile(BigDecimal weightKg) {
        UserProfile profile = new UserProfile();
        profile.setGoal(Goal.MUSCLE_GAIN);
        profile.setWeightKg(weightKg);
        return profile;
    }

    private NutritionRule rule(Map<String, Object> formula) {
        NutritionRule rule = new NutritionRule();
        rule.setId(RULE_ID);
        rule.setFormula(formula);
        rule.setNote("训练前补充能量");
        rule.setNoteEn("Fuel before training");
        rule.setVersion(3);
        return rule;
    }

    private Map<String, Object> target(double perKg, int absolute, String unit) {
        return Map.of("perKg", perKg, "absolute", absolute, "unit", unit);
    }

    private NutritionTip persistedTip(String workoutId, NutritionTiming timing) {
        NutritionTip tip = new NutritionTip();
        tip.setId("tip-" + workoutId + timing);
        tip.setWorkoutId(workoutId);
        tip.setTiming(timing);
        tip.setMacroTargets(Map.of(
                "protein", Map.of("value", 100, "unit", "GRAMS", "basis", "ABSOLUTE")));
        tip.setRuleId(RULE_ID);
        tip.setRuleVersion(1);
        return tip;
    }
}
