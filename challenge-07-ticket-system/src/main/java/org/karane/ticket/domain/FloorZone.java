package org.karane.ticket.domain;

import java.math.BigDecimal;

/** Standing / close-to-stage area. */
public record FloorZone(String name, int rows, int seatsPerRow) implements Zone {
    public FloorZone {
        if (rows <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("rows and seatsPerRow must be positive");
        }
    }

    @Override
    public BigDecimal priceMultiplier() {
        return new BigDecimal("1.50");
    }
}
