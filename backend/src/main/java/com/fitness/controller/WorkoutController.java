package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.dto.GenerateOnDemandWorkoutRequest;
import com.fitness.dto.NutritionTipResponse;
import com.fitness.dto.OnDemandWorkoutResponse;
import com.fitness.service.NutritionService;
import com.fitness.service.OnDemandWorkoutService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/workouts")
@CrossOrigin(origins = "*")
public class WorkoutController {
    private final OnDemandWorkoutService workoutService;
    private final NutritionService nutritionService;

    public WorkoutController(OnDemandWorkoutService workoutService, NutritionService nutritionService) {
        this.workoutService = workoutService;
        this.nutritionService = nutritionService;
    }

    @PostMapping("/on-demand")
    public ResponseEntity<ApiResponse<OnDemandWorkoutResponse>> generate(
            @Valid @RequestBody GenerateOnDemandWorkoutRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.generate(request)));
    }

    @PostMapping("/{workoutId}/start")
    public ResponseEntity<ApiResponse<OnDemandWorkoutResponse>> start(
            @PathVariable @NotBlank String workoutId
    ) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.start(workoutId)));
    }

    @PostMapping("/{workoutId}/complete")
    public ResponseEntity<ApiResponse<OnDemandWorkoutResponse>> complete(
            @PathVariable @NotBlank String workoutId
    ) {
        return ResponseEntity.ok(ApiResponse.success(workoutService.complete(workoutId)));
    }

    @GetMapping("/{workoutId}/nutrition-tips")
    public ResponseEntity<ApiResponse<List<NutritionTipResponse>>> nutritionTips(
            @PathVariable @NotBlank String workoutId
    ) {
        return ResponseEntity.ok(ApiResponse.success(nutritionService.listOwnedWorkoutTips(workoutId)));
    }
}
