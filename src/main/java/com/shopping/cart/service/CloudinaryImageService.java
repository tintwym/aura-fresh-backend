package com.shopping.cart.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryImageService {

    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryImageService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${cloudinary.folder:shopping-cart/products}") String folder) {
        this.folder = folder;
        if (cloudName == null || cloudName.isBlank()) {
            this.cloudinary = null;
            return;
        }
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public boolean isConfigured() {
        return cloudinary != null;
    }

    public String upload(MultipartFile file) {
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image uploads are allowed");
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("Image exceeds 5MB limit");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "public_id", UUID.randomUUID().toString(),
                            "resource_type", "image"));
            Object url = result.get("secure_url");
            if (url == null) {
                throw new IllegalStateException("Cloudinary upload did not return a URL");
            }
            return url.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image for upload", e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }
}
