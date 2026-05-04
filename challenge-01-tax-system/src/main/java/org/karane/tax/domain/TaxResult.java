package org.karane.tax.domain;

import java.math.BigDecimal;

public record TaxResult(
        BigDecimal baseAmount,
        BigDecimal taxAmount,
        BigDecimal total,
        BigDecimal effectiveRate
) {
    public static TaxResult of(BigDecimal base, BigDecimal tax) {
        return new TaxResult(
                base,
                tax,
                base.add(tax),
                base.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : tax.divide(base, 4, java.math.RoundingMode.HALF_UP)
        );
    }
}
