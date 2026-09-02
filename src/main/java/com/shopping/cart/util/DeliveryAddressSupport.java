package com.shopping.cart.util;

import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.Profile;

import java.util.HashMap;
import java.util.Map;

public final class DeliveryAddressSupport {

    private DeliveryAddressSupport() {}

    public static void requireDeliverable(Profile profile) {
        if (profile == null) {
            throw new IllegalStateException(
                    "Add a delivery address in your account before checkout.");
        }
        String address1 = trim(profile.getAddress1());
        String city = trim(profile.getCity());
        String country = trim(profile.getCountry());
        if (address1 == null || city == null || country == null) {
            throw new IllegalStateException(
                    "Complete your delivery address (street, city, country) before checkout.");
        }
    }

    public static Map<String, String> toMetadata(Profile profile) {
        Map<String, String> meta = new HashMap<>();
        put(meta, "delivery_address1", profile.getAddress1());
        put(meta, "delivery_address2", profile.getAddress2());
        put(meta, "delivery_unit", profile.getUnit());
        put(meta, "delivery_floor", profile.getFloor());
        put(meta, "delivery_city", profile.getCity());
        put(meta, "delivery_state", profile.getState());
        put(meta, "delivery_country", profile.getCountry());
        put(meta, "delivery_zip_code", profile.getZipCode());
        return meta;
    }

    public static void applyMetadataToOrder(Order order, Map<String, String> metadata) {
        if (metadata == null) {
            return;
        }
        order.setDeliveryAddress1(metadata.get("delivery_address1"));
        order.setDeliveryAddress2(metadata.get("delivery_address2"));
        order.setDeliveryUnit(metadata.get("delivery_unit"));
        order.setDeliveryFloor(metadata.get("delivery_floor"));
        order.setDeliveryCity(metadata.get("delivery_city"));
        order.setDeliveryState(metadata.get("delivery_state"));
        order.setDeliveryCountry(metadata.get("delivery_country"));
        order.setDeliveryZipCode(metadata.get("delivery_zip_code"));
    }

    private static void put(Map<String, String> meta, String key, String value) {
        String trimmed = trim(value);
        if (trimmed != null) {
            meta.put(key, trimmed);
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
