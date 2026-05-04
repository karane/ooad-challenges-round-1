package org.karane.tax;

import org.karane.tax.domain.ProductCategory;
import org.karane.tax.domain.State;
import org.karane.tax.domain.TaxResult;
import org.karane.tax.catalog.TaxRuleCatalog;
import org.karane.tax.service.TaxService;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        var service = new TaxService(new TaxRuleCatalog());

        record Scenario(String label, ProductCategory category, State state, int year, String amount) {}

        var scenarios = new Scenario[]{
            new Scenario("Food in CA 2024",          new ProductCategory.Food(),        State.CA, 2024, "150.00"),
            new Scenario("Electronics in CA 2024",   new ProductCategory.Electronics(), State.CA, 2024, "999.99"),
            new Scenario("Electronics in CA 2025",   new ProductCategory.Electronics(), State.CA, 2025, "999.99"),
            new Scenario("Clothing in NY 2024",      new ProductCategory.Clothing(),    State.NY, 2024, "300.00"),
            new Scenario("Luxury in CA 2024 $800",   new ProductCategory.Luxury(),      State.CA, 2024, "800.00"),
            new Scenario("Luxury in CA 2024 $1500",  new ProductCategory.Luxury(),      State.CA, 2024, "1500.00"),
            new Scenario("Luxury in NY 2025 $1200",  new ProductCategory.Luxury(),      State.NY, 2025, "1200.00"),
            new Scenario("Medicine in TX 2024",      new ProductCategory.Medicine(),    State.TX, 2024, "200.00"),
            new Scenario("Electronics in NV 2030",   new ProductCategory.Electronics(), State.NV, 2030, "500.00"),
        };

        System.out.println("%-30s %10s %10s %10s %10s".formatted("Scenario", "Base", "Tax", "Total", "Rate"));
        System.out.println("-".repeat(75));

        for (var s : scenarios) {
            TaxResult r = service.calculate(s.category(), s.state(), s.year(), new BigDecimal(s.amount()));
            System.out.println("%-30s %10.2f %10.2f %10.2f %9.2f%%".formatted(
                    s.label(),
                    r.baseAmount(),
                    r.taxAmount(),
                    r.total(),
                    r.effectiveRate().multiply(BigDecimal.valueOf(100))
            ));
        }
    }
}
