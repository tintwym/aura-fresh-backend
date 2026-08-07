package com.shopping.cart.config;

import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.ProductImage;
import com.shopping.cart.entity.Role;
import com.shopping.cart.entity.User;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.RoleRepository;
import com.shopping.cart.repository.UserRepository;
import com.shopping.cart.utility.PasswordHashingUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${app.admin.seed-username:}")
    private String adminSeedUsername;

    @Value("${app.admin.seed-password:}")
    private String adminSeedPassword;

    public DataInitializer(
            RoleRepository roleRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRole("User");
        seedRole("Admin");
        seedSampleProducts();
        seedAdminUser();
    }

    private void seedRole(String name) {
        if (roleRepository.findByName(name) == null) {
            roleRepository.save(new Role(name));
        }
    }

    private void seedSampleProducts() {
        seedProduct("Wireless Headphones",
                "Noise-cancelling over-ear headphones with 30-hour battery life.",
                "129.00", 25);
        seedProduct("Smart Watch",
                "Track fitness, sleep, and notifications on your wrist.",
                "249.00", 15);
        seedProduct("USB-C Hub",
                "7-in-1 adapter with HDMI, USB 3.0, and SD card reader.",
                "49.00", 40);
        seedProduct("Mechanical Keyboard",
                "Compact layout with hot-swappable switches.",
                "89.00", 20);
        seedProduct("Wireless Mouse",
                "Ergonomic silent-click mouse with multi-device pairing.",
                "39.00", 35);
        seedProduct("Portable SSD 1TB",
                "USB 3.2 external drive for fast backups and travel.",
                "119.00", 18);
        seedProduct("4K Webcam",
                "Auto-focus camera with built-in mic and privacy shutter.",
                "79.00", 22);
        seedProduct("Bluetooth Speaker",
                "Water-resistant speaker with 12-hour playtime.",
                "59.00", 30);
        seedProduct("27\" Gaming Monitor",
                "144Hz IPS panel with low-latency mode.",
                "329.00", 12);
        seedProduct("Laptop Stand",
                "Aluminium riser with adjustable height and cable slot.",
                "45.00", 28);
        seedProduct("65W GaN Charger",
                "Compact dual-port USB-C charger for phone and laptop.",
                "34.00", 50);
        seedProduct("10\" Tablet",
                "Lightweight tablet for reading, notes, and streaming.",
                "199.00", 14);
        seedProduct("Wireless Earbuds",
                "In-ear buds with active noise cancellation.",
                "99.00", 32);
        seedProduct("Smart Home Hub",
                "Control lights, sensors, and routines from one app.",
                "69.00", 16);
        seedProduct("Desk Ring Light",
                "Adjustable LED ring light for video calls and streaming.",
                "42.00", 24);
        seedProduct("Power Bank 20000mAh",
                "High-capacity battery with USB-C PD fast charging.",
                "55.00", 38);
    }

    private void seedProduct(
            String name, String description, String price, int stock) {
        String imageUrl = placeholderImage(name);
        var existing = productRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            refreshLegacyImage(existing.get(), imageUrl);
            return;
        }
        saveProduct(name, description, price, stock, imageUrl);
    }

    private String placeholderImage(String name) {
        // Use PNG — placehold.co defaults to SVG, which iOS AsyncImage/UIImage cannot decode.
        String text = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://dummyimage.com/600x450/e2e8f0/64748b.png&text=" + text;
    }

    private void refreshLegacyImage(Product product, String imageUrl) {
        List<ProductImage> images = product.getImages();
        if (images == null || images.isEmpty()) {
            ProductImage image = new ProductImage(imageUrl, product.getName());
            image.setProduct(product);
            List<ProductImage> next = new ArrayList<>();
            next.add(image);
            product.setImages(next);
            productRepository.save(product);
            return;
        }
        String path = images.get(0).getPath();
        if (needsPlaceholderRefresh(path)) {
            images.get(0).setPath(imageUrl);
            productRepository.save(product);
        }
    }

    private boolean needsPlaceholderRefresh(String path) {
        if (path == null || path.isBlank()) {
            return true;
        }
        String lower = path.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return true;
        }
        // Legacy seeds that iOS cannot render (SVG) or that are broken placeholders.
        return lower.contains("placehold.co")
                || lower.contains("picsum.photos")
                || lower.endsWith(".svg");
    }

    private void saveProduct(String name, String description, String price, int stock, String imagePath) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);

        ProductImage image = new ProductImage(imagePath, name);
        image.setProduct(product);
        List<ProductImage> images = new ArrayList<>();
        images.add(image);
        product.setImages(images);

        productRepository.save(product);
    }

    private void seedAdminUser() {
        if (adminSeedUsername == null || adminSeedUsername.isBlank()
                || adminSeedPassword == null || adminSeedPassword.isBlank()) {
            return;
        }
        if (userRepository.findByUsername(adminSeedUsername) != null) {
            return;
        }
        Role adminRole = roleRepository.findByName("Admin");
        if (adminRole == null) {
            return;
        }
        User admin = new User();
        admin.setFirstName("Store");
        admin.setLastName("Admin");
        admin.setUsername(adminSeedUsername);
        admin.setEmail(adminSeedUsername + "@pixeltech.local");
        admin.setPassword(PasswordHashingUtility.hashPassword(adminSeedPassword));
        admin.setRole(adminRole);
        userRepository.save(admin);
    }
}
