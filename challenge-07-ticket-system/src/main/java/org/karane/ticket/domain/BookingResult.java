package org.karane.ticket.domain;

public sealed interface BookingResult
        permits BookingSuccess, BookingFailure {

    static BookingResult success(Ticket ticket) {
        return new BookingSuccess(ticket);
    }

    static BookingResult failure(String reason) {
        return new BookingFailure(reason);
    }

    default boolean isSuccess() {
        return this instanceof BookingSuccess;
    }
}
