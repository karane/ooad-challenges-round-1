package org.karane.tax.strategy;

import java.math.BigDecimal;

public class ExemptTaxStrategy implements TaxStrategy {

    public static final ExemptTaxStrategy INSTANCE = new ExemptTaxStrategy();

    @Override
    public BigDecimal calculate(BigDecimal amount, BigDecimal rate) {
        return BigDecimal.ZERO;
    }
}
