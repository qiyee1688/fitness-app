package com.fitness.mapper;

import com.fitness.domain.User;
import com.fitness.domain.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findUserByUsername(@Param("username") String username);

    User findUserById(@Param("id") String id);

    int insertUser(User user);

    UserProfile findProfileByUserId(@Param("userId") String userId);

    int insertProfile(UserProfile profile);

    int updateProfile(UserProfile profile);
}
