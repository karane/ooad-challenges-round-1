package org.karane.tax.domain;

import org.karane.tax.strategy.TaxStrategy;
import java.math.BigDecimal;

public record TaxRule(
        State state,
        int year,
        ProductCategory category,
        BigDecimal rate,
        TaxStrategy strategy
) {}
