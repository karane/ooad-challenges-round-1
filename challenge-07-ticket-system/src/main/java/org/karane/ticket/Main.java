package org.karane.ticket;

import org.karane.ticket.domain.*;
import org.karane.ticket.pricing.DynamicPricingStrategy;
import org.karane.ticket.pricing.FlatPricingStrategy;
import org.karane.ticket.repository.TicketRepository;
import org.karane.ticket.service.TicketingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Zones
        var vip     = new VipZone("VIP", 2, 10);          
        var floor   = new FloorZone("Floor", 5, 20);       
        var general = new GeneralZone("General", 10, 30);  
        var balcony = new BalconyZone("Balcony", 5, 25);   

        // Show: Rock Concert
        var concert = Show.builder("Rock Night -- The Thunder Cats")
                .venue("Madison Square Garden")
                .dateTime(LocalDateTime.of(2026, 7, 15, 20, 0))
                .addZone(vip).addZone(floor).addZone(general).addZone(balcony)
                .build();

        // Show: Jazz Evening
        var jazzVip  = new VipZone("VIP Lounge", 1, 5);    //  5 seats
        var jazzMain = new GeneralZone("Main Hall", 8, 15); // 120 seats
        var jazz = Show.builder("Jazz Night -- Blue Moon Quartet")
                .venue("Blue Note Club")
                .dateTime(LocalDateTime.of(2026, 8, 3, 19, 30))
                .addZone(jazzVip).addZone(jazzMain)
                .build();

        // Service with flat pricing ($100 base)
        var flatService = new TicketingService(
                new TicketRepository(),
                new FlatPricingStrategy(new BigDecimal("100.00")));

        // Service with dynamic pricing (surcharge after 80% full)
        var dynamicService = new TicketingService(
                new TicketRepository(),
                new DynamicPricingStrategy(
                        new FlatPricingStrategy(new BigDecimal("100.00")), 0.8, 25.0));

        printHeader("Concert -- available VIP seats (before any sales)");
        flatService.availableSeats(concert, vip).stream().limit(5)
                .forEach(e -> System.out.println("  " + e.seat().label()));

        printHeader("Buying tickets -- flat pricing");
        buy(flatService, concert, new Seat(vip,     1, 1), "Alice");
        buy(flatService, concert, new Seat(vip,     1, 2), "Bob");
        buy(flatService, concert, new Seat(general, 1, 1), "Carol");
        buy(flatService, concert, new Seat(balcony, 2, 5), "Dave");
        buy(flatService, jazz,    new Seat(jazzVip, 1, 1), "Eva");

        printHeader("Attempting to buy an already-sold seat");
        buy(flatService, concert, new Seat(vip, 1, 1), "Frank");

        printHeader("Attempting to buy a seat from the wrong show");
        buy(flatService, jazz, new Seat(vip, 1, 1), "Grace");  

        printHeader("Cancelling Alice's ticket then re-buying it");
        var aliceTicket = flatService.repository().findByHolder("Alice").getFirst();
        var cancelResult = flatService.cancel(concert, aliceTicket.id());
        printResult(cancelResult);
        buy(flatService, concert, new Seat(vip, 1, 1), "Hank");

        printHeader("Dynamic pricing -- selling 80%+ of VIP to trigger surcharge");
        // jazz VIP has 5 seats; sell 4 (80%) then the 5th should be more expensive
        buy(dynamicService, jazz, new Seat(jazzVip, 1, 1), "P1");
        buy(dynamicService, jazz, new Seat(jazzVip, 1, 2), "P2");
        buy(dynamicService, jazz, new Seat(jazzVip, 1, 3), "P3");
        buy(dynamicService, jazz, new Seat(jazzVip, 1, 4), "P4");
        
        System.out.printf("  VIP fill: %d/%d%n", jazz.soldCountIn(jazzVip), jazzVip.capacity());
        buy(dynamicService, jazz, new Seat(jazzVip, 1, 5), "P5 (surcharge!)");

        printHeader("Ticket repository -- all tickets for the concert");
        flatService.repository().findByShow(concert)
                .forEach(t -> System.out.println("  " + t));

        printHeader("Inventory summary");
        for (var show : List.of(concert, jazz)) {
            System.out.printf("  %s -- total capacity: %d%n", show.name(), show.totalCapacity());
            for (var zone : show.zones()) {
                long sold      = show.soldCountIn(zone);
                long available = show.availableIn(zone).size();
                System.out.printf("    %-15s  capacity: %3d  sold: %3d  available: %3d%n",
                        zone.name(), zone.capacity(), sold, available);
            }
        }
    }

    private static void buy(TicketingService svc, Show show, Seat seat, String holder) {
        printResult("BUY", svc.buy(show, seat, holder));
    }

    private static void printResult(BookingResult result) {
        printResult("CANCEL", result);
    }

    private static void printResult(String op, BookingResult result) {
        switch (result) {
            case BookingSuccess s ->
                System.out.println("  [OK]   [" + op + "] " + s.ticket());
            case BookingFailure f ->
                System.out.println("  [FAIL] [" + op + "] " + f.reason());
        }
    }

    private static void printHeader(String title) {
        System.out.println("\n--- " + title + " " + "--".repeat(Math.max(0, 58 - title.length())));
    }
}
