package com.shopping.cart.repository;

import com.shopping.cart.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi FROM OrderItem oi "
            + "JOIN FETCH oi.order o "
            + "JOIN FETCH o.user "
            + "JOIN FETCH oi.product "
            + "WHERE oi.id = :id")
    Optional<OrderItem> findByIdWithOrder(@Param("id") UUID id);
}
