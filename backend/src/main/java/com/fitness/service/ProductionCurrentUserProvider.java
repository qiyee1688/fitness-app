package com.fitness.service;

import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionCurrentUserProvider implements CurrentUserProvider {
    @Override
    public String requireUserId() {
        throw new BusinessException(ErrorCode.AUTHENTICATION_REQUIRED);
    }
}
