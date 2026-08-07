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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Seeds roles, Aura Fresh grocery catalog (demo-ready UI data), and optional admin.
 * Grocery rows match the web demo catalog so production API demos still look polished.
 */
@Component
public class DataInitializer implements ApplicationRunner {
    private static final Set<String> LEGACY_ELECTRONICS_SEEDS = Set.of(
            "Wireless Headphones",
            "Smart Watch",
            "USB-C Hub",
            "Mechanical Keyboard",
            "Wireless Mouse",
            "Portable SSD 1TB",
            "4K Webcam",
            "Bluetooth Speaker",
            "27\" Gaming Monitor",
            "Laptop Stand",
            "65W GaN Charger",
            "10\" Tablet",
            "Wireless Earbuds",
            "Smart Home Hub",
            "Desk Ring Light",
            "Power Bank 20000mAh"
    );

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
        retireLegacyElectronicsSeeds();
        seedAuraFreshGroceryCatalog();
        seedAdminUser();
    }

    private void seedRole(String name) {
        if (roleRepository.findByName(name) == null) {
            roleRepository.save(new Role(name));
        }
    }

    /** Hide old Pixel Tech–style electronics seeds so grocery demos stay clean. */
    private void retireLegacyElectronicsSeeds() {
        for (String name : LEGACY_ELECTRONICS_SEEDS) {
            productRepository.findByNameIgnoreCase(name).ifPresent(product -> {
                if (!product.isDeleted()) {
                    product.setDeleted(true);
                    productRepository.save(product);
                }
            });
        }
    }

    private void seedAuraFreshGroceryCatalog() {
        seedGrocery(
                "Shwe Bo Paw San Premium Rice",
                "Highly acclaimed, premium long-grain aromatic rice grown in the fertile lands of Shwe Bo. Fluffy, fragrant, and perfect for local meals.",
                "18500.00",
                45,
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Fresh Shan State Organic Avocados",
                "Creamy, rich avocados hand-picked from orchards in Kalaw, Shan State. Loaded with healthy fats and nutrients.",
                "3800.00",
                28,
                "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Halal Free-Range Whole Chicken",
                "Fresh, premium-quality free-range chicken, processed and certified strictly under Halal guidelines. Perfect for traditional curries.",
                "12500.00",
                12,
                "https://images.unsplash.com/photo-1604503468506-a8da13d82791?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Traditional Shan Yellow Tofu",
                "Authentic yellow tofu handmade from chickpea flour, following deep-rooted Shan traditions. Rich in plant-based proteins, gluten-free, and vegan-friendly.",
                "2500.00",
                35,
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Gluten-Free Almond & Seed Bread",
                "Freshly baked artisanal loaf made with premium almond flour, flaxseeds, and sunflower seeds. Fully gluten-free and low-carb.",
                "6500.00",
                8,
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Premium Shan Hills Arabica Coffee Beans",
                "Exquisite single-origin Arabica coffee beans grown under shade trees in the highlands of Pyin Oo Lwin. Rich aroma with notes of chocolate and citrus.",
                "14000.00",
                4,
                "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Fresh Organic Baby Spinach",
                "Tender baby spinach leaves cultivated using sustainable organic practices in local hydroponic farms. Pre-washed and ready to eat.",
                "4500.00",
                22,
                "https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Organic Farm-Fresh Grass-Fed Milk",
                "Pasteurized whole milk sourced from local grass-fed dairy cows. Highly nutritious, antibiotic-free, with no added hormones.",
                "5200.00",
                18,
                "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Natural Organic Coconut Water",
                "Pure, refreshing coconut water sourced from organic coastal groves. An excellent natural source of electrolytes with no added sugars.",
                "2900.00",
                50,
                "https://images.unsplash.com/photo-1543362906-acfc16c67564?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Premium Myanmar Raw Honey",
                "100% pure raw wildflower honey sourced sustainably from wild hives in the rural forests of Myanmar. Unfiltered to preserve all active enzymes.",
                "11500.00",
                15,
                "https://images.unsplash.com/photo-1471193945509-9ad0617afabf?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Organic Cashew & Almond Granola",
                "Crunchy artisanal granola roasted with honey, coconut flakes, organic almonds, and cashews. Rich in fiber, but note it contains nuts.",
                "8900.00",
                3,
                "https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&w=600&q=80");
    }

    private void seedGrocery(
            String name, String description, String price, int stock, String imageUrl) {
        var existing = productRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            Product product = existing.get();
            product.setDeleted(false);
            product.setDescription(description);
            product.setPrice(new BigDecimal(price));
            product.setStock(stock);
            ensureImage(product, imageUrl);
            productRepository.save(product);
            return;
        }
        saveProduct(name, description, price, stock, imageUrl);
    }

    private void ensureImage(Product product, String imageUrl) {
        List<ProductImage> images = product.getImages();
        if (images == null || images.isEmpty()) {
            ProductImage image = new ProductImage(imageUrl, product.getName());
            image.setProduct(product);
            List<ProductImage> next = new ArrayList<>();
            next.add(image);
            product.setImages(next);
            return;
        }
        ProductImage first = images.get(0);
        String path = first.getPath();
        if (path == null || path.isBlank() || needsImageRefresh(path) || !imageUrl.equals(path)) {
            // Keep grocery Unsplash URLs in sync for polished demos
            if (path == null || path.isBlank() || needsImageRefresh(path)
                    || path.contains("dummyimage.com")
                    || !path.contains("unsplash.com")) {
                first.setPath(imageUrl);
                first.setAltText(product.getName());
            }
        }
    }

    private boolean needsImageRefresh(String path) {
        String lower = path.toLowerCase();
        return lower.contains("placehold.co")
                || lower.contains("picsum.photos")
                || lower.contains("dummyimage.com")
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
        admin.setEmail(adminSeedUsername + "@aurafresh.local");
        admin.setPassword(PasswordHashingUtility.hashPassword(adminSeedPassword));
        admin.setRole(adminRole);
        userRepository.save(admin);
    }
}
