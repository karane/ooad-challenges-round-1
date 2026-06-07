package org.karane.guitar;

import org.junit.jupiter.api.*;
import org.karane.guitar.domain.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GuitarSpec")
class GuitarSpecTest {

    private static final Finish GLOSS_BLACK  = new Finish("Black", Finish.Type.GLOSS);
    private static final Finish SATIN_NATURE = new Finish("Natural", Finish.Type.SATIN);

    @Test
    @DisplayName("valid spec builds; defaults are 6 strings, C neck, empty pickups")
    void buildsWithDefaults() {
        var spec = GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.STRATOCASTER)
                .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                .finish(GLOSS_BLACK).build();
        assertEquals(6, spec.stringCount());
        assertTrue(spec.pickups().isEmpty());
    }

    @Test
    @DisplayName("missing required fields throw; string count out of 4–12 throws")
    void validationThrows() {
        assertThrows(IllegalStateException.class, () ->
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                        .finish(GLOSS_BLACK).build());  // missing bodyStyle

        assertThrows(IllegalArgumentException.class, () ->
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyStyle(BodyStyle.STRATOCASTER)
                        .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                        .finish(GLOSS_BLACK).stringCount(3).build());  // too few strings

        assertThrows(IllegalArgumentException.class, () ->
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyStyle(BodyStyle.STRATOCASTER)
                        .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                        .finish(GLOSS_BLACK).stringCount(13).build());  // too many strings
    }

    @Test
    @DisplayName("pickups list is unmodifiable and preserves order")
    void pickupsList() {
        var spec = GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.STRATOCASTER)
                .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                .pickups(GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.HUMBUCKER, GuitarSpec.PickupType.P90)
                .finish(GLOSS_BLACK).build();
        assertEquals(java.util.List.of(
                GuitarSpec.PickupType.SINGLE_COIL,
                GuitarSpec.PickupType.HUMBUCKER,
                GuitarSpec.PickupType.P90), spec.pickups());
        assertThrows(UnsupportedOperationException.class,
                () -> spec.pickups().add(GuitarSpec.PickupType.HUMBUCKER));
    }

    @Test
    @DisplayName("Finish: blank color throws; toString formats as 'color type'")
    void finish() {
        assertThrows(IllegalArgumentException.class, () -> new Finish("", Finish.Type.GLOSS));
        assertEquals("Sunburst gloss", new Finish("Sunburst", Finish.Type.GLOSS).toString());
        assertEquals("Natural satin",  new Finish("Natural",  Finish.Type.SATIN).toString());
        assertEquals("Black matte",    new Finish("Black",    Finish.Type.MATTE).toString());
    }
}
