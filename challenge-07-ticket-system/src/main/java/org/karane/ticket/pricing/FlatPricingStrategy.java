package org.karane.ticket.pricing;

import org.karane.ticket.domain.BalconyZone;
import org.karane.ticket.domain.FloorZone;
import org.karane.ticket.domain.GeneralZone;
import org.karane.ticket.domain.Show;
import org.karane.ticket.domain.VipZone;
import org.karane.ticket.domain.Zone;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fixed base price multiplied by the zone's price multiplier.
 * Uses an exhaustive switch on the sealed Zone hierarchy to confirm
 * each subtype is explicitly accounted for.
 */
public class FlatPricingStrategy implements PricingStrategy {

    private final BigDecimal basePrice;

    public FlatPricingStrategy(BigDecimal basePrice) {
        if (basePrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("basePrice must be positive");
        this.basePrice = basePrice;
    }

    @Override
    public BigDecimal price(Show show, Zone zone) {
        // Exhaustive switch -- ensures every Zone subtype has a defined price path
        BigDecimal multiplier = switch (zone) {
            case VipZone     z -> z.priceMultiplier();
            case FloorZone   z -> z.priceMultiplier();
            case GeneralZone z -> z.priceMultiplier();
            case BalconyZone z -> z.priceMultiplier();
        };
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
