package com.shopping.cart.service;

import com.shopping.cart.dto.request.AddReviewRequest;
import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.OrderItem;
import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.Review;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IReviewService;
import com.shopping.cart.repository.OrderItemRepository;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.ReviewRepository;
import com.shopping.cart.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository,
            UserService userService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public Review getReview(UUID id) {
        return reviewRepository.findById(Objects.requireNonNull(id)).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getAllReviewsForProduct(UUID productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public void addReview(String token, AddReviewRequest addReviewRequest) {
        User authUser = userService.requireUser(token);

        Product product = productRepository.findActiveByIdWithImages(addReviewRequest.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        User user = userRepository.findById(Objects.requireNonNull(authUser.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        OrderItem orderItem = orderItemRepository.findByIdWithOrder(addReviewRequest.getOrderItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        Order order = orderItem.getOrder();
        if (order == null || order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only review your own purchases");
        }
        if (orderItem.getProduct() == null || !orderItem.getProduct().getId().equals(product.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order item does not match product");
        }
        if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must be completed before reviewing");
        }
        if (reviewRepository.existsByOrderItem_Id(orderItem.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this item");
        }

        Review review = new Review(
                addReviewRequest.getComment(),
                addReviewRequest.getRating(),
                product,
                user,
                orderItem
        );

        reviewRepository.save(review);
    }

    @Override
    public void updateReview() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteReview() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
