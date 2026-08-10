package com.fitness.service;

import com.fitness.domain.User;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.UserMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class DemoCurrentUserProvider implements CurrentUserProvider {
    private final UserMapper userMapper;

    public DemoCurrentUserProvider(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public String requireUserId() {
        User user = userMapper.findUserByUsername("demo");
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}
