package org.karane.ticket.domain;

public final class SeatEntry {

    private final Seat seat;
    private SeatStatus status;
    private String holderName;

    public SeatEntry(Seat seat) {
        this.seat   = seat;
        this.status = SeatStatus.AVAILABLE;
    }

    public Seat seat() {
        return seat;
    }

    public SeatStatus status() {
        return status;
    }

    public String holderName() {
        return holderName;
    }

    public void sell(String holder) {
        this.status     = SeatStatus.SOLD;
        this.holderName = holder;
    }

    public void cancel() {
        this.status     = SeatStatus.CANCELLED;
        this.holderName = null;
    }

    public void release() {
        this.status     = SeatStatus.AVAILABLE;
        this.holderName = null;
    }
}
