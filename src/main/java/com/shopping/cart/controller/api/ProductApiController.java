package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.Product;
import com.shopping.cart.service.ProductService;
import com.shopping.cart.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;
    private final UserService userService;

    public ProductApiController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/index")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/show/{id}")
    public Product getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return product;
    }

    @PostMapping(value = "/store", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product addProduct(
            @RequestHeader("Authorization") String token,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam int stock,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        userService.requireAdmin(token);
        AddProductRequest request = new AddProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setStock(stock);
        return productService.store(request, images != null ? images : new MultipartFile[0]);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product updateProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam int stock,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        userService.requireAdmin(token);
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setStock(stock);
        return productService.update(id, request, images != null ? images : new MultipartFile[0]);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID id) {
        userService.requireAdmin(token);
        productService.delete(id);
    }
}
