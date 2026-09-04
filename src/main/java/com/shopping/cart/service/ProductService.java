package com.shopping.cart.service;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.ProductImage;
import com.shopping.cart.entity.Product;
import com.shopping.cart.interfaces.IProductService;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.ReviewRepository;
import com.stripe.Stripe;
import com.stripe.model.Price;
import com.stripe.param.PriceCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CloudinaryImageService cloudinaryImageService;
    private final ReviewRepository reviewRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public ProductService(
            ProductRepository productRepository,
            CloudinaryImageService cloudinaryImageService,
            ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.cloudinaryImageService = cloudinaryImageService;
        this.reviewRepository = reviewRepository;
    }

    // Initialize Stripe with the API key from application.properties
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findByIsDeletedFalseWithImages();
        attachReviewStats(products);
        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(UUID id) {
        Product product = productRepository.findActiveByIdWithImages(Objects.requireNonNull(id)).orElse(null);
        if (product != null) {
            attachReviewStats(List.of(product));
        }
        return product;
    }

    private void attachReviewStats(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        Map<UUID, double[]> stats = new HashMap<>();
        for (Object[] row : reviewRepository.averageRatingByProduct()) {
            UUID productId = (UUID) row[0];
            double avg = row[1] instanceof Number n ? n.doubleValue() : 0;
            long count = row[2] instanceof Number n ? n.longValue() : 0L;
            stats.put(productId, new double[]{avg, count});
        }
        for (Product product : products) {
            double[] s = stats.get(product.getId());
            if (s != null) {
                product.setAverageRating(Math.round(s[0] * 10.0) / 10.0);
                product.setReviewCount((long) s[1]);
            } else {
                product.setAverageRating(null);
                product.setReviewCount(0L);
            }
        }
    }

    @Override
    @Transactional
    public Product store(AddProductRequest addProductRequest, MultipartFile[] images) {
        if (addProductRequest.getPrice() == null || addProductRequest.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (addProductRequest.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        Product product = new Product();
        product.setName(addProductRequest.getName());
        product.setDescription(addProductRequest.getDescription());
        product.setPrice(addProductRequest.getPrice());
        product.setStock(addProductRequest.getStock());
        product.setCategory(normalizeCategory(addProductRequest.getCategory()));
        product.setExpiryDate(addProductRequest.getExpiryDate());
        validateFreshnessFields(product);

        List<ProductImage> productImages = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryImageService.upload(image);
                String altText = image.getOriginalFilename() != null
                        ? image.getOriginalFilename()
                        : product.getName();
                ProductImage productImage = new ProductImage(imageUrl, altText);
                productImage.setProduct(product);
                productImages.add(productImage);
            }
        }
        product.setImages(productImages);

        // Persist first so we have an internal id for Stripe metadata (avoids orphans without a DB row).
        product = productRepository.save(product);

        try {
            String stripeProductId = createStripeProduct(
                    addProductRequest.getName(),
                    addProductRequest.getDescription(),
                    product.getId().toString());
            product.setStripeProductId(stripeProductId);
            String stripePriceId = createStripePrice(stripeProductId, addProductRequest.getPrice());
            product.setStripePriceId(stripePriceId);
            return productRepository.save(product);
        } catch (RuntimeException e) {
            product.setDeleted(true);
            product.setName(product.getName() + "__deleted__" + product.getId());
            productRepository.save(product);
            throw e;
        }
    }

    private String createStripeProduct(String name, String description, String internalProductId) {
        try {
            com.stripe.param.ProductCreateParams params = com.stripe.param.ProductCreateParams.builder()
                    .setName(name)
                    .setDescription(description)
                    .putMetadata("internal_product_id", internalProductId)
                    .build();

            com.stripe.model.Product stripeProduct = com.stripe.model.Product.create(params);
            return stripeProduct.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create product in Stripe", e);
        }
    }

    private String createStripePrice(String productId, BigDecimal amount) {
        try {
            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(productId)
                    .setUnitAmount(toStripeAmount(amount))
                    .setCurrency("mmk")
                    .build();

            Price stripePrice = Price.create(params);
            return stripePrice.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create price in Stripe", e);
        }
    }

    /** MMK is a Stripe zero-decimal currency — amount is whole kyat, not cents. */
    private static long toStripeAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private static String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String trimmed = category.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Fresh protein and dairy aisles must carry an expiry date for the customer app. */
    private static void validateFreshnessFields(Product product) {
        if (!requiresExpiry(product)) {
            return;
        }
        if (product.getExpiryDate() == null) {
            throw new IllegalArgumentException(
                    "Fresh meat, seafood, poultry, and dairy products require an expiry date");
        }
    }

    private static boolean requiresExpiry(Product product) {
        String category = product.getCategory() == null ? "" : product.getCategory().toLowerCase();
        if (category.contains("poultry")
                || category.contains("seafood")
                || category.contains("fresh meat")
                || "meat".equals(category)
                || (category.contains("meat") && !category.contains("plant"))
                || category.contains("dairy")
                || category.contains("eggs")) {
            return true;
        }

        // Skip sauces/oils/bakery snacks so "fish sauce" / "butter cookies" are not forced expiry
        if (category.contains("sauce")
                || category.contains("condiment")
                || category.contains("oil")
                || category.contains("bakery")
                || category.contains("snack")
                || category.contains("biscuit")) {
            return false;
        }

        String haystack = (
                (product.getName() == null ? "" : product.getName()) + " " +
                (product.getDescription() == null ? "" : product.getDescription())
        ).toLowerCase();
        return haystack.contains("chicken")
                || haystack.contains("beef")
                || haystack.contains("pork")
                || haystack.contains("mutton")
                || haystack.contains("prawn")
                || haystack.contains("shrimp")
                || haystack.matches(".*\\bfish\\b.*")
                || haystack.contains("seafood")
                || haystack.matches(".*\\bmilk\\b.*")
                || haystack.contains("cheese")
                || haystack.contains("yogurt")
                || haystack.contains("yoghurt");
    }

    @Override
    @Transactional
    public Product update(UUID id, UpdateProductRequest updateProductRequest, MultipartFile[] newImages) {
        Product product = productRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.isDeleted()) {
            throw new IllegalStateException("Product is no longer available");
        }

        BigDecimal newPrice = updateProductRequest.getPrice();
        boolean priceChanged = product.getPrice().compareTo(newPrice) != 0;

        product.setName(updateProductRequest.getName());
        product.setDescription(updateProductRequest.getDescription());
        product.setPrice(newPrice);
        product.setStock(updateProductRequest.getStock());
        product.setCategory(normalizeCategory(updateProductRequest.getCategory()));
        product.setExpiryDate(updateProductRequest.getExpiryDate());
        validateFreshnessFields(product);

        if (newImages != null && newImages.length > 0) {
            List<ProductImage> productImages = product.getImages();

            for (MultipartFile newImage : newImages) {
                if (newImage != null && !newImage.isEmpty()) {
                    String imageUrl = cloudinaryImageService.upload(newImage);
                    String altText = newImage.getOriginalFilename() != null
                            ? newImage.getOriginalFilename()
                            : product.getName();
                    ProductImage productImage = new ProductImage(imageUrl, altText);
                    productImage.setProduct(product);
                    productImages.add(productImage);
                }
            }

            product.setImages(productImages);
        }

        if (priceChanged && product.getStripeProductId() != null && !product.getStripeProductId().isBlank()) {
            product.setStripePriceId(createStripePrice(product.getStripeProductId(), newPrice));
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        productRepository.findById(Objects.requireNonNull(id)).ifPresent(product -> {
            product.setDeleted(true);
            // Free the unique name so the same product title can be recreated later.
            product.setName(product.getName() + "__deleted__" + product.getId());
            productRepository.save(product);
        });
    }
}
