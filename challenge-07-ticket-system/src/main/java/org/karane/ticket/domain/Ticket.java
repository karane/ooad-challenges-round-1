package org.karane.ticket.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Ticket(
        String id,
        Show show,
        Seat seat,
        String holderName,
        BigDecimal price,
        LocalDateTime purchasedAt
) {
    public static Ticket issue(Show show, Seat seat, String holderName, BigDecimal price) {
        return new Ticket(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                show, seat, holderName, price, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Ticket[%s | %s | %s | %s | $%.2f]"
                .formatted(id, show.name(), seat.label(), holderName, price);
    }
}
