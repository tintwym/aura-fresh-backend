package com.shopping.cart.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;
    private Role role;
    private Profile profile;

    @BeforeEach
    void setUp() {
        role = new Role("User");
        profile = new Profile("123 Main St", "Apt 4", "B", "2", "Springfield", "IL", "USA", "62701");
        user = new User("John", "Doe", "johndoe", "john@example.com", "password123", role, profile);
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertNotNull(user.getRole());
        assertEquals("User", user.getRole().getName());
        assertNotNull(user.getProfile());
        assertEquals("123 Main St", user.getProfile().getAddress1());
    }

    @Test
    void testSetters() {
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setUsername("janesmith");
        user.setEmail("jane@example.com");
        user.setPassword("newpassword");

        Role newRole = new Role("Admin");
        user.setRole(newRole);

        Profile newProfile = new Profile("456 Elm St", "Suite 7", "C", "3", "Chicago", "IL", "USA", "60601");
        user.setProfile(newProfile);

        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("janesmith", user.getUsername());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("Admin", user.getRole().getName());
        assertEquals("456 Elm St", user.getProfile().getAddress1());
    }

    @Test
    void testRoleAssociation() {
        assertNotNull(user.getRole());
        assertEquals("User", user.getRole().getName());

        Role newRole = new Role("Admin");
        user.setRole(newRole);
        assertEquals("Admin", user.getRole().getName());
    }

    @Test
    void testProfileAssociation() {
        assertNotNull(user.getProfile());
        assertEquals("123 Main St", user.getProfile().getAddress1());
        assertEquals("62701", user.getProfile().getZipCode());

        Profile newProfile = new Profile("789 Oak St", null, "D", "1", "New York", "NY", "USA", "10001");
        user.setProfile(newProfile);
        assertEquals("789 Oak St", user.getProfile().getAddress1());
        assertEquals("10001", user.getProfile().getZipCode());
    }

    @Test
    void testDefaultConstructor() {
        User defaultUser = new User();
        assertNotNull(defaultUser);
        assertNull(defaultUser.getFirstName());
        assertNull(defaultUser.getLastName());
        assertNull(defaultUser.getUsername());
        assertNull(defaultUser.getEmail());
        assertNull(defaultUser.getPassword());
        assertNull(defaultUser.getRole());
        assertNull(defaultUser.getProfile());
    }
}
