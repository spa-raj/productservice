package com.vibevault.productservice.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class ProductDataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final int TARGET_PRODUCTS = 2_000_000;
    private static final int BATCH_SIZE = 10_000;
    private static final int CURRENCY_INR_ORDINAL = 3;

    private static final String[] ADJECTIVES = {
            "Premium", "Classic", "Modern", "Vintage", "Elegant",
            "Rustic", "Sleek", "Bold", "Refined", "Artisan",
            "Luxury", "Compact", "Durable", "Lightweight", "Organic",
            "Handcrafted", "Smart", "Ultra", "Pro", "Essential",
            "Deluxe", "Elite", "Royal", "Natural", "Urban",
            "Tropical", "Nordic", "Zen", "Eco", "Flex"
    };

    private static final String[] MATERIALS = {
            "Cotton", "Leather", "Silk", "Bamboo", "Titanium",
            "Ceramic", "Wooden", "Steel", "Glass", "Copper",
            "Linen", "Velvet", "Canvas", "Marble", "Carbon",
            "Wool", "Denim", "Suede", "Bronze", "Crystal"
    };

    private static final String[] PRODUCT_TYPES = {
            "Shirt", "Jacket", "Watch", "Bag", "Wallet",
            "Shoes", "Belt", "Hat", "Scarf", "Sunglasses",
            "Backpack", "Ring", "Necklace", "Bracelet", "Earrings",
            "Lamp", "Chair", "Table", "Vase", "Mug",
            "Bottle", "Pen", "Notebook", "Clock", "Mirror",
            "Pillow", "Blanket", "Towel", "Candle", "Frame",
            "Speaker", "Headphones", "Charger", "Cable", "Stand",
            "Keychain", "Coaster", "Tray", "Bowl", "Plate",
            "Curtain", "Rug", "Basket", "Hook", "Shelf",
            "Soap", "Lotion", "Perfume", "Diffuser", "Balm",
            "Gloves", "Socks", "Tie", "Cufflinks", "Brooch",
            "Planter", "Toolbox", "Apron", "Spatula", "Whisk",
            "Thermos", "Lunchbox", "Tumbler", "Flask", "Jug",
            "Sandals", "Boots", "Sneakers", "Loafers", "Heels",
            "Clutch", "Tote", "Satchel", "Duffel", "Pouch"
    };

    private static final String[] CATEGORY_NAMES = {
            "Men's Clothing", "Women's Clothing", "Kids' Clothing",
            "Footwear", "Bags & Luggage", "Watches & Accessories",
            "Jewelry", "Home Decor", "Furniture", "Kitchen & Dining",
            "Bath & Body", "Lighting", "Electronics Accessories",
            "Stationery", "Drinkware", "Sports & Outdoors",
            "Beauty & Personal Care", "Pet Supplies", "Garden & Outdoor",
            "Tools & Hardware", "Toys & Games", "Books & Media",
            "Health & Wellness", "Automotive", "Office Supplies",
            "Art & Craft", "Musical Instruments", "Travel Accessories",
            "Party & Celebrations", "Seasonal", "Vintage Collection",
            "Sustainable Living", "Tech Gadgets", "Fitness Equipment",
            "Gourmet Food", "Beverages", "Baby Products",
            "Senior Care", "Wedding Collection", "Gift Sets",
            "Luxury Premium", "Budget Finds", "New Arrivals",
            "Clearance", "Limited Edition", "Handmade",
            "Imported", "Local Artisan", "Subscription Boxes", "Bundles"
    };

    @Override
    public void run(String... args) {
        log.info("=== Product Data Seeder Started ===");

        Long productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Long.class);
        if (productCount != null && productCount >= TARGET_PRODUCTS) {
            log.info("Products table already has {} rows, skipping seed.", productCount);
            return;
        }

        long start = System.currentTimeMillis();

        List<byte[]> categoryIds = seedCategories();
        seedProducts(categoryIds);
        updateCategoryCounts();

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        log.info("=== Product Data Seeder Completed in {} seconds ===", elapsed);
    }

    private List<byte[]> seedCategories() {
        Long categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class);
        if (categoryCount != null && categoryCount >= CATEGORY_NAMES.length) {
            log.info("Categories already seeded ({} rows), fetching existing IDs.", categoryCount);
            return jdbcTemplate.query("SELECT id FROM categories", (rs, rowNum) -> rs.getBytes("id"));
        }

        log.info("Seeding {} categories...", CATEGORY_NAMES.length);
        Timestamp now = Timestamp.from(Instant.now());
        List<byte[]> categoryIds = new ArrayList<>();

        String sql = "INSERT INTO categories (id, created_at, last_modified_at, is_deleted, name, description, product_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (String categoryName : CATEGORY_NAMES) {
            byte[] id = uuidToBytes(UUID.randomUUID());
            categoryIds.add(id);
            batchArgs.add(new Object[]{id, now, now, false, categoryName, "Products in " + categoryName});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("Inserted {} categories.", categoryIds.size());
        return categoryIds;
    }

    private void seedProducts(List<byte[]> categoryIds) {
        Long existingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Long.class);
        int alreadyInserted = existingCount != null ? existingCount.intValue() : 0;
        int remaining = TARGET_PRODUCTS - alreadyInserted;

        if (remaining <= 0) {
            log.info("Products already at target count.");
            return;
        }

        log.info("Seeding {} products ({} already exist)...", remaining, alreadyInserted);

        String sql = "INSERT INTO products (id, created_at, last_modified_at, is_deleted, name, description, image_url, price, currency, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Timestamp now = Timestamp.from(Instant.now());
        int totalInserted = 0;
        int nameIndex = alreadyInserted;

        while (totalInserted < remaining) {
            int batchCount = Math.min(BATCH_SIZE, remaining - totalInserted);
            List<Object[]> batch = new ArrayList<>(batchCount);

            for (int i = 0; i < batchCount; i++) {
                int idx = nameIndex++;
                String name = generateProductName(idx);
                double price = Math.round(rng.nextDouble(49.99, 99999.99) * 100.0) / 100.0;
                byte[] categoryId = categoryIds.get(idx % categoryIds.size());

                batch.add(new Object[]{
                        uuidToBytes(UUID.randomUUID()),
                        now, now, false,
                        name,
                        name + " — high quality product for everyday use.",
                        null,
                        price,
                        CURRENCY_INR_ORDINAL,
                        categoryId
                });
            }

            jdbcTemplate.batchUpdate(sql, batch);
            totalInserted += batchCount;

            if (totalInserted % 100_000 == 0 || totalInserted == remaining) {
                log.info("Progress: {}/{} products inserted.", totalInserted, remaining);
            }
        }

        log.info("Product seeding complete. Total inserted: {}", totalInserted);
    }

    private void updateCategoryCounts() {
        log.info("Updating category product counts...");
        jdbcTemplate.update(
                "UPDATE categories c SET c.product_count = " +
                "(SELECT COUNT(*) FROM products p WHERE p.category_id = c.id)"
        );
        log.info("Category product counts updated.");
    }

    private String generateProductName(int index) {
        int baseCombos = ADJECTIVES.length * MATERIALS.length * PRODUCT_TYPES.length; // 30*20*80 = 48000
        int comboIndex = index % baseCombos;
        int suffix = index / baseCombos;

        int adjIdx = comboIndex / (MATERIALS.length * PRODUCT_TYPES.length);
        int matIdx = (comboIndex / PRODUCT_TYPES.length) % MATERIALS.length;
        int typeIdx = comboIndex % PRODUCT_TYPES.length;

        String base = ADJECTIVES[adjIdx] + " " + MATERIALS[matIdx] + " " + PRODUCT_TYPES[typeIdx];
        return suffix == 0 ? base : base + " V" + suffix;
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
