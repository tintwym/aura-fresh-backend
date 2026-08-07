package com.shopping.cart.controller.api;

import com.shopping.cart.entity.Order;
import com.shopping.cart.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderApiController {
    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/history")
    public List<Order> getOrderHistory(@RequestHeader("Authorization") String token) {
        return orderService.getOrderHistory(token);
    }

    @GetMapping("/admin")
    public List<Order> getAllOrdersForAdmin(@RequestHeader("Authorization") String token) {
        return orderService.getAllOrdersForAdmin(token);
    }

    @PutMapping("/admin/{id}/status")
    public Order updateOrderStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body) {
        return orderService.updateOrderStatusForAdmin(token, id, body.get("status"));
    }
}
