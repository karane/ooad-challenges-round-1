package org.karane.guitar;

import org.junit.jupiter.api.*;
import org.karane.guitar.domain.*;
import org.karane.guitar.factory.GuitarFactory;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GuitarFactory")
class GuitarFactoryTest {

    private static final Finish GLOSS_BLACK  = new Finish("Black", Finish.Type.GLOSS);
    private static final Finish SATIN_NATURE = new Finish("Natural", Finish.Type.SATIN);

    private final GuitarFactory electricFactory = new GuitarFactory(GuitarType.ELECTRIC);
    private final GuitarFactory acousticFactory = new GuitarFactory(GuitarType.ACOUSTIC);
    private final GuitarFactory bassFactory     = new GuitarFactory(GuitarType.BASS);

    static GuitarSpec electricSpec() {
        return GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.STRATOCASTER)
                .bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE)
                .fretboardWood(WoodType.MAPLE)
                .pickups(GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL)
                .finish(GLOSS_BLACK)
                .build();
    }

    static GuitarSpec acousticSpec() {
        return GuitarSpec.builder(GuitarType.ACOUSTIC)
                .bodyStyle(BodyStyle.DREADNOUGHT)
                .bodyWood(WoodType.SPRUCE)
                .neckWood(WoodType.MAHOGANY)
                .fretboardWood(WoodType.ROSEWOOD)
                .finish(SATIN_NATURE)
                .build();
    }

    static GuitarSpec bassSpec() {
        return GuitarSpec.builder(GuitarType.BASS)
                .bodyStyle(BodyStyle.JAZZ_BASS)
                .bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE)
                .fretboardWood(WoodType.ROSEWOOD)
                .pickups(GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL)
                .stringCount(4)
                .finish(GLOSS_BLACK)
                .build();
    }

    @Test
    @DisplayName("factory produces guitar with correct type and AVAILABLE status")
    void createsGuitarWithCorrectTypeAndStatus() {
        var g = electricFactory.create(electricSpec());
        assertEquals(GuitarType.ELECTRIC, g.spec().type());
        assertEquals(Guitar.Status.AVAILABLE, g.status());
    }

    @Test
    @DisplayName("each factory uses its type prefix; serial numbers are unique")
    void serialPrefixesAndUniqueness() {
        assertTrue(electricFactory.create(electricSpec()).serialNumber().startsWith("EL-"));
        assertTrue(acousticFactory.create(acousticSpec()).serialNumber().startsWith("AC-"));
        assertTrue(bassFactory.create(bassSpec()).serialNumber().startsWith("BS-"));

        var serials = new HashSet<String>();
        for (int i = 0; i < 20; i++) serials.add(electricFactory.create(electricSpec()).serialNumber());
        assertEquals(20, serials.size());
    }

    @Test
    @DisplayName("factory rejects spec of wrong type")
    void rejectsWrongType() {
        assertThrows(IllegalArgumentException.class, () -> electricFactory.create(acousticSpec()));
        assertThrows(IllegalArgumentException.class, () -> acousticFactory.create(electricSpec()));
    }

    @Test
    @DisplayName("electric requires pickup; acoustic rejects pickup; bass requires pickup")
    void pickupRules() {
        var noPickupElectric = GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.STRATOCASTER).bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                .finish(GLOSS_BLACK).build();
        assertThrows(IllegalArgumentException.class, () -> electricFactory.create(noPickupElectric));

        var acousticWithPickup = GuitarSpec.builder(GuitarType.ACOUSTIC)
                .bodyStyle(BodyStyle.DREADNOUGHT).bodyWood(WoodType.SPRUCE)
                .neckWood(WoodType.MAHOGANY).fretboardWood(WoodType.ROSEWOOD)
                .pickups(GuitarSpec.PickupType.SINGLE_COIL).finish(SATIN_NATURE).build();
        assertThrows(IllegalArgumentException.class, () -> acousticFactory.create(acousticWithPickup));

        var noPickupBass = GuitarSpec.builder(GuitarType.BASS)
                .bodyStyle(BodyStyle.JAZZ_BASS).bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE).fretboardWood(WoodType.ROSEWOOD)
                .finish(GLOSS_BLACK).build();
        assertThrows(IllegalArgumentException.class, () -> bassFactory.create(noPickupBass));
    }

    @Test
    @DisplayName("bass rejects >6 strings; electric accepts 7-string spec")
    void stringCountRules() {
        var bassSevenString = GuitarSpec.builder(GuitarType.BASS)
                .bodyStyle(BodyStyle.JAZZ_BASS).bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE).fretboardWood(WoodType.ROSEWOOD)
                .pickups(GuitarSpec.PickupType.SINGLE_COIL).stringCount(7)
                .finish(GLOSS_BLACK).build();
        assertThrows(IllegalArgumentException.class, () -> bassFactory.create(bassSevenString));

        var electricSevenString = GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.FLYING_V).bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                .pickups(GuitarSpec.PickupType.HUMBUCKER).stringCount(7)
                .finish(GLOSS_BLACK).build();
        assertDoesNotThrow(() -> electricFactory.create(electricSevenString));
    }

    @Test
    @DisplayName("factory rejects wrong body style for type")
    void rejectsWrongBodyStyle() {
        var wrongStyle = GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.JAZZ_BASS).bodyWood(WoodType.ALDER)
                .neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                .pickups(GuitarSpec.PickupType.SINGLE_COIL).finish(GLOSS_BLACK).build();
        assertThrows(IllegalArgumentException.class, () -> electricFactory.create(wrongStyle));
    }
}
