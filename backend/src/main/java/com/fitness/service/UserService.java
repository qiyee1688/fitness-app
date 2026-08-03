package com.fitness.service;

import com.fitness.domain.User;
import com.fitness.domain.UserProfile;
import com.fitness.dto.UserProfileRequest;
import com.fitness.dto.UserProfileResponse;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private static final String LOCAL_DEV_PASSWORD_HASH = "local-dev";

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserProfileResponse getProfileByUsername(String username) {
        User user = userMapper.findUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserProfile profile = userMapper.findProfileByUserId(user.getId());
        if (profile == null) {
            throw new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND);
        }

        return toResponse(user, profile);
    }

    public UserProfileResponse saveProfile(UserProfileRequest request) {
        User user = ensureUser(request);
        UserProfile profile = userMapper.findProfileByUserId(user.getId());

        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(user.getId());
            profile.setFitnessLevel(request.fitnessLevel());
            profile.setGoal(request.goal());
            profile.setDaysPerWeek(request.daysPerWeek());
            profile.setAvailableEquipment(request.availableEquipment());
            userMapper.insertProfile(profile);
            profile = userMapper.findProfileByUserId(user.getId());
        } else {
            profile.setFitnessLevel(request.fitnessLevel());
            profile.setGoal(request.goal());
            profile.setDaysPerWeek(request.daysPerWeek());
            profile.setAvailableEquipment(request.availableEquipment());
            userMapper.updateProfile(profile);
            profile = userMapper.findProfileByUserId(user.getId());
        }

        return toResponse(user, profile);
    }

    private User ensureUser(UserProfileRequest request) {
        User user = userMapper.findUserByUsername(request.username());
        if (user != null) {
            return user;
        }

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPasswordHash(LOCAL_DEV_PASSWORD_HASH);
        userMapper.insertUser(newUser);

        User createdUser = userMapper.findUserByUsername(request.username());
        if (createdUser == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        return createdUser;
    }

    private UserProfileResponse toResponse(User user, UserProfile profile) {
        String profileId = profile == null ? null : profile.getId();
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profileId,
                profile.getFitnessLevel(),
                profile.getGoal(),
                profile.getDaysPerWeek(),
                profile.getAvailableEquipment(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
