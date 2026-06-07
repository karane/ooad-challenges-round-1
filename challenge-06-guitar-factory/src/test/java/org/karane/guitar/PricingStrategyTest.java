package org.karane.guitar;

import org.junit.jupiter.api.*;
import org.karane.guitar.domain.*;
import org.karane.guitar.pricing.BasePricingStrategy;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BasePricingStrategy")
class PricingStrategyTest {

    private static final Finish GLOSS_BLACK  = new Finish("Black", Finish.Type.GLOSS);
    private static final Finish SATIN_NATURE = new Finish("Natural", Finish.Type.SATIN);

    private final BasePricingStrategy base = new BasePricingStrategy();

    @Test
    @DisplayName("standard woods and no pickups → $500 base")
    void basePrice() {
        var spec = GuitarSpec.builder(GuitarType.ACOUSTIC)
                .bodyStyle(BodyStyle.DREADNOUGHT)
                .bodyWood(WoodType.SPRUCE).neckWood(WoodType.MAPLE).fretboardWood(WoodType.ALDER)
                .finish(SATIN_NATURE).build();
        assertEquals(0, new BigDecimal("500.00").compareTo(base.price(spec)));
    }

    @Test
    @DisplayName("premium woods add $150 each; pickups add their cost; extra strings add $30 each")
    void surcharges() {
        // spruce(standard) + mahogany(+150) + rosewood(+150) = 800
        var acoustic = GuitarSpec.builder(GuitarType.ACOUSTIC)
                .bodyStyle(BodyStyle.DREADNOUGHT)
                .bodyWood(WoodType.SPRUCE).neckWood(WoodType.MAHOGANY).fretboardWood(WoodType.ROSEWOOD)
                .finish(SATIN_NATURE).build();
        assertEquals(0, new BigDecimal("800.00").compareTo(base.price(acoustic)));

        // alder+maple+maple (standard) + 3×SC($50) = 650
        var electric = GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.STRATOCASTER)
                .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                .pickups(GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL)
                .finish(GLOSS_BLACK).build();
        assertEquals(0, new BigDecimal("650.00").compareTo(base.price(electric)));

        // alder+maple+maple + humbucker($100) + 1 extra string($30) = 630
        var sevenString = GuitarSpec.builder(GuitarType.ELECTRIC)
                .bodyStyle(BodyStyle.FLYING_V)
                .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                .pickups(GuitarSpec.PickupType.HUMBUCKER).stringCount(7)
                .finish(GLOSS_BLACK).build();
        assertEquals(0, new BigDecimal("630.00").compareTo(base.price(sevenString)));
    }

    @Test
    @DisplayName("markup multiplies the base price; negative markup throws")
    void markup() {
        // 650 × 1.40 = 910
        var price = new BasePricingStrategy(40).price(
                GuitarSpec.builder(GuitarType.ELECTRIC)
                        .bodyStyle(BodyStyle.STRATOCASTER)
                        .bodyWood(WoodType.ALDER).neckWood(WoodType.MAPLE).fretboardWood(WoodType.MAPLE)
                        .pickups(GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL, GuitarSpec.PickupType.SINGLE_COIL)
                        .finish(GLOSS_BLACK).build());
        assertEquals(0, new BigDecimal("910.00").compareTo(price));

        assertThrows(IllegalArgumentException.class, () -> new BasePricingStrategy(-1));
    }
}
