package com.shopping.cart.repository;

import com.shopping.cart.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Profile findByUserId(UUID userId);
}
