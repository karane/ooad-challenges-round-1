package org.karane.guitar.inventory;

import org.karane.guitar.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class GuitarInventory {

    private final List<Guitar> guitars = new ArrayList<>();

    public void add(Guitar guitar) {
        guitars.add(guitar);
    }

    public List<Guitar> all() {
        return Collections.unmodifiableList(guitars);
    }

    public List<Guitar> available() {
        return findBy(g -> g.status() == Guitar.Status.AVAILABLE);
    }

    public List<Guitar> findBy(Predicate<Guitar> predicate) {
        return guitars.stream().filter(predicate).toList();
    }

    public List<Guitar> findByType(GuitarType type) {
        return findBy(g -> g.spec().type() == type);
    }

    public List<Guitar> findByBodyWood(WoodType wood) {
        return findBy(g -> g.spec().bodyWood() == wood);
    }

    public List<Guitar> findByBodyStyle(BodyStyle style) {
        return findBy(g -> g.spec().bodyStyle() == style);
    }

    /** Find all available guitars whose spec matches every non-null field of {@code criteria}. */
    public List<Guitar> search(SearchCriteria criteria) {
        return findBy(g -> g.status() == Guitar.Status.AVAILABLE && criteria.matches(g.spec()));
    }

    public Optional<Guitar> findBySerial(String serial) {
        return guitars.stream().filter(g -> g.serialNumber().equals(serial)).findFirst();
    }

    public void reserve(Guitar guitar) {
        if (guitar.status() != Guitar.Status.AVAILABLE)
            throw new IllegalStateException("Guitar %s is not available (status: %s)"
                    .formatted(guitar.serialNumber(), guitar.status()));
        guitar.setStatus(Guitar.Status.RESERVED);
    }

    public void sell(Guitar guitar) {
        if (guitar.status() == Guitar.Status.SOLD)
            throw new IllegalStateException("Guitar %s is already sold".formatted(guitar.serialNumber()));
        guitar.setStatus(Guitar.Status.SOLD);
    }

    public void cancelReservation(Guitar guitar) {
        if (guitar.status() != Guitar.Status.RESERVED)
            throw new IllegalStateException("Guitar %s is not reserved".formatted(guitar.serialNumber()));
        guitar.setStatus(Guitar.Status.AVAILABLE);
    }

    public long countByStatus(Guitar.Status status) {
        return guitars.stream().filter(g -> g.status() == status).count();
    }
}
