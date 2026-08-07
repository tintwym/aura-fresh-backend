package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.LoginAdminRequest;
import com.shopping.cart.dto.request.LoginUserRequest;
import com.shopping.cart.dto.request.RegisterUserRequest;
import com.shopping.cart.dto.response.AuthResponse;
import com.shopping.cart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final UserService userService;

    public AuthApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        AuthResponse authResponse = userService.registerUser(registerUserRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginUserRequest loginUserRequest) {
        AuthResponse authResponse = userService.loginUser(loginUserRequest);
        if (authResponse == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/admins/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginAdminRequest loginAdminRequest) {
        AuthResponse authResponse = userService.loginAdmin(loginAdminRequest);
        if (authResponse == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authResponse);
    }
}
