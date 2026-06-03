package org.karane.guitar.domain;

import java.math.BigDecimal;

public final class Guitar {

    public enum Status { AVAILABLE, RESERVED, SOLD }

    private final String serialNumber;
    private final GuitarSpec spec;
    private final BigDecimal price;
    private Status status;

    public Guitar(String serialNumber, GuitarSpec spec, BigDecimal price) {
        this.serialNumber = serialNumber;
        this.spec         = spec;
        this.price        = price;
        this.status       = Status.AVAILABLE;
    }

    public String serialNumber() { return serialNumber; }
    public GuitarSpec spec()     { return spec; }
    public BigDecimal price()    { return price; }
    public Status status()       { return status; }

    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "Guitar[%s | %s %s | %s | $%.2f | %s]".formatted(
                serialNumber,
                spec.type(), spec.bodyStyle(),
                spec.finish(),
                price,
                status);
    }
}
