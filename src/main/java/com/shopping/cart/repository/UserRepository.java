package com.shopping.cart.repository;

import com.shopping.cart.entity.AuthProvider;
import com.shopping.cart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);

    User findByEmail(String email);

    User findByEmailIgnoreCase(String email);

    Optional<User> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
