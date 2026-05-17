package org.karane.freight.strategy;

import org.karane.freight.domain.ShippingEstimate;
import org.karane.freight.domain.Shipment;
import org.karane.freight.domain.TransportationType;
import org.karane.freight.pricing.FreightRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TruckFreightCalculator implements FreightCalculator {

    private final TransportationType.Truck truck;

    public TruckFreightCalculator(TransportationType.Truck truck) {
        this.truck = truck;
    }

    @Override
    public TransportationType transportationType() { return truck; }

    @Override
    public ShippingEstimate calculate(Shipment shipment, FreightRate rate) {
        var baseCost = shipment.billableWeightKg()
                               .multiply(rate.ratePerKg())
                               .setScale(2, RoundingMode.HALF_UP);

        // 20% oversize penalty when shipment weight exceeds truck payload
        var oversizeSurcharge = BigDecimal.ZERO;
        if (shipment.weightKg().compareTo(BigDecimal.valueOf(truck.maxPayloadKg())) > 0) {
            oversizeSurcharge = baseCost.multiply(BigDecimal.valueOf(0.20)).setScale(2, RoundingMode.HALF_UP);
        }

        var fuelSurcharge = baseCost
                .multiply(rate.fuelSurchargePercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        var totalHandling = rate.handlingFee().add(oversizeSurcharge);

        return new ShippingEstimate(truck, baseCost, fuelSurcharge, totalHandling, rate.baseTransitDays());
    }
}
