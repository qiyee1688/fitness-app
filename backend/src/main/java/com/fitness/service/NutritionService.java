package com.fitness.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fitness.domain.MacroTarget;
import com.fitness.domain.MacroTargetBasis;
import com.fitness.domain.MacroTargetValue;
import com.fitness.domain.NutritionRule;
import com.fitness.domain.NutritionTiming;
import com.fitness.domain.NutritionTip;
import com.fitness.domain.NutritionUnit;
import com.fitness.domain.TrainingDayFocus;
import com.fitness.domain.UserProfile;
import com.fitness.domain.Workout;
import com.fitness.dto.NutritionTipResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.NutritionMapper;
import com.fitness.mapper.WorkoutMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NutritionService {
    private final CurrentUserProvider currentUserProvider;
    private final NutritionMapper nutritionMapper;
    private final WorkoutMapper workoutMapper;
    private final ObjectMapper objectMapper;

    public NutritionService(
            CurrentUserProvider currentUserProvider,
            NutritionMapper nutritionMapper,
            WorkoutMapper workoutMapper,
            ObjectMapper objectMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.nutritionMapper = nutritionMapper;
        this.workoutMapper = workoutMapper;
        this.objectMapper = objectMapper;
    }

    public List<NutritionTipResponse> generateForWorkout(Workout workout, UserProfile profile) {
        if (profile == null || profile.getGoal() == null) {
            return List.of();
        }

        List<NutritionTipResponse> generated = new ArrayList<>();
        for (NutritionTiming timing : NutritionTiming.values()) {
            NutritionRule rule = nutritionMapper.findMatchingRule(
                    profile.getGoal(), workout.getFocus(), timing);
            if (rule == null) {
                continue;
            }
            MacroTarget macroTarget = calculate(rule.getFormula(), profile.getWeightKg());
            if (macroTarget.isEmpty()) {
                System.out.printf(
                        "NutritionTip skipped: ruleId=%s workoutId=%s has no calculable targets%n",
                        rule.getId(), workout.getId());
                continue;
            }

            NutritionTip tip = new NutritionTip();
            tip.setId(UUID.randomUUID().toString());
            tip.setWorkoutId(workout.getId());
            tip.setTiming(timing);
            tip.setMacroTargets(objectMapper.convertValue(
                    macroTarget, new TypeReference<Map<String, Object>>() { }));
            tip.setNote(rule.getNote());
            tip.setNoteEn(rule.getNoteEn());
            tip.setRuleId(rule.getId());
            tip.setRuleVersion(rule.getVersion());
            tip.setWeightKgSnapshot(profile.getWeightKg());
            if (nutritionMapper.insertTip(tip) == 1) {
                generated.add(toResponse(tip));
            } else {
                nutritionMapper.findByWorkoutId(workout.getId()).stream()
                        .filter(existing -> existing.getTiming() == timing)
                        .findFirst()
                        .map(this::toResponse)
                        .ifPresent(generated::add);
            }
        }
        return List.copyOf(generated);
    }

    @Transactional(readOnly = true)
    public List<NutritionTipResponse> listOwnedWorkoutTips(String workoutId) {
        String normalizedWorkoutId = normalizeWorkoutId(workoutId);
        String userId = currentUserProvider.requireUserId();
        if (workoutMapper.findAnyOwnedById(normalizedWorkoutId, userId) == null) {
            throw new BusinessException(ErrorCode.WORKOUT_RESOURCE_NOT_FOUND);
        }
        return nutritionMapper.findByWorkoutId(normalizedWorkoutId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<NutritionTipResponse>> listByPlanId(String planId) {
        return nutritionMapper.findByPlanId(planId).stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.groupingBy(
                        NutritionTipResponse::workoutId,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));
    }

    private MacroTarget calculate(Map<String, Object> formula, BigDecimal weightKg) {
        if (formula == null) {
            return new MacroTarget(null, null, null, null);
        }
        return new MacroTarget(
                calculateValue(formula.get("protein"), NutritionUnit.GRAMS, weightKg),
                calculateValue(formula.get("carbs"), NutritionUnit.GRAMS, weightKg),
                calculateValue(formula.get("fat"), NutritionUnit.GRAMS, weightKg),
                calculateValue(formula.get("kcal"), NutritionUnit.KILOCALORIES, weightKg));
    }

    @SuppressWarnings("unchecked")
    private MacroTargetValue calculateValue(
            Object rawSpec,
            NutritionUnit defaultUnit,
            BigDecimal weightKg
    ) {
        if (!(rawSpec instanceof Map<?, ?> rawMap)) {
            return null;
        }
        Map<String, Object> spec = (Map<String, Object>) rawMap;
        BigDecimal perKg = number(spec.get("perKg"));
        BigDecimal absolute = number(spec.get("absolute"));
        NutritionUnit unit = parseUnit(spec.get("unit"), defaultUnit);
        if (weightKg != null && perKg != null) {
            return new MacroTargetValue(
                    perKg.multiply(weightKg).setScale(1, RoundingMode.HALF_UP),
                    unit,
                    MacroTargetBasis.PER_KG_BODYWEIGHT);
        }
        if (absolute != null) {
            return new MacroTargetValue(
                    absolute.setScale(1, RoundingMode.HALF_UP),
                    unit,
                    MacroTargetBasis.ABSOLUTE);
        }
        return null;
    }

    private BigDecimal number(Object value) {
        return value instanceof Number number
                ? new BigDecimal(number.toString())
                : null;
    }

    private NutritionUnit parseUnit(Object value, NutritionUnit fallback) {
        if (value == null) {
            return fallback;
        }
        return NutritionUnit.valueOf(value.toString());
    }

    private NutritionTipResponse toResponse(NutritionTip tip) {
        return new NutritionTipResponse(
                tip.getId(),
                tip.getWorkoutId(),
                tip.getTiming(),
                objectMapper.convertValue(tip.getMacroTargets(), MacroTarget.class),
                tip.getNote(),
                tip.getNoteEn(),
                tip.getRuleId(),
                tip.getRuleVersion(),
                tip.getWeightKgSnapshot());
    }

    private String normalizeWorkoutId(String workoutId) {
        try {
            return UUID.fromString(workoutId).toString();
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BusinessException(ErrorCode.WORKOUT_RESOURCE_NOT_FOUND);
        }
    }
}
