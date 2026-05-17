package org.karane.freight;

import org.karane.freight.domain.ShippingEstimate;
import org.karane.freight.domain.Shipment;
import org.karane.freight.domain.TransportationType;
import org.karane.freight.pricing.FreightRate;
import org.karane.freight.pricing.InMemoryFreightRateProvider;
import org.karane.freight.service.FreightService;
import org.karane.freight.strategy.BoatFreightCalculator;
import org.karane.freight.strategy.RailFreightCalculator;
import org.karane.freight.strategy.TruckFreightCalculator;

import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        var boat  = new TransportationType.Boat("Port of LA", "Port of Rotterdam");
        var truck = new TransportationType.Truck(20_000);
        var rail  = new TransportationType.Rail("US Freight Network");

        var rateProvider = new InMemoryFreightRateProvider();

        var service = new FreightService(
                List.of(
                        new BoatFreightCalculator(boat),
                        new TruckFreightCalculator(truck),
                        new RailFreightCalculator(rail)
                ),
                rateProvider
        );

        String[] labels = {
            "Small parcel (5 kg, 40 x 30 x 20 cm)",
            "Heavy machinery (800 kg, 200 x 150 x 120 cm)",
            "Bulk grain (2000 kg, 300 x 200 x 150 cm)",
            "Oversize truck load (25000 kg, 500 x 250 x 280 cm)"
        };

        Shipment[] shipments = {
            new Shipment(bd("5"),     bd("40"),  bd("30"),  bd("20"),  "Small parcel"),
            new Shipment(bd("800"),   bd("200"), bd("150"), bd("120"), "Heavy machinery"),
            new Shipment(bd("2000"),  bd("300"), bd("200"), bd("150"), "Bulk grain"),
            new Shipment(bd("25000"), bd("500"), bd("250"), bd("280"), "Oversize load")
        };

        printHeader("=== Initial Rates ===");
        for (int i = 0; i < labels.length; i++) printScenario(service, labels[i], shipments[i]);

        System.out.println("\n>>> Fuel crisis: Boat fuel surcharge jumps from 8.5% to 25%\n");
        rateProvider.updateRate(boat, new FreightRate(bd("0.80"), bd("120.00"), bd("25.0"), bd("75.00"), 21));

        printHeader("=== Updated Rates (after fuel crisis) ===");
        for (int i = 0; i < labels.length; i++) printScenario(service, labels[i], shipments[i]);
    }

    private static void printHeader(String header) {
        System.out.println("\n" + header);
        System.out.println("=".repeat(header.length()));
    }

    private static void printScenario(FreightService service, String label, Shipment shipment) {
        System.out.printf("%n  Shipment: %s%n", label);
        System.out.printf("    Weight: %.1f kg  |  Volume: %.4f m**3  |  Billable weight: %.1f kg%n",
                shipment.weightKg(), shipment.volumeM3(), shipment.billableWeightKg());

        System.out.printf("  %-10s %12s %12s %12s %10s%n", "Mode", "Base", "Fuel", "Handling", "TOTAL / Days");
        System.out.println("  " + "-".repeat(60));

        for (ShippingEstimate q : service.quoteAll(shipment)) {
            System.out.printf("  %-10s %12.2f %12.2f %12.2f   $%8.2f / %d days%n",
                    q.transportationType().label(),
                    q.baseCost(), q.fuelSurcharge(), q.handlingFee(),
                    q.totalCost(), q.estimatedDays());
        }

        var cheapest = service.cheapest(shipment);
        var fastest  = service.fastest(shipment);
        System.out.printf("  >> Cheapest: %s ($%.2f)   Fastest: %s (%d days)%n",
                cheapest.transportationType().label(), cheapest.totalCost(),
                fastest.transportationType().label(), fastest.estimatedDays());
    }

    private static BigDecimal bd(String val) { return new BigDecimal(val); }
}
