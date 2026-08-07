package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.AddReviewRequest;
import com.shopping.cart.dto.response.ReviewResponse;
import com.shopping.cart.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {
    private final ReviewService reviewService;

    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getAllReviewsForProduct(@PathVariable UUID productId) {
        List<ReviewResponse> reviews = reviewService.getAllReviewsForProduct(productId).stream()
                .map(ReviewResponse::from)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/show")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        List<ReviewResponse> reviews = reviewService.getAllReviews().stream()
                .map(ReviewResponse::from)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/store")
    public ResponseEntity<?> addReview(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody AddReviewRequest addReviewRequest) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Authorization token is missing.");
        }

        reviewService.addReview(token, addReviewRequest);
        return ResponseEntity.ok("Review added successfully");
    }
}
