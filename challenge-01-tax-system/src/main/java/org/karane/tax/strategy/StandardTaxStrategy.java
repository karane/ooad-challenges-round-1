package org.karane.tax.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StandardTaxStrategy implements TaxStrategy {

    public static final StandardTaxStrategy INSTANCE = new StandardTaxStrategy();

    @Override
    public BigDecimal calculate(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
