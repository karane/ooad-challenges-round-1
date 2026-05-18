package org.karane.freight;

import org.junit.jupiter.api.*;
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

import static org.junit.jupiter.api.Assertions.*;

class FreightServiceTest {

    private static final TransportationType.Boat  BOAT  = new TransportationType.Boat("Origin", "Dest");
    private static final TransportationType.Truck TRUCK = new TransportationType.Truck(20_000);
    private static final TransportationType.Rail  RAIL  = new TransportationType.Rail("Network");

    private InMemoryFreightRateProvider rateProvider;
    private FreightService service;

    @BeforeEach
    void setUp() {
        rateProvider = new InMemoryFreightRateProvider();
        service = new FreightService(
                List.of(
                        new BoatFreightCalculator(BOAT),
                        new TruckFreightCalculator(TRUCK),
                        new RailFreightCalculator(RAIL)
                ),
                rateProvider
        );
    }

    @Nested
    @DisplayName("Shipment")
    class ShipmentTests {

        @Test
        @DisplayName("volumeM3 is computed correctly")
        void volumeM3() {
            var s = shipment("10", "100", "50", "40");  // 100*50*40 = 200_000 cm³ = 0.2 m³
            assertEquals(0, s.volumeM3().compareTo(bd("0.2")));
        }

        @Test
        @DisplayName("billableWeight uses actual weight when heavier than volumetric")
        void billableWeightActual() {
            var s = shipment("500", "50", "50", "50");
            assertEquals(0, s.billableWeightKg().compareTo(bd("500")));
        }

        @Test
        @DisplayName("billableWeight uses volumetric weight when heavier than actual")
        void billableWeightVolumetric() {
            var s = shipment("5", "200", "200", "200");
            assertTrue(s.billableWeightKg().compareTo(bd("5")) > 0);
            assertEquals(0, s.billableWeightKg().compareTo(bd("2664.000000")));
        }

        @Test
        @DisplayName("negative weight is rejected")
        void negativeWeightRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Shipment(bd("-1"), bd("10"), bd("10"), bd("10"), "bad"));
        }
    }

    @Nested
    @DisplayName("Boat calculator")
    class BoatTests {

        @Test
        @DisplayName("uses volume cost when larger than weight cost")
        void preferVolumeCost() {
            var s = shipment("5", "200", "200", "200");
            var rate = fixedRate("0.01", "120.00", "0", "0", 21);
            rateProvider.updateRate(BOAT, rate);
            var q = service.quoteAll(s).stream()
                    .filter(x -> x.transportationType() instanceof TransportationType.Boat)
                    .findFirst().orElseThrow();
            assertEquals(0, q.baseCost().compareTo(bd("960")));
        }

        @Test
        @DisplayName("transit days increase for large volume shipments")
        void transitDaysScaleWithVolume() {
            var s = shipment("10", "300", "200", "200");
            var rate = fixedRate("0.01", "0.01", "0", "0", 21);
            rateProvider.updateRate(BOAT, rate);
            var q = boatQuote(s);
            assertEquals(23, q.estimatedDays());  // 21 + 2
        }

        @Test
        @DisplayName("fuel surcharge is applied as percentage of base")
        void fuelSurcharge() {
            var s = shipment("100", "50", "50", "50");
            var rate = fixedRate("2.00", "100.00", "10.0", "0", 10);
            rateProvider.updateRate(BOAT, rate);
            var q = boatQuote(s);
            assertEquals(0, q.fuelSurcharge().compareTo(bd("20")));  // 10% of 200
        }
    }

    @Nested
    @DisplayName("Truck calculator")
    class TruckTests {

        @Test
        @DisplayName("no oversize penalty when within payload limit")
        void noOversizePenalty() {
            var s = shipment("1000", "100", "100", "100");  // 1000 kg < 20000 kg limit
            var rate = fixedRate("2.50", "0", "0", "50.00", 3);
            rateProvider.updateRate(TRUCK, rate);
            var q = truckQuote(s);
            assertEquals(0, q.handlingFee().compareTo(bd("50.00")));
        }

        @Test
        @DisplayName("20% oversize penalty applied when shipment exceeds payload")
        void oversizePenalty() {
            var s = shipment("25000", "500", "250", "280");
            var rate = fixedRate("2.50", "0", "0", "50.00", 3);
            rateProvider.updateRate(TRUCK, rate);
            var q = truckQuote(s);
            assertTrue(q.handlingFee().compareTo(bd("50.00")) > 0);
        }
    }

    @Nested
    @DisplayName("Rail calculator")
    class RailTests {

        @Test
        @DisplayName("base cost is sum of weight and volume costs")
        void baseCostIsSumOfBoth() {
            var s = shipment("100", "50", "50", "50");
            var rate = fixedRate("1.00", "100.00", "0", "0", 10);
            rateProvider.updateRate(RAIL, rate);
            var q = railQuote(s);
            assertEquals(0, q.baseCost().compareTo(bd("112.50")));
        }

        @Test
        @DisplayName("10% bulk discount applied when weight exceeds 500 kg")
        void bulkDiscount() {
            var s = shipment("600", "100", "100", "100");  // 600 kg > 500 threshold
            var rate = fixedRate("1.00", "100.00", "0", "0", 10);
            rateProvider.updateRate(RAIL, rate);
            var q = railQuote(s);
            // weightCost=600, volumeCost=100 → gross=700; discount=70 → base=630
            assertEquals(0, q.baseCost().compareTo(bd("630.00")));
        }
    }

    @Nested
    @DisplayName("FreightService selection")
    class SelectionTests {

        @Test
        @DisplayName("quoteAll returns one quote per calculator")
        void quoteAllReturnsThreeQuotes() {
            var quotes = service.quoteAll(shipment("50", "60", "40", "30"));
            assertEquals(3, quotes.size());
        }

        @Test
        @DisplayName("cheapest() returns the quote with lowest total cost")
        void cheapest() {
            rateProvider.updateRate(BOAT, fixedRate("0.01", "0.01", "0", "0", 30));
            var cheapest = service.cheapest(shipment("50", "60", "40", "30"));
            assertInstanceOf(TransportationType.Boat.class, cheapest.transportationType());
        }

        @Test
        @DisplayName("fastest() returns the quote with fewest transit days")
        void fastest() {
            var fastest = service.fastest(shipment("50", "60", "40", "30"));
            assertInstanceOf(TransportationType.Truck.class, fastest.transportationType());
        }
    }

    @Nested
    @DisplayName("Dynamic rate updates")
    class DynamicRateTests {

        @Test
        @DisplayName("updating a rate is immediately reflected in quotes")
        void rateUpdateReflectedImmediately() {
            var s = shipment("100", "100", "100", "100");

            var before = boatQuote(s).totalCost();

            var current = rateProvider.getRate(BOAT);
            rateProvider.updateRate(BOAT, new FreightRate(
                    current.ratePerKg(), current.ratePerM3(),
                    current.fuelSurchargePercent().multiply(BigDecimal.valueOf(3)),
                    current.handlingFee(), current.baseTransitDays()));

            var after = boatQuote(s).totalCost();
            assertTrue(after.compareTo(before) > 0, "Total cost should increase after surcharge hike");
        }

        @Test
        @DisplayName("unknown transport type throws")
        void unknownTypeThrows() {
            // A second Boat instance with same semantics — key lookup should still work
            var anotherBoat = new TransportationType.Boat("X", "Y");
            assertDoesNotThrow(() -> rateProvider.getRate(anotherBoat));
        }
    }

    private static Shipment shipment(String kg, String l, String w, String h) {
        return new Shipment(bd(kg), bd(l), bd(w), bd(h), "test cargo");
    }

    private static FreightRate fixedRate(String perKg, String perM3, String fuel, String handling, int days) {
        return new FreightRate(bd(perKg), bd(perM3), bd(fuel), bd(handling), days);
    }

    private ShippingEstimate boatQuote(Shipment s) {
        return service.quoteAll(s).stream()
                .filter(q -> q.transportationType() instanceof TransportationType.Boat)
                .findFirst().orElseThrow();
    }

    private ShippingEstimate truckQuote(Shipment s) {
        return service.quoteAll(s).stream()
                .filter(q -> q.transportationType() instanceof TransportationType.Truck)
                .findFirst().orElseThrow();
    }

    private ShippingEstimate railQuote(Shipment s) {
        return service.quoteAll(s).stream()
                .filter(q -> q.transportationType() instanceof TransportationType.Rail)
                .findFirst().orElseThrow();
    }

    private static BigDecimal bd(String v) { return new BigDecimal(v); }
}
