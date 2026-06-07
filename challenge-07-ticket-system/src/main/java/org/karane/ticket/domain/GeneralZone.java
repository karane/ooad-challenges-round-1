package org.karane.ticket.domain;

import java.math.BigDecimal;

/** Main seated area. */
public record GeneralZone(String name, int rows, int seatsPerRow) implements Zone {
    public GeneralZone {
        if (rows <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("rows and seatsPerRow must be positive");
        }
    }

    @Override
    public BigDecimal priceMultiplier() {
        return new BigDecimal("1.00");
    }
}
