package com.shopping.cart.interfaces;

import com.shopping.cart.dto.request.AddReviewRequest;
import com.shopping.cart.entity.Review;

import java.util.List;
import java.util.UUID;

public interface IReviewService {
    Review getReview(UUID id);
    List<Review> getAllReviews();
    List<Review> getAllReviewsForProduct(UUID productId);

    void addReview(String token, AddReviewRequest addReviewRequest);
    void deleteReview();
    void updateReview();
}
