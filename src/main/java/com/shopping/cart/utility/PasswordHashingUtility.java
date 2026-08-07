package com.shopping.cart.utility;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashingUtility {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // Hash a password using BCrypt
    public static String hashPassword(String plainTextPassword) {
        return encoder.encode(plainTextPassword);
    }

    // Check that an unhashed password matches one that has been hashed
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        return encoder.matches(plainTextPassword, hashedPassword);
    }
}
