package com.shopping.cart.service;

import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IOrderService;
import com.shopping.cart.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService implements IOrderService {
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING",
            "PROCESSING",
            "OUT_FOR_DELIVERY",
            "COMPLETED",
            "CANCELLED",
            "PAID_STOCK_SHORTAGE"
    );

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public OrderService(
            OrderRepository orderRepository,
            UserService userService,
            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(String token) {
        User user = userService.requireUser(token);
        return orderRepository.findByUserWithItems(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrdersForAdmin(String token) {
        userService.requireAdmin(token);
        return orderRepository.findAllWithItems();
    }

    @Override
    @Transactional
    public Order updateOrderStatusForAdmin(String token, UUID orderId, String status) {
        userService.requireAdmin(token);
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        // Accept UI aliases
        if ("DELIVERED".equals(normalized)) {
            normalized = "COMPLETED";
        }
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported order status: " + status);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        String previous = order.getStatus();
        order.setStatus(normalized);
        Order saved = orderRepository.save(order);
        notificationService.notifyOrderStatus(saved, previous, normalized);
        return saved;
    }
}
