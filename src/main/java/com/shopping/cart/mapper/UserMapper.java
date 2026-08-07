package com.shopping.cart.mapper;

import com.shopping.cart.dto.request.RegisterAdminRequest;
import com.shopping.cart.dto.request.RegisterUserRequest;
import com.shopping.cart.entity.User;

public class UserMapper {
    public static User fromRegisterUserRequest(RegisterUserRequest registerUserRequest) {
        User user = new User();
        user.setFirstName(registerUserRequest.getFirstName());
        user.setLastName(registerUserRequest.getLastName());
        user.setUsername(registerUserRequest.getUsername());
        user.setEmail(registerUserRequest.getEmail());
        user.setPassword(registerUserRequest.getPassword());
        return user;
    }

    public static User fromRegisterAdminRequest(RegisterAdminRequest registerAdminRequest) {
        User user = new User();
        user.setFirstName(registerAdminRequest.getFirstName());
        user.setLastName(registerAdminRequest.getLastName());
        user.setUsername(registerAdminRequest.getUsername());
        user.setEmail(registerAdminRequest.getEmail());
        user.setPassword(registerAdminRequest.getPassword());
        return user;
    }
}
