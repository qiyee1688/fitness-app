package com.fitness.dto;

import jakarta.validation.constraints.Min;

public record ResolvePrescriptionAdjustmentRequest(@Min(0) int expectedPlanVersion) {}
