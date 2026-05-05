package org.karane.tax;

import org.karane.tax.domain.ProductCategory;
import org.karane.tax.domain.State;
import org.karane.tax.domain.TaxResult;
import org.karane.tax.catalog.TaxRuleCatalog;
import org.karane.tax.service.TaxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TaxServiceTest {

    private TaxService service;

    @BeforeEach
    void setUp() {
        service = new TaxService(new TaxRuleCatalog());
    }

    @Nested
    @DisplayName("Exempt categories")
    class ExemptCategories {

        @Test
        @DisplayName("Food is tax-exempt in CA 2024")
        void foodExemptCA2024() {
            var result = service.calculate(new ProductCategory.Food(), State.CA, 2024, new BigDecimal("100.00"));
            assertEquals(0, result.taxAmount().compareTo(BigDecimal.ZERO));
            assertEquals(0, result.total().compareTo(new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("Medicine is tax-exempt in TX 2024")
        void medicineExemptTX2024() {
            var result = service.calculate(new ProductCategory.Medicine(), State.TX, 2024, new BigDecimal("200.00"));
            assertEquals(0, result.taxAmount().compareTo(BigDecimal.ZERO));
        }
    }

    @Nested
    @DisplayName("Standard tax (flat rate)")
    class StandardTax {

        @Test
        @DisplayName("Electronics in CA 2024 at 7.25%")
        void electronicsCA2024() {
            var result = service.calculate(new ProductCategory.Electronics(), State.CA, 2024, new BigDecimal("500.00"));
            assertEquals(new BigDecimal("36.25"), result.taxAmount());
            assertEquals(new BigDecimal("536.25"), result.total());
        }

        @Test
        @DisplayName("Clothing in NY 2024 at 4%")
        void clothingNY2024() {
            var result = service.calculate(new ProductCategory.Clothing(), State.NY, 2024, new BigDecimal("300.00"));
            assertEquals(new BigDecimal("12.00"), result.taxAmount());
            assertEquals(new BigDecimal("312.00"), result.total());
        }

        @Test
        @DisplayName("Electronics in TX 2025 reflects updated 6.5% rate")
        void electronicsTX2025RateChange() {
            var result2024 = service.calculate(new ProductCategory.Electronics(), State.TX, 2024, new BigDecimal("1000.00"));
            var result2025 = service.calculate(new ProductCategory.Electronics(), State.TX, 2025, new BigDecimal("1000.00"));
            assertTrue(result2025.taxAmount().compareTo(result2024.taxAmount()) > 0,
                    "2025 rate (6.5%) should produce more tax than 2024 rate (6.25%)");
        }
    }

    @Nested
    @DisplayName("Tiered tax (luxury bracket)")
    class TieredTax {

        @Test
        @DisplayName("Luxury item below $1000 threshold — standard rate applies")
        void luxuryBelowThreshold() {
            var result = service.calculate(new ProductCategory.Luxury(), State.CA, 2024, new BigDecimal("800.00"));
            assertEquals(new BigDecimal("58.00"), result.taxAmount());
        }

        @Test
        @DisplayName("Luxury item above $1000 threshold — excess taxed at 1.5x")
        void luxuryAboveThreshold() {
            var result = service.calculate(new ProductCategory.Luxury(), State.CA, 2024, new BigDecimal("1500.00"));
            assertEquals(new BigDecimal("126.88"), result.taxAmount());
        }

        @Test
        @DisplayName("Luxury in NY 2025 above threshold")
        void luxuryNY2025AboveThreshold() {
            var result = service.calculate(new ProductCategory.Luxury(), State.NY, 2025, new BigDecimal("1200.00"));
            assertEquals(new BigDecimal("110.50"), result.taxAmount());
        }
    }

    @Nested
    @DisplayName("Fallback to default rule")
    class DefaultRule {

        @Test
        @DisplayName("Unknown state/year combination falls back to 5% standard tax")
        void unknownStateFallback() {
            var result = service.calculate(new ProductCategory.Electronics(), State.NV, 2030, new BigDecimal("200.00"));
            assertEquals(new BigDecimal("10.00"), result.taxAmount());
        }
    }

    @Nested
    @DisplayName("TaxResult integrity")
    class TaxResultIntegrity {

        @Test
        @DisplayName("total = baseAmount + taxAmount")
        void totalIsSum() {
            var result = service.calculate(new ProductCategory.Electronics(), State.FL, 2024, new BigDecimal("250.00"));
            assertEquals(result.baseAmount().add(result.taxAmount()), result.total());
        }

        @Test
        @DisplayName("effectiveRate is non-negative")
        void effectiveRateNonNegative() {
            var result = service.calculate(new ProductCategory.Clothing(), State.WA, 2024, new BigDecimal("150.00"));
            assertTrue(result.effectiveRate().compareTo(BigDecimal.ZERO) >= 0);
        }

        @Test
        @DisplayName("Zero amount yields zero tax and zero total")
        void zeroAmount() {
            var result = service.calculate(new ProductCategory.Electronics(), State.CA, 2024, BigDecimal.ZERO);
            assertEquals(0, result.taxAmount().compareTo(BigDecimal.ZERO));
            assertEquals(0, result.total().compareTo(BigDecimal.ZERO));
        }
    }
}
