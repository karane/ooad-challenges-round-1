package org.karane.tax.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TieredTaxStrategy implements TaxStrategy {

    public static final TieredTaxStrategy INSTANCE = new TieredTaxStrategy(
            new BigDecimal("1000"), new BigDecimal("1.5")
    );

    private final BigDecimal threshold;
    private final BigDecimal multiplier;

    public TieredTaxStrategy(BigDecimal threshold, BigDecimal multiplier) {
        this.threshold = threshold;
        this.multiplier = multiplier;
    }

    @Override
    public BigDecimal calculate(BigDecimal amount, BigDecimal rate) {
        if (amount.compareTo(threshold) <= 0) {
            return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }
        var baseTax = threshold.multiply(rate);
        var excessTax = amount.subtract(threshold).multiply(rate).multiply(multiplier);
        return baseTax.add(excessTax).setScale(2, RoundingMode.HALF_UP);
    }
}
