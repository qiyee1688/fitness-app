package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.dto.CreateWorkoutTemplateRequest;
import com.fitness.dto.UpdateWorkoutTemplateRequest;
import com.fitness.dto.WorkoutTemplateResponse;
import com.fitness.service.WorkoutTemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/workout-templates")
@CrossOrigin(origins = "*")
public class WorkoutTemplateController {
    private final WorkoutTemplateService templateService;

    public WorkoutTemplateController(WorkoutTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutTemplateResponse>> create(
            @Valid @RequestBody CreateWorkoutTemplateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkoutTemplateResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(templateService.list()));
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<ApiResponse<WorkoutTemplateResponse>> update(
            @PathVariable @NotBlank String templateId,
            @Valid @RequestBody UpdateWorkoutTemplateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.update(templateId, request)));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @NotBlank String templateId) {
        templateService.delete(templateId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
