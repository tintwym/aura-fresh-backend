package com.shopping.cart.util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Yangon delivery zones and fees (MMK). Free delivery when grocery subtotal ≥ FREE_OVER_MMK.
 */
public final class DeliveryZones {

    public static final BigDecimal FREE_OVER_MMK = BigDecimal.valueOf(15_000);

    public static final Set<String> ZONES = Set.of(
            "Downtown Yangon",
            "Yankin",
            "Bahan",
            "Hlaing"
    );

    private static final Map<String, Integer> FEE_MMK = Map.of(
            "Downtown Yangon", 2000,
            "Yankin", 2500,
            "Bahan", 3000,
            "Hlaing", 3500
    );

    private static final Map<String, String> ETA = Map.of(
            "Downtown Yangon", "30–45 min",
            "Yankin", "35–50 min",
            "Bahan", "40–55 min",
            "Hlaing", "45–60 min"
    );

    private DeliveryZones() {}

    public static String normalize(String zone) {
        if (zone == null || zone.isBlank()) {
            return "Yankin";
        }
        String trimmed = zone.trim();
        for (String known : ZONES) {
            if (known.equalsIgnoreCase(trimmed)) {
                return known;
            }
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.contains("downtown")) return "Downtown Yangon";
        if (lower.contains("bahan")) return "Bahan";
        if (lower.contains("hlaing")) return "Hlaing";
        if (lower.contains("yankin")) return "Yankin";
        return "Yankin";
    }

    public static BigDecimal feeFor(String zone, BigDecimal grocerySubtotal) {
        if (grocerySubtotal != null && grocerySubtotal.compareTo(FREE_OVER_MMK) >= 0) {
            return BigDecimal.ZERO;
        }
        String normalized = normalize(zone);
        return BigDecimal.valueOf(FEE_MMK.getOrDefault(normalized, 2500));
    }

    public static String etaFor(String zone) {
        return ETA.getOrDefault(normalize(zone), "40–55 min");
    }
}
