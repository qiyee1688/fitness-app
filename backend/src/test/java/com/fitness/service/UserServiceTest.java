package com.fitness.service;

import com.fitness.domain.FitnessLevel;
import com.fitness.domain.Goal;
import com.fitness.domain.User;
import com.fitness.domain.UserProfile;
import com.fitness.dto.UserProfileRequest;
import com.fitness.dto.UserProfileResponse;
import com.fitness.exception.BusinessException;
import com.fitness.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String USER_ID = "11111111-1111-1111-1111-111111111111";
    private static final String PROFILE_ID = "22222222-2222-2222-2222-222222222222";

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void saveProfileCreatesUserAndProfileWhenUserDoesNotExist() {
        User createdUser = createUser();
        UserProfile createdProfile = createProfile(FitnessLevel.BEGINNER, Goal.FAT_LOSS, 3);
        UserProfileRequest request = createRequest(FitnessLevel.BEGINNER, Goal.FAT_LOSS, 3);

        when(userMapper.findUserByUsername("demo"))
                .thenReturn(null)
                .thenReturn(createdUser);
        when(userMapper.findProfileByUserId(USER_ID))
                .thenReturn(null)
                .thenReturn(createdProfile);

        UserProfileResponse response = userService.saveProfile(request);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.profileId()).isEqualTo(PROFILE_ID);
        assertThat(response.fitnessLevel()).isEqualTo(FitnessLevel.BEGINNER);
        assertThat(response.goal()).isEqualTo(Goal.FAT_LOSS);
        assertThat(response.availableEquipment()).containsExactly("body weight", "dumbbell");
        verify(userMapper).insertUser(any(User.class));
        verify(userMapper).insertProfile(any(UserProfile.class));
    }

    @Test
    void saveProfileUpdatesExistingProfile() {
        User user = createUser();
        UserProfile existingProfile = createProfile(FitnessLevel.BEGINNER, Goal.FAT_LOSS, 3);
        UserProfile updatedProfile = createProfile(FitnessLevel.INTERMEDIATE, Goal.MUSCLE_GAIN, 4);
        UserProfileRequest request = createRequest(FitnessLevel.INTERMEDIATE, Goal.MUSCLE_GAIN, 4);

        when(userMapper.findUserByUsername("demo")).thenReturn(user);
        when(userMapper.findProfileByUserId(USER_ID))
                .thenReturn(existingProfile)
                .thenReturn(updatedProfile);

        UserProfileResponse response = userService.saveProfile(request);

        assertThat(response.fitnessLevel()).isEqualTo(FitnessLevel.INTERMEDIATE);
        assertThat(response.goal()).isEqualTo(Goal.MUSCLE_GAIN);
        assertThat(response.daysPerWeek()).isEqualTo(4);
        verify(userMapper).updateProfile(existingProfile);
    }

    @Test
    void getProfileByUsernameThrowsWhenUserDoesNotExist() {
        when(userMapper.findUserByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> userService.getProfileByUsername("missing"))
                .isInstanceOf(BusinessException.class);
    }

    private UserProfileRequest createRequest(FitnessLevel fitnessLevel, Goal goal, int daysPerWeek) {
        return new UserProfileRequest("demo", "demo@fitness.local", fitnessLevel, goal, daysPerWeek, List.of("body weight", "dumbbell"));
    }

    private User createUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("demo");
        user.setEmail("demo@fitness.local");
        return user;
    }

    private UserProfile createProfile(FitnessLevel fitnessLevel, Goal goal, int daysPerWeek) {
        UserProfile profile = new UserProfile();
        profile.setId(PROFILE_ID);
        profile.setUserId(USER_ID);
        profile.setFitnessLevel(fitnessLevel);
        profile.setGoal(goal);
        profile.setDaysPerWeek(daysPerWeek);
        profile.setAvailableEquipment(List.of("body weight", "dumbbell"));
        return profile;
    }
}
