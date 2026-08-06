package com.fitness.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OnDemandDraftCleanupJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnDemandDraftCleanupJob.class);

    private final OnDemandWorkoutService workoutService;

    public OnDemandDraftCleanupJob(OnDemandWorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @Scheduled(cron = "${fitness.workouts.draft-cleanup-cron:0 15 * * * *}")
    public void cleanup() {
        LOGGER.info("Confirming cleanup of expired, never-started on-demand workout drafts");
        int deleted = workoutService.cleanupExpiredDrafts();
        LOGGER.info("Expired on-demand workout draft cleanup removed {} records", deleted);
    }
}
