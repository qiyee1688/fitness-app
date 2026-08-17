package com.fitness.controller;

import com.fitness.domain.OnDemandBodyPart;
import com.fitness.domain.WorkoutSource;
import com.fitness.domain.WorkoutStatus;
import com.fitness.dto.OnDemandWorkoutResponse;
import com.fitness.exception.GlobalExceptionHandler;
import com.fitness.service.OnDemandWorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkoutControllerTest {
    private final OnDemandWorkoutService service = mock(OnDemandWorkoutService.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new WorkoutController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void generateDoesNotAcceptUserIdAndReturnsDraft() throws Exception {
        when(service.generate(any())).thenReturn(response(WorkoutStatus.DRAFT));

        mockMvc.perform(post("/workouts/on-demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bodyPart\":\"CHEST\",\"equipment\":[\"dumbbell\"],\"userId\":\"other\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void startAndCompleteUseOnlyWorkoutId() throws Exception {
        when(service.start("workout-id")).thenReturn(response(WorkoutStatus.IN_PROGRESS));
        when(service.complete("workout-id")).thenReturn(response(WorkoutStatus.COMPLETED));

        mockMvc.perform(post("/workouts/workout-id/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
        mockMvc.perform(post("/workouts/workout-id/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        verify(service).start("workout-id");
        verify(service).complete("workout-id");
    }

    @Test
    void rejectsMissingBodyPart() throws Exception {
        mockMvc.perform(post("/workouts/on-demand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private OnDemandWorkoutResponse response(WorkoutStatus status) {
        return new OnDemandWorkoutResponse(
                "workout-id", OnDemandBodyPart.CHEST, List.of("body weight"),
                WorkoutSource.ON_DEMAND, status,
                status == WorkoutStatus.IN_PROGRESS ? LocalDateTime.now() : null,
                status == WorkoutStatus.COMPLETED ? LocalDateTime.now() : null,
                LocalDateTime.now().plusHours(24), List.of());
    }
}
