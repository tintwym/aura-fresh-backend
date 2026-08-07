package com.shopping.cart.controller.api;

import com.shopping.cart.service.CheckoutFulfillmentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final CheckoutFulfillmentService checkoutFulfillmentService;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    public StripeWebhookController(CheckoutFulfillmentService checkoutFulfillmentService) {
        this.checkoutFulfillmentService = checkoutFulfillmentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
                                          @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            return ResponseEntity.status(501).body("Stripe webhook not configured");
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature header");
        }

        final Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = deserializeSession(event);
            if (session == null || session.getId() == null) {
                log.error("Could not deserialize checkout.session.completed event id={}", event.getId());
                return ResponseEntity.status(500).body("Could not read checkout session");
            }
            try {
                checkoutFulfillmentService.fulfillBySessionId(session.getId());
            } catch (Exception e) {
                log.error("Webhook fulfillment failed for session {}: {}", session.getId(), e.toString());
                return ResponseEntity.status(500).body("Webhook processing failed");
            }
        }

        return ResponseEntity.ok("ok");
    }

    private Session deserializeSession(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = deserializer.getObject().orElse(null);
        if (stripeObject instanceof Session session) {
            return session;
        }
        try {
            stripeObject = deserializer.deserializeUnsafe();
        } catch (Exception e) {
            log.warn("deserializeUnsafe failed for event {}: {}", event.getId(), e.toString());
            return null;
        }
        return stripeObject instanceof Session session ? session : null;
    }
}
