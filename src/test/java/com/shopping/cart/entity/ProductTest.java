//package com.shopping.cart.entity;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class ProductTest {
//    private Product product;
//
//    @BeforeEach
//    void setUp() {
//        List<ProductImage> images = new ArrayList<>();
//        product = new Product("Test Product", "Test Description", BigDecimal.valueOf(19.99), 100, false, images);
//    }
//
//    @Test
//    void testProductCreation() {
//        assertNotNull(product);
//        assertEquals("Test Product", product.getName());
//        assertEquals("Test Description", product.getDescription());
//        assertEquals(BigDecimal.valueOf(19.99), product.getPrice());
//        assertEquals(100, product.getStock());
//        assertFalse(product.isDeleted());
//    }
//
//    @Test
//    void testAddImage() {
//        ProductImage image = new ProductImage("path/to/image.jpg", "Image Alt Text");
//        image.setProduct(product); // Link back to the product
//        product.getImages().add(image);
//
//        assertEquals(1, product.getImages().size());
//        assertEquals(image, product.getImages().get(0));
//        assertEquals(product, image.getProduct());
//    }
//}
