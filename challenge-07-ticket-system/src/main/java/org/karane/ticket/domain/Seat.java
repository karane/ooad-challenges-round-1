package org.karane.ticket.domain;

public record Seat(Zone zone, int row, int number) {

    public Seat {
        if (row < 1) {
            throw new IllegalArgumentException("row must be >= 1");
        }
        if (number < 1) {
            throw new IllegalArgumentException("number must be >= 1");
        }
    }

    public String label() {
        return "%s-R%dS%d".formatted(zone.name(), row, number);
    }
}
