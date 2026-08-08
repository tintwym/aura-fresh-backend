package com.shopping.cart.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SocialLoginRequest {
    @NotBlank
    private String provider;

    @NotBlank
    private String idToken;

    /** Optional — Apple only returns the person's name on the first authorization. */
    private String firstName;

    private String lastName;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
