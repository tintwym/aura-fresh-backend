package com.shopping.cart.controller.api;

import com.shopping.cart.dto.response.NotificationResponse;
import com.shopping.cart.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final NotificationService notificationService;

    public NotificationApiController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(@RequestHeader("Authorization") String token) {
        return notificationService.listForUser(token);
    }

    @PostMapping("/read-all")
    public Map<String, String> markAllRead(@RequestHeader("Authorization") String token) {
        notificationService.markAllRead(token);
        return Map.of("status", "ok");
    }
}
