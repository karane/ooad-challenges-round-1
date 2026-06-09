package org.karane.ticket.pricing;

import org.karane.ticket.domain.Show;
import org.karane.ticket.domain.Zone;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DynamicPricingStrategy implements PricingStrategy {

    private final PricingStrategy base;
    private final double demandThreshold; 
    private final BigDecimal surchargeRate; // e.g. 0.25 = 25%

    public DynamicPricingStrategy(PricingStrategy base, double demandThreshold, double surchargePercent) {
        if (demandThreshold < 0 || demandThreshold > 1)
            throw new IllegalArgumentException("demandThreshold must be 0–1");
        if (surchargePercent < 0)
            throw new IllegalArgumentException("surchargePercent must be >= 0");
        this.base           = base;
        this.demandThreshold = demandThreshold;
        this.surchargeRate  = BigDecimal.valueOf(surchargePercent / 100.0);
    }

    @Override
    public BigDecimal price(Show show, Zone zone) {
        var basePrice    = base.price(show, zone);
        double fillRate  = (double) show.soldCountIn(zone) / zone.capacity();
        if (fillRate >= demandThreshold) {
            return basePrice.multiply(BigDecimal.ONE.add(surchargeRate))
                            .setScale(2, RoundingMode.HALF_UP);
        }
        return basePrice;
    }
}
