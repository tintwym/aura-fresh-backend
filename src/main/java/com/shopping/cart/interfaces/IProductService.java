package com.shopping.cart.interfaces;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    List<Product> getAllProducts();
    Product getProductById(UUID id);
    Product store(AddProductRequest addProductRequest, MultipartFile[] images);
    Product update(UUID id, UpdateProductRequest updateProductRequest, MultipartFile[] newImages);
    void delete(UUID id);
}
