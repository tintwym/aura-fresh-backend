package com.shopping.cart.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile("123 Main St", "Apt 4", "B", "2", "Springfield", "IL", "USA", "62701");
    }

    @Test
    void testProfileCreation() {
        assertNotNull(profile);
        assertEquals("123 Main St", profile.getAddress1());
        assertEquals("Apt 4", profile.getAddress2());
        assertEquals("B", profile.getUnit());
        assertEquals("2", profile.getFloor());
        assertEquals("Springfield", profile.getCity());
        assertEquals("IL", profile.getState());
        assertEquals("USA", profile.getCountry());
        assertEquals("62701", profile.getZipCode());
    }

    @Test
    void testSetters() {
        profile.setAddress1("456 Elm St");
        profile.setAddress2("Suite 7");
        profile.setUnit("C");
        profile.setFloor("3");
        profile.setCity("Chicago");
        profile.setState("IL");
        profile.setCountry("USA");
        profile.setZipCode("60601");

        assertEquals("456 Elm St", profile.getAddress1());
        assertEquals("Suite 7", profile.getAddress2());
        assertEquals("C", profile.getUnit());
        assertEquals("3", profile.getFloor());
        assertEquals("Chicago", profile.getCity());
        assertEquals("IL", profile.getState());
        assertEquals("USA", profile.getCountry());
        assertEquals("60601", profile.getZipCode());
    }

    @Test
    void testUserAssociation() {
        User user = new User();
        profile.setUser(user);

        assertNotNull(profile.getUser());
        assertEquals(user, profile.getUser());
    }

    @Test
    void testDefaultConstructor() {
        Profile defaultProfile = new Profile();
        assertNotNull(defaultProfile);
        assertNull(defaultProfile.getAddress1());
        assertNull(defaultProfile.getAddress2());
        assertNull(defaultProfile.getUnit());
        assertNull(defaultProfile.getFloor());
        assertNull(defaultProfile.getCity());
        assertNull(defaultProfile.getState());
        assertNull(defaultProfile.getCountry());
        assertNull(defaultProfile.getZipCode());
        assertNull(defaultProfile.getUser());
    }
}
