package com.shopping.cart.util;

import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.Profile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryAddressSupportTest {

    @Test
    void requireDeliverable_rejectsMissingProfile() {
        assertThrows(IllegalStateException.class, () -> DeliveryAddressSupport.requireDeliverable(null));
    }

    @Test
    void requireDeliverable_rejectsIncompleteAddress() {
        Profile profile = new Profile();
        profile.setAddress1("123 Main St");
        assertThrows(IllegalStateException.class, () -> DeliveryAddressSupport.requireDeliverable(profile));
    }

    @Test
    void toMetadata_and_applyMetadata_roundTrip() {
        Profile profile = new Profile(
                "123 Main St",
                "Apt 2",
                "5B",
                "3",
                "Yangon",
                "Yangon Region",
                "Myanmar",
                "11201");

        Map<String, String> meta = DeliveryAddressSupport.toMetadata(profile);
        Order order = new Order();
        DeliveryAddressSupport.applyMetadataToOrder(order, meta);

        assertEquals("123 Main St", order.getDeliveryAddress1());
        assertEquals("Apt 2", order.getDeliveryAddress2());
        assertEquals("Yangon", order.getDeliveryCity());
        assertEquals("Myanmar", order.getDeliveryCountry());
    }
}
