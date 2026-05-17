package org.karane.freight.strategy;

import org.karane.freight.domain.ShippingEstimate;
import org.karane.freight.domain.Shipment;
import org.karane.freight.domain.TransportationType;
import org.karane.freight.pricing.FreightRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BoatFreightCalculator implements FreightCalculator {

    private final TransportationType.Boat boat;

    public BoatFreightCalculator(TransportationType.Boat boat) {
        this.boat = boat;
    }

    @Override
    public TransportationType transportationType() { return boat; }

    @Override
    public ShippingEstimate calculate(Shipment shipment, FreightRate rate) {
        // Boat charges whichever is greater: weight-based or volume-based cost
        var weightCost  = shipment.billableWeightKg().multiply(rate.ratePerKg());
        var volumeCost  = shipment.volumeM3().multiply(rate.ratePerM3());
        var baseCost    = weightCost.max(volumeCost).setScale(2, RoundingMode.HALF_UP);

        var fuelSurcharge = baseCost
                .multiply(rate.fuelSurchargePercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Extra day for each full 5 m**3 of volume
        int volumeExtra = shipment.volumeM3().intValue() / 5;
        int transitDays = rate.baseTransitDays() + volumeExtra;

        return new ShippingEstimate(boat, baseCost, fuelSurcharge, rate.handlingFee(), transitDays);
    }
}
