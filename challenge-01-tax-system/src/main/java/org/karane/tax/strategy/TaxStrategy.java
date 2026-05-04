package org.karane.tax.strategy;

import java.math.BigDecimal;

public interface TaxStrategy {
    BigDecimal calculate(BigDecimal amount, BigDecimal rate);
}
