package org.karane.tax.catalog;

import org.karane.tax.domain.ProductCategory;
import org.karane.tax.domain.State;
import org.karane.tax.domain.TaxRule;
import org.karane.tax.strategy.ExemptTaxStrategy;
import org.karane.tax.strategy.StandardTaxStrategy;
import org.karane.tax.strategy.TieredTaxStrategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TaxRuleCatalog {

    private record RuleKey(State state, int year, String category) {}

    private final Map<RuleKey, TaxRule> rules = new HashMap<>();

    public TaxRuleCatalog() {
        seed();
    }

    public Optional<TaxRule> find(State state, int year, ProductCategory category) {
        var key = new RuleKey(state, year, category.getClass().getSimpleName());
        return Optional.ofNullable(rules.get(key));
    }

    public void addRule(TaxRule rule) {
        var key = new RuleKey(rule.state(), rule.year(), rule.category().getClass().getSimpleName());
        rules.put(key, rule);
    }

    private void seed() {
        // --- 2024 ---
        add(State.CA, 2024, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.CA, 2024, new ProductCategory.Electronics(), "0.0725", StandardTaxStrategy.INSTANCE);
        add(State.CA, 2024, new ProductCategory.Clothing(),    "0.0725", StandardTaxStrategy.INSTANCE);
        add(State.CA, 2024, new ProductCategory.Luxury(),      "0.0725", TieredTaxStrategy.INSTANCE);
        add(State.CA, 2024, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);

        add(State.TX, 2024, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.TX, 2024, new ProductCategory.Electronics(), "0.0625", StandardTaxStrategy.INSTANCE);
        add(State.TX, 2024, new ProductCategory.Clothing(),    "0.0625", StandardTaxStrategy.INSTANCE);
        add(State.TX, 2024, new ProductCategory.Luxury(),      "0.0625", TieredTaxStrategy.INSTANCE);
        add(State.TX, 2024, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);

        add(State.NY, 2024, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.NY, 2024, new ProductCategory.Electronics(), "0.08",  StandardTaxStrategy.INSTANCE);
        add(State.NY, 2024, new ProductCategory.Clothing(),    "0.04",  StandardTaxStrategy.INSTANCE);
        add(State.NY, 2024, new ProductCategory.Luxury(),      "0.08",  TieredTaxStrategy.INSTANCE);
        add(State.NY, 2024, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);

        add(State.FL, 2024, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.FL, 2024, new ProductCategory.Electronics(), "0.06",  StandardTaxStrategy.INSTANCE);
        add(State.FL, 2024, new ProductCategory.Clothing(),    "0.06",  StandardTaxStrategy.INSTANCE);
        add(State.FL, 2024, new ProductCategory.Luxury(),      "0.06",  TieredTaxStrategy.INSTANCE);
        add(State.FL, 2024, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);

        add(State.WA, 2024, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.WA, 2024, new ProductCategory.Electronics(), "0.065", StandardTaxStrategy.INSTANCE);
        add(State.WA, 2024, new ProductCategory.Clothing(),    "0.065", StandardTaxStrategy.INSTANCE);
        add(State.WA, 2024, new ProductCategory.Luxury(),      "0.065", TieredTaxStrategy.INSTANCE);
        add(State.WA, 2024, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);

        // --- 2025 (rate changes) ---
        add(State.CA, 2025, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.CA, 2025, new ProductCategory.Electronics(), "0.075", StandardTaxStrategy.INSTANCE);
        add(State.CA, 2025, new ProductCategory.Clothing(),    "0.075", StandardTaxStrategy.INSTANCE);
        add(State.CA, 2025, new ProductCategory.Luxury(),      "0.075", TieredTaxStrategy.INSTANCE);
        add(State.CA, 2025, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);

        add(State.TX, 2025, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.TX, 2025, new ProductCategory.Electronics(), "0.065", StandardTaxStrategy.INSTANCE);
        add(State.TX, 2025, new ProductCategory.Clothing(),    "0.065", StandardTaxStrategy.INSTANCE);
        add(State.TX, 2025, new ProductCategory.Luxury(),      "0.065", TieredTaxStrategy.INSTANCE);
        add(State.TX, 2025, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);

        add(State.NY, 2025, new ProductCategory.Food(),        "0.00",  ExemptTaxStrategy.INSTANCE);
        add(State.NY, 2025, new ProductCategory.Electronics(), "0.085", StandardTaxStrategy.INSTANCE);
        add(State.NY, 2025, new ProductCategory.Clothing(),    "0.045", StandardTaxStrategy.INSTANCE);
        add(State.NY, 2025, new ProductCategory.Luxury(),      "0.085", TieredTaxStrategy.INSTANCE);
        add(State.NY, 2025, new ProductCategory.Medicine(),    "0.00",  ExemptTaxStrategy.INSTANCE);
    }

    private void add(State state, int year, ProductCategory cat, String rate, org.karane.tax.strategy.TaxStrategy strategy) {
        addRule(new TaxRule(state, year, cat, new BigDecimal(rate), strategy));
    }
}
