package org.karane.freight.pricing;

import org.karane.freight.domain.TransportationType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFreightRateProvider implements FreightRateProvider {

    private final Map<String, FreightRate> rates = new ConcurrentHashMap<>();

    public InMemoryFreightRateProvider() {
        // Defaults expressed as USD per unit — kept intentionally adjustable
        rates.put("Boat",  new FreightRate(bd("0.80"),  bd("120.00"), bd("8.5"),  bd("75.00"),  21));
        rates.put("Truck", new FreightRate(bd("2.50"),  bd("350.00"), bd("15.0"), bd("50.00"),   3));
        rates.put("Rail",  new FreightRate(bd("1.20"),  bd("180.00"), bd("10.0"), bd("60.00"),  10));
    }

    @Override
    public FreightRate getRate(TransportationType type) {
        var key = keyFor(type);
        var rate = rates.get(key);
        if (rate == null) throw new IllegalArgumentException("No rate registered for: " + key);
        return rate;
    }

    @Override
    public void updateRate(TransportationType type, FreightRate rate) {
        rates.put(keyFor(type), rate);
    }

    private static String keyFor(TransportationType type) {
        return switch (type) {
            case TransportationType.Boat   ignored -> "Boat";
            case TransportationType.Truck  ignored -> "Truck";
            case TransportationType.Rail   ignored -> "Rail";
        };
    }

    private static BigDecimal bd(String val) { return new BigDecimal(val); }
}
