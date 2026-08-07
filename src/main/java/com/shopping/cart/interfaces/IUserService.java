package com.shopping.cart.interfaces;

import com.shopping.cart.dto.request.ChangePasswordRequest;
import com.shopping.cart.dto.request.LoginAdminRequest;
import com.shopping.cart.dto.request.LoginUserRequest;
import com.shopping.cart.dto.request.RegisterAdminRequest;
import com.shopping.cart.dto.request.RegisterUserRequest;
import com.shopping.cart.dto.response.AuthResponse;
import com.shopping.cart.entity.User;

import java.util.UUID;

public interface IUserService {
    boolean verifyToken(String token, String username);
    User getUserFromToken(String token);
    User getUserById(UUID id);
    User getUserByUsername(String username);
    AuthResponse registerUser(RegisterUserRequest registerUserRequest);
    boolean registerAdmin(RegisterAdminRequest registerAdminRequest);
    AuthResponse loginUser(LoginUserRequest loginUserRequest);
    AuthResponse loginAdmin(LoginAdminRequest loginAdminRequest);
    void changePassword(String token, ChangePasswordRequest request);
    User requireUser(String authorizationHeader);
    User requireAdmin(String authorizationHeader);
    boolean isAdmin(User user);
}
