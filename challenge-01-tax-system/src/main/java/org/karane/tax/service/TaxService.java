package org.karane.tax.service;

import org.karane.tax.domain.ProductCategory;
import org.karane.tax.domain.State;
import org.karane.tax.domain.TaxResult;
import org.karane.tax.domain.TaxRule;
import org.karane.tax.catalog.TaxRuleCatalog;
import org.karane.tax.strategy.StandardTaxStrategy;

import java.math.BigDecimal;
import java.util.Optional;

public class TaxService {

    private static final BigDecimal DEFAULT_RATE = new BigDecimal("0.05");

    private final TaxRuleCatalog catalog;

    public TaxService(TaxRuleCatalog catalog) {
        this.catalog = catalog;
    }

    public TaxResult calculate(ProductCategory category, State state, int year, BigDecimal amount) {
        var rule = catalog.find(state, year, category)
                .orElse(defaultRule(state, year, category));

        var tax = rule.strategy().calculate(amount, rule.rate());
        return TaxResult.of(amount, tax);
    }

    public Optional<TaxRule> getRule(ProductCategory category, State state, int year) {
        return catalog.find(state, year, category);
    }

    private TaxRule defaultRule(State state, int year, ProductCategory category) {
        return new TaxRule(state, year, category, DEFAULT_RATE, StandardTaxStrategy.INSTANCE);
    }
}
