package org.karane.freight.domain;

import java.math.BigDecimal;

/**
 * Immutable description of a cargo shipment.
 *
 * @param weightKg    gross weight in kilograms
 * @param lengthCm    package length in centimetres
 * @param widthCm     package width in centimetres
 * @param heightCm    package height in centimetres
 * @param description human-readable cargo description
 */
public record Shipment(
        BigDecimal weightKg,
        BigDecimal lengthCm,
        BigDecimal widthCm,
        BigDecimal heightCm,
        String description
) {
    public Shipment {
        if (weightKg.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("weightKg must be positive");
        if (lengthCm.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("lengthCm must be positive");
        if (widthCm.compareTo(BigDecimal.ZERO)  <= 0) throw new IllegalArgumentException("widthCm must be positive");
        if (heightCm.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("heightCm must be positive");
    }

    public BigDecimal volumeM3() {
        return lengthCm.multiply(widthCm).multiply(heightCm)
                       .divide(BigDecimal.valueOf(1_000_000));
    }

    public BigDecimal billableWeightKg() {
        var volumetric = volumeM3().multiply(BigDecimal.valueOf(333));
        return weightKg.max(volumetric);
    }
}
