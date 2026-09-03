package com.shopping.cart.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryZonesTest {

    @Test
    void freeOverThreshold() {
        assertEquals(BigDecimal.ZERO, DeliveryZones.feeFor("Yankin", BigDecimal.valueOf(15_000)));
        assertEquals(BigDecimal.ZERO, DeliveryZones.feeFor("Hlaing", BigDecimal.valueOf(20_000)));
    }

    @Test
    void zoneFees() {
        assertEquals(BigDecimal.valueOf(2000), DeliveryZones.feeFor("Downtown Yangon", BigDecimal.valueOf(5_000)));
        assertEquals(BigDecimal.valueOf(3500), DeliveryZones.feeFor("Hlaing", BigDecimal.valueOf(5_000)));
    }

    @Test
    void normalizeFuzzy() {
        assertEquals("Bahan", DeliveryZones.normalize("Golden Valley, Bahan"));
        assertEquals("Yankin", DeliveryZones.normalize(null));
    }
}
