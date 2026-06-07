package org.karane.ticket.domain;

import java.math.BigDecimal;

public record VipZone(String name, int rows, int seatsPerRow) implements Zone {
    public VipZone {
        if (rows <= 0 || seatsPerRow <= 0) {
            throw new IllegalArgumentException("rows and seatsPerRow must be positive");
        }
    }

    @Override
    public BigDecimal priceMultiplier() {
        return new BigDecimal("3.00");
    }
}
