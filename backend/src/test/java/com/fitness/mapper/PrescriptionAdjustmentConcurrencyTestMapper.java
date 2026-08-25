package com.fitness.mapper;

import org.apache.ibatis.annotations.Param;

interface PrescriptionAdjustmentConcurrencyTestMapper {
    void dropAdjustments();

    void dropPlans();

    void dropAdjustmentStatusType();

    void dropPlanStatusType();

    void createPlanStatusType();

    void createAdjustmentStatusType();

    void createPlans();

    void createAdjustments();

    void insertPlan(@Param("planId") String planId, @Param("userId") String userId);

    void insertPendingAdjustment(@Param("adjustmentId") String adjustmentId, @Param("planId") String planId);
}
