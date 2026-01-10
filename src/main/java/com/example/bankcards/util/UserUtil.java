package com.example.bankcards.util;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class UserUtil {

    public static UserResponse toDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }

    public static User toEntity(UserCreateRequest dto) {
        return User.builder()
                .username(dto.getUsername())
                .build();
    }

}