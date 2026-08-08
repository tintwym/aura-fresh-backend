package com.shopping.cart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.cart.dto.request.ChangePasswordRequest;
import com.shopping.cart.dto.request.LoginAdminRequest;
import com.shopping.cart.dto.request.LoginUserRequest;
import com.shopping.cart.dto.request.RegisterAdminRequest;
import com.shopping.cart.dto.request.RegisterUserRequest;
import com.shopping.cart.dto.response.AuthResponse;
import com.shopping.cart.entity.AuthProvider;
import com.shopping.cart.entity.Role;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IUserService;
import com.shopping.cart.mapper.UserMapper;
import com.shopping.cart.repository.RoleRepository;
import com.shopping.cart.repository.UserRepository;
import com.shopping.cart.utility.JwtUtility;
import com.shopping.cart.utility.PasswordHashingUtility;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtility jwtUtility;

    public UserService(JwtUtility jwtUtility, UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtility = jwtUtility;
    }

    @Override
    public boolean verifyToken(String token, String usernameJson) {
        String jwt = normalizeBearer(token);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(usernameJson);
            String username = jsonNode.get("username").asText();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return false;
            }
            return jwtUtility.isTokenValid(jwt, username, user.getTokenVersion());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username payload");
        }
    }

    @Override
    public User getUserFromToken(String token) {
        return requireUser(token);
    }

    @Override
    public User requireUser(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is missing or invalid");
        }
        String jwt = authorizationHeader.substring(7);
        try {
            String username = jwtUtility.extractUsername(jwt);
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
            }
            if (!jwtUtility.isTokenValid(jwt, username, user.getTokenVersion())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }
            return user;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(Objects.requireNonNull(id)).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public AuthResponse registerUser(RegisterUserRequest registerUserRequest) {
        if (registerUserRequest.getPassword() == null || registerUserRequest.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (userRepository.findByUsername(registerUserRequest.getUsername()) != null
                || userRepository.findByEmail(registerUserRequest.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already registered");
        }

        registerUserRequest.setPassword(PasswordHashingUtility.hashPassword(registerUserRequest.getPassword()));
        User user = UserMapper.fromRegisterUserRequest(registerUserRequest);
        Role role = roleRepository.findByName("User");
        user.setRole(role);
        user.setProvider(AuthProvider.LOCAL);
        user.setProviderSubject(null);
        userRepository.save(user);

        String token = jwtUtility.generateToken(user.getUsername(), user.getTokenVersion());
        return new AuthResponse(token);
    }

    @Override
    public boolean registerAdmin(RegisterAdminRequest registerAdminRequest) {
        registerAdminRequest.setPassword(PasswordHashingUtility.hashPassword(registerAdminRequest.getPassword()));
        User user = UserMapper.fromRegisterAdminRequest(registerAdminRequest);
        Role role = roleRepository.findByName("Admin");
        user.setRole(role);
        user.setProvider(AuthProvider.LOCAL);
        user.setProviderSubject(null);
        userRepository.save(user);
        return true;
    }

    @Override
    public AuthResponse loginUser(LoginUserRequest loginUserRequest) {
        User user = userRepository.findByUsername(loginUserRequest.getUsername());

        if (user == null || user.getRole() == null || user.getPassword() == null) {
            return null;
        }

        String roleName = user.getRole().getName();
        if (!"User".equalsIgnoreCase(roleName) && !"Admin".equalsIgnoreCase(roleName)) {
            return null;
        }

        if (PasswordHashingUtility.verifyPassword(loginUserRequest.getPassword(), user.getPassword())) {
            String token = jwtUtility.generateToken(user.getUsername(), user.getTokenVersion());
            return new AuthResponse(token);
        }

        return null;
    }

    @Override
    public AuthResponse loginAdmin(LoginAdminRequest loginAdminRequest) {
        User user = userRepository.findByUsername(loginAdminRequest.getUsername());
        if (user == null
                || user.getRole() == null
                || user.getPassword() == null
                || !"Admin".equalsIgnoreCase(user.getRole().getName())
                || !PasswordHashingUtility.verifyPassword(loginAdminRequest.getPassword(), user.getPassword())) {
            return null;
        }
        return new AuthResponse(jwtUtility.generateToken(user.getUsername(), user.getTokenVersion()));
    }

    @Override
    public void changePassword(String token, ChangePasswordRequest request) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()
                || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current and new password are required");
        }
        if (request.getNewPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters");
        }

        User user = requireUser(token);
        if (user.getPassword() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This account uses social sign-in and has no password to change");
        }
        if (!PasswordHashingUtility.verifyPassword(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(PasswordHashingUtility.hashPassword(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Override
    public User requireAdmin(String authorizationHeader) {
        User user = requireUser(authorizationHeader);
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return user;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getRole() != null
                && "Admin".equalsIgnoreCase(user.getRole().getName());
    }

    private static String normalizeBearer(String authorizationHeader) {
        if (authorizationHeader == null) {
            return "";
        }
        return authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }
}
