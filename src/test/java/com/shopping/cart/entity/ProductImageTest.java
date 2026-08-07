//package com.shopping.cart.entity;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class ProductImageTest {
//    private ProductImage productImage;
//    private Product product;
//
//    @BeforeEach
//    void setUp() {
//        product = new Product("Test Product", "Test Description", BigDecimal.valueOf(19.99), 100, false, new ArrayList<>());
//        productImage = new ProductImage("path/to/image.jpg", "Image Alt Text");
//        productImage.setProduct(product);
//    }
//
//    @Test
//    void testProductImageCreation() {
//        assertNotNull(productImage);
//        assertEquals("path/to/image.jpg", productImage.getPath());
//        assertEquals("Image Alt Text", productImage.getAltText());
//        assertEquals(product, productImage.getProduct());
//    }
//}
