package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.dto.GeneratePlanRequest;
import com.fitness.dto.ExerciseFeedbackResponse;
import com.fitness.dto.GeneratedPlanResponse;
import com.fitness.dto.PlanDetailResponse;
import com.fitness.dto.TodayWorkoutResponse;
import com.fitness.dto.SubmitExerciseFeedbackRequest;
import com.fitness.service.PlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Validated
@RequestMapping("/plans")
@CrossOrigin(origins = "*")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GeneratedPlanResponse>> generate(
            @Valid @RequestBody GeneratePlanRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(planService.generatePlan(request)));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<PlanDetailResponse>> getCurrent(
            @RequestParam(defaultValue = "demo") @NotBlank String username
    ) {
        return ResponseEntity.ok(ApiResponse.success(planService.getActivePlan(username)));
    }
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayWorkoutResponse>> getToday(
            @RequestParam(defaultValue = "demo") @NotBlank String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(planService.getTodayWorkout(username, date)));
    }

    @PostMapping("/workouts/{workoutId}/complete")
    public ResponseEntity<ApiResponse<TodayWorkoutResponse>> completeWorkout(
            @PathVariable @NotBlank String workoutId,
            @RequestParam(defaultValue = "demo") @NotBlank String username
    ) {
        return ResponseEntity.ok(ApiResponse.success(planService.completeWorkout(username, workoutId)));
    }

    @PostMapping("/workouts/{workoutId}/exercises/{exerciseId}/feedback")
    public ResponseEntity<ApiResponse<ExerciseFeedbackResponse>> submitFeedback(
            @PathVariable @NotBlank String workoutId,
            @PathVariable @NotBlank String exerciseId,
            @RequestParam(defaultValue = "demo") @NotBlank String username,
            @Valid @RequestBody SubmitExerciseFeedbackRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                planService.submitExerciseFeedback(username, workoutId, exerciseId, request)));
    }

}
