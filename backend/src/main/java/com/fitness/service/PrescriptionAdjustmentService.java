package com.fitness.service;

import com.fitness.domain.ExerciseFeedback;
import com.fitness.domain.Plan;
import com.fitness.domain.Prescription;
import com.fitness.domain.PrescriptionAdjustment;
import com.fitness.domain.PrescriptionAdjustmentStatus;
import com.fitness.mapper.PrescriptionAdjustmentMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PrescriptionAdjustmentService {
    private static final BigDecimal RPE_STEP = new BigDecimal("0.5");

    private final PrescriptionAdjustmentMapper adjustmentMapper;

    public PrescriptionAdjustmentService(PrescriptionAdjustmentMapper adjustmentMapper) {
        this.adjustmentMapper = adjustmentMapper;
    }

    public void createCandidateIfTriggered(Plan plan, ExerciseFeedback newestFeedback, int sourceDayNumber) {
        List<ExerciseFeedback> recent = adjustmentMapper.findRecentUnconsumedFeedbacks(
                plan.getId(), newestFeedback.getExerciseId());
        if (recent.size() != 2 || !isConsecutiveTrigger(recent)) {
            return;
        }
        Prescription target = adjustmentMapper.findNextUnstartedPrescription(
                plan.getId(), newestFeedback.getExerciseId(), sourceDayNumber);
        PrescriptionAdjustment adjustment = new PrescriptionAdjustment();
        adjustment.setId(UUID.randomUUID().toString());
        adjustment.setPlanId(plan.getId());
        adjustment.setSourceWorkoutId(newestFeedback.getWorkoutId());
        adjustment.setSourceExerciseId(newestFeedback.getExerciseId());
        adjustment.setFirstFeedbackId(recent.get(1).getId());
        adjustment.setSecondFeedbackId(recent.getFirst().getId());
        adjustment.setCreatedAt(LocalDateTime.now());
        if (target == null) {
            adjustment.setStatus(PrescriptionAdjustmentStatus.EXPIRED);
            adjustment.setProcessedAt(adjustment.getCreatedAt());
            adjustment.setReason("连续两次反馈相同，但当前计划中没有下一次未开始训练可调整。");
            adjustment.setReasonEn("Two matching feedbacks were recorded, but this plan has no next unstarted workout to adjust.");
        } else {
            boolean tooEasy = "TOO_EASY".equals(recent.getFirst().getFeedbackType());
            adjustment.setTargetWorkoutId(target.getWorkoutId());
            adjustment.setTargetPrescriptionId(target.getId());
            adjustment.setOriginalPrescription(snapshot(target));
            String substituteId = tooEasy ? null : adjustmentMapper.findAllowedHardFeedbackSubstitute(
                    plan.getId(), target.getWorkoutId(), target.getExerciseId());
            adjustment.setSuggestedExerciseId(substituteId);
            adjustment.setSuggestedPrescription(substituteId == null
                    ? suggestedSnapshot(plan, target, tooEasy)
                    : substituteSnapshot(target, substituteId));
            adjustment.setStatus(PrescriptionAdjustmentStatus.PENDING);
            adjustment.setReason(tooEasy
                    ? "同一动作连续两次反馈偏轻松，建议下次训练小幅提高 RPE。"
                    : substituteId == null
                    ? "同一动作连续两次反馈偏困难，建议下次训练小幅降低 RPE。"
                    : "同一动作连续两次反馈偏困难，建议下次训练替换为当前器械可用的替代动作。");
            adjustment.setReasonEn(tooEasy
                    ? "Two consecutive responses said this exercise was too easy; slightly raise RPE next time."
                    : substituteId == null
                    ? "Two consecutive responses said this exercise was too hard; slightly lower RPE next time."
                    : "Two consecutive responses said this exercise was too hard; use an available substitute next time.");
        }
        adjustmentMapper.insert(adjustment);
    }

    private boolean isConsecutiveTrigger(List<ExerciseFeedback> recent) {
        String type = recent.getFirst().getFeedbackType();
        return ("TOO_EASY".equals(type) || "TOO_HARD".equals(type))
                && type.equals(recent.get(1).getFeedbackType());
    }

    private Map<String, Object> snapshot(Prescription prescription) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("exerciseId", prescription.getExerciseId());
        snapshot.put("sets", prescription.getSets());
        snapshot.put("reps", prescription.getReps());
        snapshot.put("load", prescription.getLoad());
        snapshot.put("loadType", prescription.getLoadType() == null ? null : prescription.getLoadType().name());
        snapshot.put("rpe", prescription.getRpe());
        return snapshot;
    }

    private Map<String, Object> suggestedSnapshot(Plan plan, Prescription target, boolean tooEasy) {
        Map<String, Object> suggested = snapshot(target);
        BigDecimal current = target.getRpe() == null ? new BigDecimal("7.0") : target.getRpe();
        suggested.put("rpe", current.add(tooEasy ? RPE_STEP : RPE_STEP.negate())
                .max(minRpe(plan)).min(maxRpe(plan)).setScale(1, RoundingMode.HALF_UP));
        return suggested;
    }

    private Map<String, Object> substituteSnapshot(Prescription target, String substituteId) {
        Map<String, Object> suggested = snapshot(target);
        suggested.put("exerciseId", substituteId);
        return suggested;
    }

    private BigDecimal minRpe(Plan plan) {
        return "ADVANCED".equals(plan.getProfileSnapshot() == null ? null : plan.getProfileSnapshot().get("fitnessLevel"))
                ? new BigDecimal("7.0") : new BigDecimal("6.0");
    }

    private BigDecimal maxRpe(Plan plan) {
        Object level = plan.getProfileSnapshot() == null ? null : plan.getProfileSnapshot().get("fitnessLevel");
        if ("BEGINNER".equals(level)) return new BigDecimal("7.5");
        return "INTERMEDIATE".equals(level) ? new BigDecimal("8.5") : new BigDecimal("9.0");
    }
}
