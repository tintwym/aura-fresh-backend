package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.ChangePasswordRequest;
import com.shopping.cart.entity.User;
import com.shopping.cart.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody String usernameJson) {
        // Check if the token is present
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is missing or invalid");
        }

        // Verify the token
        boolean isValid = userService.verifyToken(token, usernameJson);

        // Return a 200 OK response if the token is valid, otherwise return a 401 Unauthorized response
        return isValid ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/token")
    public ResponseEntity<User> getUserFromToken(@RequestHeader(value = "Authorization", required = false) String token) {
        User user = userService.requireUser(token);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody ChangePasswordRequest request) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is missing or invalid");
        }
        userService.changePassword(token, request);
        return ResponseEntity.ok().build();
    }
}
