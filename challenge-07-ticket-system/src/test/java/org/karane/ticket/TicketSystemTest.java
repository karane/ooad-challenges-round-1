package org.karane.ticket;

import org.junit.jupiter.api.*;
import org.karane.ticket.domain.*;
import org.karane.ticket.pricing.DynamicPricingStrategy;
import org.karane.ticket.pricing.FlatPricingStrategy;
import org.karane.ticket.repository.TicketRepository;
import org.karane.ticket.service.TicketingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketSystemTest {

    private static final VipZone     VIP     = new VipZone("VIP", 2, 5);         // 10 seats
    private static final GeneralZone GENERAL = new GeneralZone("General", 3, 10); // 30 seats

    private static Show buildShow() {
        return Show.builder("Test Show")
                .venue("Test Venue")
                .dateTime(LocalDateTime.of(2026, 1, 1, 20, 0))
                .addZone(VIP)
                .addZone(GENERAL)
                .build();
    }

    private static TicketingService buildService(Show show) {
        return new TicketingService(
                new TicketRepository(),
                new FlatPricingStrategy(new BigDecimal("100.00")));
    }

    @Nested
    @DisplayName("Zone")
    class ZoneTests {

        @Test
        @DisplayName("capacity equals rows times seatsPerRow")
        void capacity() {
            assertEquals(10, VIP.capacity());
            assertEquals(30, GENERAL.capacity());
        }

        @Test
        @DisplayName("each zone type has a distinct multiplier")
        void distinctMultipliers() {
            var multipliers = List.of(
                    new VipZone("v", 1, 1).priceMultiplier(),
                    new FloorZone("f", 1, 1).priceMultiplier(),
                    new GeneralZone("g", 1, 1).priceMultiplier(),
                    new BalconyZone("b", 1, 1).priceMultiplier());
            assertEquals(4, multipliers.stream().distinct().count());
        }

        @Test
        @DisplayName("VIP multiplier is highest")
        void vipHighestMultiplier() {
            assertTrue(new VipZone("v", 1, 1).priceMultiplier()
                    .compareTo(new GeneralZone("g", 1, 1).priceMultiplier()) > 0);
        }

        @Test
        @DisplayName("zero rows rejected")
        void zeroRowsRejected() {
            assertThrows(IllegalArgumentException.class, () -> new VipZone("v", 0, 5));
        }
    }

    @Nested
    @DisplayName("Show Builder")
    class ShowBuilderTests {

        @Test
        @DisplayName("missing venue throws")
        void missingVenueThrows() {
            assertThrows(IllegalStateException.class, () ->
                    Show.builder("X")
                            .dateTime(LocalDateTime.now())
                            .addZone(VIP)
                            .build());
        }

        @Test
        @DisplayName("missing dateTime throws")
        void missingDateTimeThrows() {
            assertThrows(IllegalStateException.class, () ->
                    Show.builder("X").venue("V").addZone(VIP).build());
        }

        @Test
        @DisplayName("show with no zones throws")
        void noZonesThrows() {
            assertThrows(IllegalStateException.class, () ->
                    Show.builder("X").venue("V").dateTime(LocalDateTime.now()).build());
        }

        @Test
        @DisplayName("seating plan is initialised with all seats available and correct total capacity")
        void seatingPlanInitialised() {
            var show = buildShow();
            // VIP: 2x5=10, General: 3x10=30, total 40
            assertEquals(40, show.seatingPlan().size());
            assertEquals(40, show.totalCapacity());
            assertTrue(show.seatingPlan().values().stream()
                    .allMatch(e -> e.status() == SeatStatus.AVAILABLE));
        }
    }

    @Nested
    @DisplayName("PricingStrategy")
    class PricingStrategyTests {

        private final FlatPricingStrategy flat =
                new FlatPricingStrategy(new BigDecimal("100.00"));

        @Test
        @DisplayName("flat price equals base times zone multiplier: VIP x3, General x1, Balcony x0.70")
        void flatPriceByZoneType() {
            var balcony  = new BalconyZone("B", 1, 1);
            var showAll  = Show.builder("S").venue("V").dateTime(LocalDateTime.now())
                    .addZone(VIP).addZone(GENERAL).addZone(balcony).build();
            assertEquals(0, new BigDecimal("300.00").compareTo(flat.price(showAll, VIP)));
            assertEquals(0, new BigDecimal("100.00").compareTo(flat.price(showAll, GENERAL)));
            assertEquals(0, new BigDecimal("70.00").compareTo(flat.price(showAll, balcony)));
        }

        @Test
        @DisplayName("flat pricing rejects non-positive base price")
        void nonPositiveBaseRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new FlatPricingStrategy(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("dynamic pricing applies surcharge at threshold")
        void dynamicSurchargeApplied() {
            // tiny zone: 1 row × 2 seats = 2 seats; threshold = 50%
            var tinyZone = new GeneralZone("Tiny", 1, 2);
            var show = Show.builder("S").venue("V").dateTime(LocalDateTime.now()).addZone(tinyZone).build();
            var svc = new TicketingService(new TicketRepository(),
                    new DynamicPricingStrategy(flat, 0.5, 25.0));

            // sell seat 1 → 50% full → surcharge kicks in for seat 2
            svc.buy(show, new Seat(tinyZone, 1, 1), "A");
            var result = svc.buy(show, new Seat(tinyZone, 1, 2), "B");
            var ticket = ((BookingSuccess) result).ticket();
            // base General = 100, surcharge +25% = 125
            assertEquals(0, new BigDecimal("125.00").compareTo(ticket.price()));
        }

        @Test
        @DisplayName("dynamic pricing does NOT surcharge below threshold")
        void dynamicNoSurchargeBelow() {
            var show = buildShow();  // GENERAL has 30 seats; nobody sold yet
            var svc  = new TicketingService(new TicketRepository(),
                    new DynamicPricingStrategy(flat, 0.8, 25.0));
            var result = svc.buy(show, new Seat(GENERAL, 1, 1), "A");
            var ticket = ((BookingSuccess) result).ticket();
            assertEquals(0, new BigDecimal("100.00").compareTo(ticket.price()));
        }
    }

    @Nested
    @DisplayName("TicketingService")
    class TicketingServiceTests {

        private Show show;
        private TicketingService service;

        @BeforeEach
        void setUp() {
            show    = buildShow();
            service = buildService(show);
        }

        @Test
        @DisplayName("buy returns Success, marks seat SOLD, and persists ticket")
        void buyHappyPath() {
            var seat   = new Seat(VIP, 1, 1);
            var result = service.buy(show, seat, "Alice");
            assertInstanceOf(BookingSuccess.class, result);
            var ticket = ((BookingSuccess) result).ticket();
            assertEquals(SeatStatus.SOLD, show.findEntry(seat).get().status());
            assertTrue(service.repository().findById(ticket.id()).isPresent());
        }

        @Test
        @DisplayName("buying an already-sold seat returns Failure")
        void doubleBuyReturnsFailure() {
            var seat = new Seat(VIP, 1, 1);
            service.buy(show, seat, "Alice");
            var result = service.buy(show, seat, "Bob");
            assertInstanceOf(BookingFailure.class, result);
        }

        @Test
        @DisplayName("buying non-existent seat returns Failure")
        void nonExistentSeatReturnsFailure() {
            // row 99 does not exist in a 2-row zone
            var result = service.buy(show, new Seat(VIP, 99, 1), "Alice");
            assertInstanceOf(BookingFailure.class, result);
        }

        @Test
        @DisplayName("blank holder name returns Failure")
        void blankHolderReturnsFailure() {
            var result = service.buy(show, new Seat(VIP, 1, 1), "  ");
            assertInstanceOf(BookingFailure.class, result);
        }

        @Test
        @DisplayName("cancel releases seat to AVAILABLE and removes ticket from repository")
        void cancelHappyPath() {
            var seat   = new Seat(VIP, 1, 1);
            var ticket = ((BookingSuccess) service.buy(show, seat, "Alice")).ticket();
            service.cancel(show, ticket.id());
            assertEquals(SeatStatus.AVAILABLE, show.findEntry(seat).get().status());
            assertTrue(service.repository().findById(ticket.id()).isEmpty());
        }

        @Test
        @DisplayName("seat released by cancellation can be bought again")
        void cancelledSeatRebuyable() {
            var seat   = new Seat(VIP, 1, 1);
            var ticket = ((BookingSuccess) service.buy(show, seat, "Alice")).ticket();
            service.cancel(show, ticket.id());
            var result = service.buy(show, seat, "Bob");
            assertInstanceOf(BookingSuccess.class, result);
        }

        @Test
        @DisplayName("cancel with unknown ticket id returns Failure")
        void cancelUnknownIdReturnsFailure() {
            var result = service.cancel(show, "NOPE");
            assertInstanceOf(BookingFailure.class, result);
        }

        @Test
        @DisplayName("availableSeats decreases after a purchase")
        void availableSeatsDecrease() {
            int before = service.availableSeats(show, VIP).size();
            service.buy(show, new Seat(VIP, 1, 1), "Alice");
            int after  = service.availableSeats(show, VIP).size();
            assertEquals(before - 1, after);
        }

        @Test
        @DisplayName("seat from different zone is not found when looked up via that zone's seat")
        void seatLabelIsZoneScoped() {
            // VIP row1 seat1 ≠ GENERAL row1 seat1 — different labels
            var vipSeat  = new Seat(VIP,     1, 1);
            var genSeat  = new Seat(GENERAL, 1, 1);
            assertNotEquals(vipSeat.label(), genSeat.label());
        }
    }

    @Nested
    @DisplayName("TicketRepository")
    class TicketRepositoryTests {

        private Show show;
        private TicketingService service;

        @BeforeEach
        void setUp() {
            show    = buildShow();
            service = buildService(show);
        }

        @Test
        @DisplayName("findByHolder returns tickets for that holder only")
        void findByHolder() {
            service.buy(show, new Seat(VIP,     1, 1), "Alice");
            service.buy(show, new Seat(GENERAL, 1, 1), "Alice");
            service.buy(show, new Seat(VIP,     1, 2), "Bob");

            var aliceTickets = service.repository().findByHolder("Alice");
            assertEquals(2, aliceTickets.size());
            assertTrue(aliceTickets.stream().allMatch(t -> t.holderName().equals("Alice")));
        }

        @Test
        @DisplayName("findByShow returns only tickets for that show")
        void findByShow() {
            var show2 = buildShow();
            var svc2  = buildService(show2);

            service.buy(show,  new Seat(VIP, 1, 1), "Alice");
            svc2.buy(show2, new Seat(VIP, 1, 1), "Bob");

            assertEquals(1, service.repository().findByShow(show).size());
            assertEquals(1, svc2.repository().findByShow(show2).size());
        }

        @Test
        @DisplayName("countByShow matches actual purchases")
        void countByShow() {
            service.buy(show, new Seat(VIP,     1, 1), "A");
            service.buy(show, new Seat(VIP,     1, 2), "B");
            service.buy(show, new Seat(GENERAL, 1, 1), "C");
            assertEquals(3, service.repository().countByShow(show));
        }
    }
}
