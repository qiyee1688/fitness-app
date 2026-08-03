package com.fitness.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;
import com.fitness.dto.UserProfileRequest;
import com.fitness.dto.UserProfileResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.service.UserService;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Mapper.class)
)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void getProfileReturnsUnifiedSuccessResponse() throws Exception {
        when(userService.getProfileByUsername("demo")).thenReturn(createResponse());

        mockMvc.perform(get("/users/profile").param("username", "demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.username").value("demo"))
                .andExpect(jsonPath("$.data.fitnessLevel").value("BEGINNER"))
                .andExpect(jsonPath("$.data.availableEquipment[0]").value("body weight"));

        verify(userService).getProfileByUsername("demo");
    }

    @Test
    void getProfileReturnsUnifiedNotFoundResponse() throws Exception {
        when(userService.getProfileByUsername("missing"))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/users/profile").param("username", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40402))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void saveProfileReturnsUnifiedSuccessResponse() throws Exception {
        UserProfileRequest request = createRequest();
        when(userService.saveProfile(request)).thenReturn(createResponse());

        mockMvc.perform(post("/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.goal").value("GENERAL_FITNESS"))
                .andExpect(jsonPath("$.data.daysPerWeek").value(3));

        verify(userService).saveProfile(request);
    }

    @Test
    void saveProfileRejectsInvalidDaysPerWeek() throws Exception {
        UserProfileRequest request = new UserProfileRequest(
                "demo",
                "demo@fitness.local",
                FitnessLevel.BEGINNER,
                Goal.GENERAL_FITNESS,
                1,
                List.of("body weight")
        );

        mockMvc.perform(post("/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private UserProfileRequest createRequest() {
        return new UserProfileRequest(
                "demo",
                "demo@fitness.local",
                FitnessLevel.BEGINNER,
                Goal.GENERAL_FITNESS,
                3,
                List.of("body weight")
        );
    }

    private UserProfileResponse createResponse() {
        return new UserProfileResponse(
                "11111111-1111-1111-1111-111111111111",
                "demo",
                "demo@fitness.local",
                "22222222-2222-2222-2222-222222222222",
                FitnessLevel.BEGINNER,
                Goal.GENERAL_FITNESS,
                3,
                List.of("body weight"),
                null,
                null
        );
    }
}
