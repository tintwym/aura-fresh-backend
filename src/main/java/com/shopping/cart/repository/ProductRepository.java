package com.shopping.cart.repository;

import com.shopping.cart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    // Custom query to fetch only products where isDeleted is false
    List<Product> findByIsDeletedFalse();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findActiveByIdWithImages(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.isDeleted = false")
    List<Product> findByIsDeletedFalseWithImages();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images")
    List<Product> findAllWithImages();

    Optional<Product> findByStripePriceId(String stripePriceId);

    Optional<Product> findByStripeProductId(String stripeProductId);

    Optional<Product> findByNameIgnoreCase(String name);
}
