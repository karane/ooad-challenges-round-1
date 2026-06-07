package org.karane.ticket.domain;

import java.math.BigDecimal;

public sealed interface Zone
        permits VipZone, FloorZone, GeneralZone, BalconyZone {

    String name();
    int rows();
    int seatsPerRow();
    BigDecimal priceMultiplier();

    default int capacity() {
        return rows() * seatsPerRow();
    }
}
