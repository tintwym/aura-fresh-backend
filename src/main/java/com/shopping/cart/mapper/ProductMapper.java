package com.shopping.cart.mapper;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.Product;

public class ProductMapper {
    public static Product fromAddProductRequest(AddProductRequest addProductRequest) {
        Product product = new Product();
        product.setName(addProductRequest.getName());
        product.setDescription(addProductRequest.getDescription());
        product.setPrice(addProductRequest.getPrice());
        product.setStock(addProductRequest.getStock());
        return product;
    }

    public static void fromUpdateProductRequest(Product product, UpdateProductRequest updateProductRequest) {
        product.setName(updateProductRequest.getName());
        product.setDescription(updateProductRequest.getDescription());
        product.setPrice(updateProductRequest.getPrice());
        product.setStock(updateProductRequest.getStock());
    }
}
