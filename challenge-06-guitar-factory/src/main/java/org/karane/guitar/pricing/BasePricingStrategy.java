package org.karane.guitar.pricing;

import org.karane.guitar.domain.GuitarSpec;
import org.karane.guitar.domain.WoodType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BasePricingStrategy {

    private static final BigDecimal BASE         = new BigDecimal("500.00");
    private static final BigDecimal WOOD_PREMIUM = new BigDecimal("150.00");
    private static final BigDecimal EXTRA_STRING = new BigDecimal("30.00");

    private final BigDecimal markupFactor;

    public BasePricingStrategy() {
        this(0);
    }

    public BasePricingStrategy(double markupPercent) {
        if (markupPercent < 0) throw new IllegalArgumentException("markupPercent must be >= 0");
        this.markupFactor = BigDecimal.ONE.add(BigDecimal.valueOf(markupPercent / 100.0));
    }

    public BigDecimal price(GuitarSpec spec) {
        var total = BASE;

        for (var wood : new WoodType[]{spec.bodyWood(), spec.neckWood(), spec.fretboardWood()}) {
            if (isPremiumWood(wood)) total = total.add(WOOD_PREMIUM);
        }

        for (var pickup : spec.pickups()) {
            total = total.add(pickupCost(pickup));
        }

        int extraStrings = spec.stringCount() - 6;
        if (extraStrings > 0) {
            total = total.add(EXTRA_STRING.multiply(BigDecimal.valueOf(extraStrings)));
        }

        return total.multiply(markupFactor).setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isPremiumWood(WoodType w) {
        return w == WoodType.MAHOGANY || w == WoodType.ROSEWOOD
            || w == WoodType.EBONY   || w == WoodType.WALNUT;
    }

    private static BigDecimal pickupCost(GuitarSpec.PickupType pickup) {
        return switch (pickup) {
            case SINGLE_COIL -> new BigDecimal("50.00");
            case P90         -> new BigDecimal("75.00");
            case HUMBUCKER   -> new BigDecimal("100.00");
        };
    }
}
