package org.karane.freight.strategy;

import org.karane.freight.domain.ShippingEstimate;
import org.karane.freight.domain.Shipment;
import org.karane.freight.domain.TransportationType;
import org.karane.freight.pricing.FreightRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RailFreightCalculator implements FreightCalculator {

    private static final BigDecimal BULK_THRESHOLD_KG  = BigDecimal.valueOf(500);
    private static final BigDecimal BULK_DISCOUNT       = BigDecimal.valueOf(0.10);

    private final TransportationType.Rail rail;

    public RailFreightCalculator(TransportationType.Rail rail) {
        this.rail = rail;
    }

    @Override
    public TransportationType transportationType() { return rail; }

    @Override
    public ShippingEstimate calculate(Shipment shipment, FreightRate rate) {
        var weightCost = shipment.billableWeightKg().multiply(rate.ratePerKg());
        var volumeCost = shipment.volumeM3().multiply(rate.ratePerM3());
        var baseCost   = weightCost.add(volumeCost).setScale(2, RoundingMode.HALF_UP);

        // Bulk discount for heavy cargo
        if (shipment.weightKg().compareTo(BULK_THRESHOLD_KG) > 0) {
            baseCost = baseCost.multiply(BigDecimal.ONE.subtract(BULK_DISCOUNT))
                               .setScale(2, RoundingMode.HALF_UP);
        }

        var fuelSurcharge = baseCost
                .multiply(rate.fuelSurchargePercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new ShippingEstimate(rail, baseCost, fuelSurcharge, rate.handlingFee(), rate.baseTransitDays());
    }
}
