package com.shopping.cart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_provider_subject",
                columnNames = {"provider", "provider_subject"}
        )
)
public class User extends BaseEntity {
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    /** Null for social-only accounts (no local password). */
    @JsonIgnore
    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private AuthProvider provider = AuthProvider.LOCAL;

    /** Stable subject from the IdP (Google `sub`, Apple `sub`). */
    @Column(name = "provider_subject")
    private String providerSubject;

    /** Incremented on password change so existing JWTs are rejected. */
    @Column(nullable = false)
    private long tokenVersion = 0L;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    // Default constructor is required by JPA
    public User() {}

    // Parameterized constructor
    public User(String firstName, String lastName, String username, String email, String password, Role role, Profile profile) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profile = profile;
        this.provider = AuthProvider.LOCAL;
    }

    @PrePersist
    void ensureAuthProvider() {
        if (provider == null) {
            provider = AuthProvider.LOCAL;
        }
    }
}
