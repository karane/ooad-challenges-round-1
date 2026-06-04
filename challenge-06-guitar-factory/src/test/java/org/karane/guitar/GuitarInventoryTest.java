package org.karane.guitar;

import org.junit.jupiter.api.*;
import org.karane.guitar.domain.*;
import org.karane.guitar.factory.GuitarFactory;
import org.karane.guitar.inventory.GuitarInventory;
import org.karane.guitar.inventory.SearchCriteria;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GuitarInventory")
class GuitarInventoryTest {

    private GuitarInventory inventory;
    private GuitarFactory electricFactory;
    private GuitarFactory acousticFactory;

    @BeforeEach
    void setUp() {
        inventory       = new GuitarInventory();
        electricFactory = new GuitarFactory(GuitarType.ELECTRIC);
        acousticFactory = new GuitarFactory(GuitarType.ACOUSTIC);
    }

    private Guitar electric() { return electricFactory.create(GuitarFactoryTest.electricSpec()); }
    private Guitar acoustic() { return acousticFactory.create(GuitarFactoryTest.acousticSpec()); }

    @Test
    @DisplayName("added guitar appears in all(); findByType filters by type; findBySerial locates by serial")
    void collectionQueries() {
        var e = electric();
        var a = acoustic();
        inventory.add(e); inventory.add(a);

        assertTrue(inventory.all().contains(e));
        assertTrue(inventory.findByType(GuitarType.ELECTRIC).contains(e));
        assertFalse(inventory.findByType(GuitarType.ELECTRIC).contains(a));
        assertEquals(e, inventory.findBySerial(e.serialNumber()).orElseThrow());
        assertTrue(inventory.findBySerial("UNKNOWN").isEmpty());
    }

    @Test
    @DisplayName("state transitions: reserve → sell → cancel; invalid transitions throw")
    void stateTransitions() {
        var g = electric();
        inventory.add(g);

        inventory.reserve(g);
        assertEquals(Guitar.Status.RESERVED, g.status());
        assertThrows(IllegalStateException.class, () -> inventory.reserve(g));

        inventory.cancelReservation(g);
        assertEquals(Guitar.Status.AVAILABLE, g.status());
        assertThrows(IllegalStateException.class, () -> inventory.cancelReservation(g));

        inventory.reserve(g);
        inventory.sell(g);
        assertEquals(Guitar.Status.SOLD, g.status());
        assertThrows(IllegalStateException.class, () -> inventory.sell(g));
    }

    @Test
    @DisplayName("available() and countByStatus reflect current status")
    void statusFiltering() {
        var g1 = electric(); var g2 = electric(); var g3 = electric();
        inventory.add(g1); inventory.add(g2); inventory.add(g3);

        inventory.reserve(g1);
        inventory.sell(g2);

        assertFalse(inventory.available().contains(g1));
        assertFalse(inventory.available().contains(g2));
        assertTrue(inventory.available().contains(g3));
        assertEquals(1, inventory.countByStatus(Guitar.Status.AVAILABLE));
        assertEquals(1, inventory.countByStatus(Guitar.Status.RESERVED));
        assertEquals(1, inventory.countByStatus(Guitar.Status.SOLD));
    }

    @Test
    @DisplayName("search filters by criteria and excludes non-available guitars")
    void search() {
        var e = electric();
        var a = acoustic();
        inventory.add(e); inventory.add(a);
        inventory.reserve(e);

        var byType = inventory.search(SearchCriteria.builder().type(GuitarType.ACOUSTIC).build());
        assertTrue(byType.contains(a));
        assertFalse(byType.contains(e));

        var reserved = inventory.search(SearchCriteria.builder().type(GuitarType.ELECTRIC).build());
        assertFalse(reserved.contains(e));
    }
}
