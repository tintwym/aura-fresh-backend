package com.shopping.cart.repository;

import com.shopping.cart.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProductId(UUID productId);

    boolean existsByOrderItem_Id(UUID orderItemId);

    @Query("SELECT r.product.id, AVG(r.rating), COUNT(r) FROM Review r GROUP BY r.product.id")
    List<Object[]> averageRatingByProduct();
}
