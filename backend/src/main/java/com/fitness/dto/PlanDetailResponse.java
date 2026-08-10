package com.fitness.dto;

import com.fitness.domain.LoadType;
import com.fitness.domain.PlanStatus;
import com.fitness.domain.TrainingDayFocus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record PlanDetailResponse(
        String planId,
        PlanStatus status,
        LocalDate startDate,
        LocalDate endDate,
        int totalWeeks,
        Map<String, Object> profileSnapshot,
        List<WorkoutDetail> workouts
) {
    public record WorkoutDetail(
            String workoutId,
            int dayNumber,
            int weekNumber,
            LocalDate scheduledDate,
            TrainingDayFocus focus,
            List<PrescriptionDetail> prescriptions
    ) {
    }

    public record PrescriptionDetail(
            String prescriptionId,
            int sequence,
            int sets,
            int reps,
            BigDecimal load,
            LoadType loadType,
            BigDecimal rpe,
            ExerciseSummary exercise
    ) {
    }

    public record ExerciseSummary(
            String id,
            String name,
            String bodyPart,
            String target,
            String equipment,
            String gifUrl,
            String imageUrl,
            String coachCue,
            String coachCueEn
    ) {
        public ExerciseSummary(
                String id,
                String name,
                String bodyPart,
                String target,
                String equipment,
                String gifUrl,
                String imageUrl
        ) {
            this(id, name, bodyPart, target, equipment, gifUrl, imageUrl, null, null);
        }
    }
}
