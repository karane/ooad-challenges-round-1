package org.karane.ticket.repository;

import org.karane.ticket.domain.Show;
import org.karane.ticket.domain.Ticket;

import java.util.*;

public class TicketRepository {

    private final Map<String, Ticket> store = new LinkedHashMap<>();

    public void save(Ticket ticket) {
        store.put(ticket.id(), ticket);
    }

    public void remove(String ticketId) {
        store.remove(ticketId);
    }

    public Optional<Ticket> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Ticket> findByHolder(String holderName) {
        return store.values().stream()
                .filter(t -> t.holderName().equalsIgnoreCase(holderName))
                .toList();
    }

    public List<Ticket> findByShow(Show show) {
        return store.values().stream()
                .filter(t -> t.show().id().equals(show.id()))
                .toList();
    }

    public long countByShow(Show show) {
        return store.values().stream()
                .filter(t -> t.show().id().equals(show.id()))
                .count();
    }

    public List<Ticket> all() {
        return List.copyOf(store.values());
    }
}
