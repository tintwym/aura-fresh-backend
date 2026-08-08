package com.shopping.cart.service.social;

/** Verified identity claims from a social IdP. */
public record SocialIdentity(
        String subject,
        String email,
        String firstName,
        String lastName,
        boolean emailVerified
) {}
