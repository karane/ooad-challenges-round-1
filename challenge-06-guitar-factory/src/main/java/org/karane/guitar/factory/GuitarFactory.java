package org.karane.guitar.factory;

import org.karane.guitar.domain.*;
import org.karane.guitar.pricing.BasePricingStrategy;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GuitarFactory {

    private static final AtomicInteger COUNTER = new AtomicInteger(1000);

    private static final Set<BodyStyle> ELECTRIC_STYLES =
            Set.of(BodyStyle.STRATOCASTER, BodyStyle.LES_PAUL, BodyStyle.FLYING_V, BodyStyle.SG);
    private static final Set<BodyStyle> ACOUSTIC_STYLES =
            Set.of(BodyStyle.DREADNOUGHT);
    private static final Set<BodyStyle> BASS_STYLES =
            Set.of(BodyStyle.JAZZ_BASS);

    private final GuitarType type;
    private final String prefix;
    private final BasePricingStrategy pricing;

    public GuitarFactory(GuitarType type) {
        this(type, 0);
    }

    public GuitarFactory(GuitarType type, double markupPercent) {
        this.type    = type;
        this.prefix  = switch (type) {
            case ELECTRIC -> "EL";
            case ACOUSTIC -> "AC";
            case BASS     -> "BS";
        };
        this.pricing = new BasePricingStrategy(markupPercent);
    }

    public Guitar create(GuitarSpec spec) {
        if (spec.type() != type)
            throw new IllegalArgumentException(
                    "This factory creates %s guitars, got %s".formatted(type, spec.type()));
        validate(spec);
        var serial = prefix + "-" + COUNTER.getAndIncrement();
        var price  = pricing.price(spec);
        return new Guitar(serial, spec, price);
    }

    private void validate(GuitarSpec spec) {
        switch (type) {
            case ELECTRIC -> validateElectric(spec);
            case ACOUSTIC -> validateAcoustic(spec);
            case BASS     -> validateBass(spec);
        }
    }

    private void validateElectric(GuitarSpec spec) {
        if (!ELECTRIC_STYLES.contains(spec.bodyStyle()))
            throw new IllegalArgumentException("Body style " + spec.bodyStyle() + " is not an electric style");
        if (spec.pickups().isEmpty())
            throw new IllegalArgumentException("Electric guitars must have at least one pickup");
    }

    private void validateAcoustic(GuitarSpec spec) {
        if (!ACOUSTIC_STYLES.contains(spec.bodyStyle()))
            throw new IllegalArgumentException("Body style " + spec.bodyStyle() + " is not an acoustic style");
        if (!spec.pickups().isEmpty())
            throw new IllegalArgumentException("Acoustic guitars do not support electric pickups");
    }

    private void validateBass(GuitarSpec spec) {
        if (!BASS_STYLES.contains(spec.bodyStyle()))
            throw new IllegalArgumentException("Body style " + spec.bodyStyle() + " is not a bass style");
        if (spec.stringCount() > 6)
            throw new IllegalArgumentException("Bass guitars support at most 6 strings");
        if (spec.pickups().isEmpty())
            throw new IllegalArgumentException("Bass guitars must have at least one pickup");
    }
}
