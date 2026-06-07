package org.karane.ticket.domain;

import java.time.LocalDateTime;
import java.util.*;

/**
 * A ticketed event at a venue. On construction it initialises the full seating
 * plan — one {@link SeatEntry} per seat across all zones.
 * Built via the inner {@link Builder}.
 */
public final class Show {

    private final String id;
    private final String name;
    private final String venueName;
    private final LocalDateTime dateTime;
    private final List<Zone> zones;
    private final Map<String, SeatEntry> seatingPlan;   // key = seat label

    private Show(Builder b) {
        this.id          = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name        = b.name;
        this.venueName   = b.venueName;
        this.dateTime    = b.dateTime;
        this.zones       = List.copyOf(b.zones);
        this.seatingPlan = buildSeatingPlan(this.zones);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String venueName() {
        return venueName;
    }

    public LocalDateTime dateTime() {
        return dateTime;
    }

    public List<Zone> zones() {
        return zones;
    }

    public Map<String, SeatEntry> seatingPlan() {
        return Collections.unmodifiableMap(seatingPlan);
    }

    public Optional<SeatEntry> findEntry(Seat seat) {
        return Optional.ofNullable(seatingPlan.get(seat.label()));
    }

    public List<SeatEntry> availableIn(Zone zone) {
        return seatingPlan.values().stream()
                .filter(e -> e.seat().zone().equals(zone) && e.status() == SeatStatus.AVAILABLE)
                .sorted(Comparator.comparingInt(e -> e.seat().row() * 1000 + e.seat().number()))
                .toList();
    }

    public long soldCountIn(Zone zone) {
        return seatingPlan.values().stream()
                .filter(e -> e.seat().zone().equals(zone) && e.status() == SeatStatus.SOLD)
                .count();
    }

    public int totalCapacity() {
        return zones.stream().mapToInt(Zone::capacity).sum();
    }

    @Override
    public String toString() {
        return "Show[%s | %s @ %s | %s]".formatted(id, name, venueName, dateTime);
    }

    private static Map<String, SeatEntry> buildSeatingPlan(List<Zone> zones) {
        var plan = new LinkedHashMap<String, SeatEntry>();
        for (var zone : zones) {
            for (int row = 1; row <= zone.rows(); row++) {
                for (int seat = 1; seat <= zone.seatsPerRow(); seat++) {
                    var s = new Seat(zone, row, seat);
                    plan.put(s.label(), new SeatEntry(s));
                }
            }
        }
        return plan;
    }

    // ------------------------------------------------------------------ //
    // Builder                                                             //
    // ------------------------------------------------------------------ //

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private String venueName;
        private LocalDateTime dateTime;
        private final List<Zone> zones = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder venue(String v) {
            this.venueName = v;
            return this;
        }

        public Builder dateTime(LocalDateTime v) {
            this.dateTime = v;
            return this;
        }

        public Builder addZone(Zone z) {
            this.zones.add(z);
            return this;
        }

        public Show build() {
            if (venueName == null || venueName.isBlank()) {
                throw new IllegalStateException("venueName is required");
            }
            if (dateTime == null) {
                throw new IllegalStateException("dateTime is required");
            }
            if (zones.isEmpty()) {
                throw new IllegalStateException("at least one zone is required");
            }
            return new Show(this);
        }
    }
}
