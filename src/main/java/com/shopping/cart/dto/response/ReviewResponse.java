package com.shopping.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReviewResponse {
    private final UUID id;
    private final String comment;
    private final int rating;
    private final UUID productId;
    private final String authorDisplayName;

    public static ReviewResponse from(com.shopping.cart.entity.Review review) {
        String displayName = "Customer";
        if (review.getUser() != null) {
            String first = review.getUser().getFirstName() != null ? review.getUser().getFirstName() : "";
            String lastInitial = review.getUser().getLastName() != null && !review.getUser().getLastName().isBlank()
                    ? review.getUser().getLastName().substring(0, 1) + "."
                    : "";
            String combined = (first + " " + lastInitial).trim();
            if (!combined.isBlank()) {
                displayName = combined;
            } else if (review.getUser().getUsername() != null) {
                displayName = review.getUser().getUsername();
            }
        }
        UUID productId = review.getProduct() != null ? review.getProduct().getId() : null;
        return new ReviewResponse(
                review.getId(),
                review.getComment(),
                review.getRating(),
                productId,
                displayName
        );
    }
}
