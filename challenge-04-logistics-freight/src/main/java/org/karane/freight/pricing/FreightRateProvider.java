package org.karane.freight.pricing;

import org.karane.freight.domain.TransportationType;

public interface FreightRateProvider {
    FreightRate getRate(TransportationType type);
    void updateRate(TransportationType type, FreightRate rate);
}
