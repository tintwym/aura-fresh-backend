package com.shopping.cart.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role("Admin");
    }

    @Test
    void testRoleCreation() {
        assertNotNull(role);
        assertEquals("Admin", role.getName());
    }

    @Test
    void testSetters() {
        role.setName("User");
        assertEquals("User", role.getName());
    }

    @Test
    void testDefaultConstructor() {
        Role defaultRole = new Role();
        assertNotNull(defaultRole);
        assertNull(defaultRole.getName());
    }
}
