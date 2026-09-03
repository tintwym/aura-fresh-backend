package com.shopping.cart.service;

import com.shopping.cart.dto.response.NotificationResponse;
import com.shopping.cart.entity.Notification;
import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.User;
import com.shopping.cart.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Transactional
    public void notifyOrderStatus(Order order, String previousStatus, String newStatus) {
        if (order.getUser() == null) {
            return;
        }
        if (previousStatus != null && previousStatus.equalsIgnoreCase(newStatus)) {
            return;
        }

        String title = titleForStatus(previousStatus, newStatus);
        String message = messageForStatus(order, previousStatus, newStatus);

        notificationRepository.save(new Notification(
                order.getUser(),
                title,
                message,
                "order",
                order.getId()));

        String email = order.getUser().getEmail();
        if (email != null && !email.isBlank()) {
            log.info("Order status notify → {} | {} | {}", email, title, message);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(String token) {
        User user = userService.requireUser(token);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAllRead(String token) {
        User user = userService.requireUser(token);
        List<Notification> list = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
            }
        }
        notificationRepository.saveAll(list);
    }

    private static String titleForStatus(String previous, String status) {
        String s = (status == null ? "" : status).toUpperCase();
        boolean firstConfirm = previous == null && "COMPLETED".equals(s);
        return switch (s) {
            case "PROCESSING" -> "Order is being prepared";
            case "OUT_FOR_DELIVERY" -> "Your groceries are on the way";
            case "COMPLETED" -> firstConfirm ? "Payment confirmed" : "Order delivered";
            case "CANCELLED" -> "Order cancelled";
            case "PAID_STOCK_SHORTAGE" -> "Order needs attention";
            case "PENDING" -> "Order received";
            default -> "Order update";
        };
    }

    private static String messageForStatus(Order order, String previous, String status) {
        String shortId = order.getId() != null
                ? order.getId().toString().substring(0, 8).toUpperCase()
                : "";
        String s = (status == null ? "" : status).toUpperCase();
        boolean firstConfirm = previous == null && "COMPLETED".equals(s);
        return switch (s) {
            case "PROCESSING" ->
                    "Order #" + shortId + " is being packed at our hub.";
            case "OUT_FOR_DELIVERY" ->
                    "Order #" + shortId + " is out for delivery" +
                            (order.getDeliveryZone() != null ? " to " + order.getDeliveryZone() : "") + ".";
            case "COMPLETED" ->
                    firstConfirm
                            ? "Order #" + shortId + " is confirmed. We’ll notify you when it’s on the way."
                            : "Order #" + shortId + " was marked delivered. Enjoy your groceries!";
            case "CANCELLED" ->
                    "Order #" + shortId + " was cancelled. Contact support if you need help.";
            case "PAID_STOCK_SHORTAGE" ->
                    "Order #" + shortId + " was paid but has a stock issue — our team will follow up.";
            default ->
                    "Order #" + shortId + " status is now " + status + ".";
        };
    }
}
