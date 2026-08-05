package com.fitness.dto;

import com.fitness.domain.PlanStatus;

public record PlanLifecycleResponse(
        String planId,
        PlanStatus previousStatus,
        PlanStatus currentStatus,
        String childPlanId,
        boolean changed
) {
}
