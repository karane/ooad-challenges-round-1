package org.karane.ticket.service;

import org.karane.ticket.domain.*;
import org.karane.ticket.pricing.PricingStrategy;
import org.karane.ticket.repository.TicketRepository;

import java.util.List;

public class TicketingService {

    private final TicketRepository repository;
    private final PricingStrategy  pricingStrategy;

    public TicketingService(TicketRepository repository, PricingStrategy pricingStrategy) {
        this.repository      = repository;
        this.pricingStrategy = pricingStrategy;
    }

    public BookingResult buy(Show show, Seat seat, String holderName) {
        if (holderName == null || holderName.isBlank()) {
            return BookingResult.failure("Holder name must not be blank");
        }

        var entryOpt = show.findEntry(seat);
        if (entryOpt.isEmpty()) {
            return BookingResult.failure("Seat %s does not exist in this show".formatted(seat.label()));
        }

        var entry = entryOpt.get();
        if (entry.status() == SeatStatus.SOLD) {
            return BookingResult.failure("Seat %s is already sold".formatted(seat.label()));
        }

        var price  = pricingStrategy.price(show, seat.zone());
        var ticket = Ticket.issue(show, seat, holderName, price);

        entry.sell(holderName);
        repository.save(ticket);

        return BookingResult.success(ticket);
    }

    public BookingResult cancel(Show show, String ticketId) {
        var ticketOpt = repository.findById(ticketId);
        if (ticketOpt.isEmpty()) {
            return BookingResult.failure("Ticket %s not found".formatted(ticketId));
        }

        var ticket = ticketOpt.get();
        show.findEntry(ticket.seat()).ifPresent(SeatEntry::release);
        repository.remove(ticketId);

        return BookingResult.success(ticket);
    }

    public List<SeatEntry> availableSeats(Show show, Zone zone) {
        return show.availableIn(zone);
    }

    public TicketRepository repository() {
        return repository;
    }
}
