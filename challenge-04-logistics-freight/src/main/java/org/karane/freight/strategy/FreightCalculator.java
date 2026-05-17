package org.karane.freight.strategy;

import org.karane.freight.domain.ShippingEstimate;
import org.karane.freight.domain.Shipment;
import org.karane.freight.domain.TransportationType;
import org.karane.freight.pricing.FreightRate;

public interface FreightCalculator {
    TransportationType transportationType();
    ShippingEstimate calculate(Shipment shipment, FreightRate rate);
}
