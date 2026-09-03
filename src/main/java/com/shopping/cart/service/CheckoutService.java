package com.shopping.cart.service;

import com.shopping.cart.dto.response.CheckoutSessionResponse;
import com.shopping.cart.entity.Cart;
import com.shopping.cart.entity.CartItem;
import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.ICheckoutService;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.ProfileRepository;
import com.shopping.cart.util.DeliveryAddressSupport;
import com.shopping.cart.util.DeliveryZones;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CheckoutService implements ICheckoutService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final CheckoutFulfillmentService checkoutFulfillmentService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${app.checkout.success-url:}")
    private String checkoutSuccessUrl;

    @Value("${app.checkout.cancel-url:}")
    private String checkoutCancelUrl;

    public CheckoutService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            ProfileRepository profileRepository,
            UserService userService,
            CheckoutFulfillmentService checkoutFulfillmentService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.profileRepository = profileRepository;
        this.userService = userService;
        this.checkoutFulfillmentService = checkoutFulfillmentService;
    }

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutSessionResponse checkout(String token, String deliveryZone) {
        User user = userService.requireUser(token);

        var profile = profileRepository.findByUserId(user.getId());
        DeliveryAddressSupport.requireDeliverable(profile);

        Cart cart = cartRepository.findByUserWithItems(user);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Cannot proceed with checkout.");
        }

        BigDecimal grocerySubtotal = BigDecimal.ZERO;
        List<SessionCreateParams.LineItem> stripeLineItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            UUID productId = Objects.requireNonNull(cartItem.getProduct().getId());
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + productId));

            if (product.isDeleted()) {
                throw new IllegalStateException("Removed product still in cart: " + product.getName());
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for " + product.getName()
                        + " (requested " + cartItem.getQuantity() + ", available " + product.getStock() + ")");
            }

            grocerySubtotal = grocerySubtotal.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            SessionCreateParams.LineItem stripeLineItem;
            if (product.getStripePriceId() != null && !product.getStripePriceId().isBlank()) {
                stripeLineItem = SessionCreateParams.LineItem.builder()
                        .setPrice(product.getStripePriceId())
                        .setQuantity((long) cartItem.getQuantity())
                        .build();
            } else {
                stripeLineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("mmk")
                                        .setUnitAmount(toStripeAmount(product.getPrice()))
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(product.getName())
                                                        .putMetadata("internal_product_id", product.getId().toString())
                                                        .build()
                                        )
                                        .build()
                        )
                        .setQuantity((long) cartItem.getQuantity())
                        .build();
            }
            stripeLineItems.add(stripeLineItem);
        }

        String zone = DeliveryZones.normalize(
                deliveryZone != null && !deliveryZone.isBlank()
                        ? deliveryZone
                        : profile.getCity());
        BigDecimal deliveryFee = DeliveryZones.feeFor(zone, grocerySubtotal);
        if (deliveryFee.signum() > 0) {
            stripeLineItems.add(
                    SessionCreateParams.LineItem.builder()
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("mmk")
                                            .setUnitAmount(toStripeAmount(deliveryFee))
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName("Delivery — " + zone)
                                                            .putMetadata("kind", "delivery_fee")
                                                            .putMetadata("delivery_zone", zone)
                                                            .build()
                                            )
                                            .build()
                            )
                            .setQuantity(1L)
                            .build());
        }

        String successUrl;
        String cancelUrl;
        if (checkoutSuccessUrl != null && !checkoutSuccessUrl.isBlank()) {
            successUrl = checkoutSuccessUrl;
            cancelUrl = checkoutCancelUrl != null && !checkoutCancelUrl.isBlank()
                    ? checkoutCancelUrl
                    : "aurafresh://cart";
        } else {
            String base = frontendBaseUrl.endsWith("/")
                    ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                    : frontendBaseUrl;
            successUrl = base + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
            cancelUrl = base + "/cart";
        }

        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addAllLineItem(stripeLineItems)
                    .setClientReferenceId(user.getId().toString())
                    .putMetadata("user_id", user.getId().toString())
                    .putMetadata("delivery_zone", zone)
                    .putMetadata("delivery_fee", deliveryFee.toPlainString())
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl);
            DeliveryAddressSupport.toMetadata(profile).forEach(builder::putMetadata);

            Session session = Session.create(builder.build());
            return new CheckoutSessionResponse(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    @Override
    public void confirmCheckoutSession(String token, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalStateException("sessionId is required");
        }

        User user = userService.requireUser(token);
        try {
            checkoutFulfillmentService.confirmSessionForUser(sessionId, user);
        } catch (StripeException e) {
            throw new RuntimeException("Unable to confirm checkout with Stripe", e);
        }
    }

    /** MMK is a Stripe zero-decimal currency — amount is whole kyat, not cents. */
    private static long toStripeAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
