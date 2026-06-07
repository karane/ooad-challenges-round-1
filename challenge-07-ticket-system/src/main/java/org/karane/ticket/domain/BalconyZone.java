package org.karane.ticket.domain;

import java.math.BigDecimal;

/** Upper-level seating, furthest from stage. */
public record BalconyZone(String name, int rows, int seatsPerRow) implements Zone {
    public BalconyZone {
        if (rows <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("rows and seatsPerRow must be positive");
        }
    }

    @Override
    public BigDecimal priceMultiplier() {
        return new BigDecimal("0.70");
    }
}
