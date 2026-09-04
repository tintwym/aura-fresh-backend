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
import java.time.LocalDate;
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
                "Rice & Grains",
                null,
                "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Fresh Shan State Organic Avocados",
                "Creamy, rich avocados hand-picked from orchards in Kalaw, Shan State. Loaded with healthy fats and nutrients.",
                "3800.00",
                28,
                "Fresh Fruit",
                4,
                "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Halal Free-Range Whole Chicken",
                "Fresh, premium-quality free-range chicken, processed and certified strictly under Halal guidelines. Perfect for traditional curries.",
                "12500.00",
                12,
                "Poultry",
                3,
                "https://images.unsplash.com/photo-1604503468506-a8da13d82791?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Traditional Shan Yellow Tofu",
                "Authentic yellow tofu handmade from chickpea flour, following deep-rooted Shan traditions. Rich in plant-based proteins, gluten-free, and vegan-friendly.",
                "2500.00",
                35,
                "Tofu & Plant Protein",
                5,
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Gluten-Free Almond & Seed Bread",
                "Freshly baked artisanal loaf made with premium almond flour, flaxseeds, and sunflower seeds. Fully gluten-free and low-carb.",
                "6500.00",
                8,
                "Bakery & Bread",
                2,
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Premium Shan Hills Arabica Coffee Beans",
                "Exquisite single-origin Arabica coffee beans grown under shade trees in the highlands of Pyin Oo Lwin. Rich aroma with notes of chocolate and citrus.",
                "14000.00",
                4,
                "Tea & Coffee",
                null,
                "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Fresh Organic Baby Spinach",
                "Tender baby spinach leaves cultivated using sustainable organic practices in local hydroponic farms. Pre-washed and ready to eat.",
                "4500.00",
                22,
                "Fresh Vegetables",
                3,
                "https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Organic Farm-Fresh Grass-Fed Milk",
                "Pasteurized whole milk sourced from local grass-fed dairy cows. Highly nutritious, antibiotic-free, with no added hormones.",
                "5200.00",
                18,
                "Dairy & Eggs",
                5,
                "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Natural Organic Coconut Water",
                "Pure, refreshing coconut water sourced from organic coastal groves. An excellent natural source of electrolytes with no added sugars.",
                "2900.00",
                50,
                "Soft Drinks & Juices",
                10,
                "https://images.unsplash.com/photo-1543362906-acfc16c67564?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Premium Myanmar Raw Honey",
                "100% pure raw wildflower honey sourced sustainably from wild hives in the rural forests of Myanmar. Unfiltered to preserve all active enzymes.",
                "11500.00",
                15,
                "Spreads & Sweeteners",
                null,
                "https://images.unsplash.com/photo-1471193945509-9ad0617afabf?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Organic Cashew & Almond Granola",
                "Crunchy artisanal granola roasted with honey, coconut flakes, organic almonds, and cashews. Rich in fiber, but note it contains nuts.",
                "8900.00",
                3,
                "Breakfast & Cereals",
                null,
                "https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&w=600&q=80");

        // Fresh produce & herbs
        seedGrocery(
                "Vine-Ripened Inle Tomatoes",
                "Sweet, juicy tomatoes grown around Inle Lake. Ideal for salads, curries, and fresh dips.",
                "2200.00",
                40,
                "Fresh Vegetables",
                4,
                "https://images.unsplash.com/photo-1546470427-e26264be0b0d?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Ayeyarwady Cavendish Bananas",
                "Naturally ripened Cavendish bananas from the Ayeyarwady delta. Soft, sweet, and perfect for snacking.",
                "1800.00",
                55,
                "Fresh Fruit",
                5,
                "https://images.unsplash.com/photo-1571771894821-ce9b6d11abb3?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Seasonal Myanmar Mangoes",
                "Ripe seasonal mangoes with fragrant flesh and natural sweetness. Best enjoyed chilled.",
                "4500.00",
                24,
                "Fresh Fruit",
                3,
                "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Fresh Green Cabbage",
                "Crisp whole green cabbage heads for salads, stir-fries, and traditional soups.",
                "1500.00",
                32,
                "Fresh Vegetables",
                6,
                "https://images.unsplash.com/photo-1504721838965-dfcb29cc11f5?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Organic Fresh Ginger Root",
                "Aromatic ginger root harvested from highland farms. Essential for curries, teas, and marinades.",
                "2800.00",
                30,
                "Herbs & Spices",
                7,
                "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Hot Bird's Eye Chilies",
                "Fiery bird's eye chilies packed with heat and flavor. Use sparingly in stir-fries and pastes.",
                "1200.00",
                48,
                "Herbs & Spices",
                5,
                "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=600&q=80");

        // Fresh meat & seafood
        seedGrocery(
                "Fresh River Fish Fillet",
                "Cleaned river fish fillets delivered chilled. Mild flavor that works well grilled or in sour soups.",
                "9800.00",
                14,
                "Seafood",
                2,
                "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Lean Pork Shoulder",
                "Tender pork shoulder cuts trimmed for stews and stir-fries. Packed fresh daily.",
                "8900.00",
                16,
                "Fresh Meat",
                3,
                "https://images.unsplash.com/photo-1602470520998-f4a78bf3c7d3?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Coastal Tiger Prawns",
                "Firm tiger prawns from coastal suppliers. Peel, cook quickly, and serve in garlic or curry dishes.",
                "15500.00",
                10,
                "Seafood",
                2,
                "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?auto=format&fit=crop&w=600&q=80");

        // Dairy & eggs
        seedGrocery(
                "Natural Plain Yogurt",
                "Creamy plain yogurt made from local milk. Great for breakfast bowls, marinades, and cooling sides.",
                "3500.00",
                26,
                "Dairy & Eggs",
                6,
                "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Farm-Fresh Free-Range Eggs",
                "Dozen free-range eggs from small farms. Rich yolks for omelettes, baking, and everyday cooking.",
                "4200.00",
                40,
                "Dairy & Eggs",
                10,
                "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?auto=format&fit=crop&w=600&q=80");

        // Bakery
        seedGrocery(
                "Warm Butter Naan Flatbread",
                "Soft bakery naan brushed with butter. Serve with curries or wrap fillings while warm.",
                "3200.00",
                18,
                "Bakery & Bread",
                2,
                "https://images.unsplash.com/photo-1565557623262-b51c2513a641?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Crispy Butter Cookies",
                "Golden butter cookies baked in small batches. Lightly sweet and perfect with tea.",
                "4800.00",
                20,
                "Snacks & Biscuits",
                8,
                "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?auto=format&fit=crop&w=600&q=80");

        // Pantry aisles
        seedGrocery(
                "Dried Chickpeas",
                "Clean dried chickpeas for hummus, curries, and stews. Soak overnight for best texture.",
                "3600.00",
                38,
                "Pulses & Legumes",
                null,
                "https://images.unsplash.com/photo-1515543904379-3d757afe72e4?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Cold-Pressed Peanut Oil",
                "Golden cold-pressed peanut oil for frying and everyday Myanmar cooking. Clean nutty aroma.",
                "7500.00",
                22,
                "Cooking Oils",
                null,
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Traditional Fish Sauce",
                "Savory fermented fish sauce for salads, noodle soups, and dipping sauces. Use a splash for umami depth.",
                "3100.00",
                34,
                "Sauces & Condiments",
                null,
                "https://images.unsplash.com/photo-1596040033229-a9821f5b1d05?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Dried Rice Vermicelli Noodles",
                "Fine dried rice vermicelli ready for salads, soups, and stir-fries. Cooks in minutes.",
                "2700.00",
                42,
                "Noodles & Pasta",
                null,
                "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=600&q=80");

        // Drinks
        seedGrocery(
                "Highland Green Tea Leaves",
                "Fragrant green tea leaves from highland gardens. Brew light and floral cups morning or afternoon.",
                "6800.00",
                28,
                "Tea & Coffee",
                null,
                "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=600&q=80");
        seedGrocery(
                "Fresh-Pressed Lime Juice",
                "Tangy lime juice bottled without added sugar. Brighten drinks, salads, and seafood dishes.",
                "3900.00",
                25,
                "Soft Drinks & Juices",
                7,
                "https://images.unsplash.com/photo-1590502593747-42a996133562?auto=format&fit=crop&w=600&q=80");
    }

    private void seedGrocery(
            String name,
            String description,
            String price,
            int stock,
            String category,
            Integer expiryInDays,
            String imageUrl) {
        LocalDate expiryDate = expiryInDays == null ? null : LocalDate.now().plusDays(expiryInDays);
        var existing = productRepository.findByNameIgnoreCase(name);
        if (existing.isPresent()) {
            Product product = existing.get();
            product.setDeleted(false);
            product.setDescription(description);
            product.setPrice(new BigDecimal(price));
            product.setStock(stock);
            product.setCategory(category);
            product.setExpiryDate(expiryDate);
            ensureImage(product, imageUrl);
            productRepository.save(product);
            return;
        }
        saveProduct(name, description, price, stock, category, expiryDate, imageUrl);
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
        // Always keep seed Unsplash URLs in sync on API restart
        if (path == null || path.isBlank() || needsImageRefresh(path) || !imageUrl.equals(path)) {
            first.setPath(imageUrl);
            first.setAltText(product.getName());
        }
    }

    private boolean needsImageRefresh(String path) {
        String lower = path.toLowerCase();
        return lower.contains("placehold.co")
                || lower.contains("picsum.photos")
                || lower.contains("dummyimage.com")
                || lower.endsWith(".svg");
    }

    private void saveProduct(
            String name,
            String description,
            String price,
            int stock,
            String category,
            LocalDate expiryDate,
            String imagePath) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setCategory(category);
        product.setExpiryDate(expiryDate);

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
