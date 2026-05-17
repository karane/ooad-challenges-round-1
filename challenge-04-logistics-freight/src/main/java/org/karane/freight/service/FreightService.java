package org.karane.freight.service;

import org.karane.freight.domain.ShippingEstimate;
import org.karane.freight.domain.Shipment;
import org.karane.freight.pricing.FreightRateProvider;
import org.karane.freight.strategy.FreightCalculator;

import java.util.List;

public class FreightService {

    private final List<FreightCalculator> calculators;
    private final FreightRateProvider rateProvider;

    public FreightService(List<FreightCalculator> calculators, FreightRateProvider rateProvider) {
        this.calculators  = List.copyOf(calculators);
        this.rateProvider = rateProvider;
    }

    public List<ShippingEstimate> quoteAll(Shipment shipment) {
        return calculators.stream()
                .map(calc -> calc.calculate(shipment, rateProvider.getRate(calc.transportationType())))
                .toList();
    }

    public ShippingEstimate cheapest(Shipment shipment) {
        return quoteAll(shipment).stream()
                .min((a, b) -> a.totalCost().compareTo(b.totalCost()))
                .orElseThrow(() -> new IllegalStateException("No calculators registered"));
    }

    public ShippingEstimate fastest(Shipment shipment) {
        return quoteAll(shipment).stream()
                .min((a, b) -> Integer.compare(a.estimatedDays(), b.estimatedDays()))
                .orElseThrow(() -> new IllegalStateException("No calculators registered"));
    }
}
