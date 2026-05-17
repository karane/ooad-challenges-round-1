package org.karane.freight.pricing;

import java.math.BigDecimal;

/**
 * A snapshot of dynamic rates for one transport type at a point in time.
 *
 * @param ratePerKg              cost per billable kilogram (USD)
 * @param ratePerM3              cost per cubic metre (USD)
 * @param fuelSurchargePercent   fuel surcharge as a percentage of base cost (0–100)
 * @param handlingFee            flat handling fee per shipment (USD)
 * @param baseTransitDays        base transit-time estimate (days)
 */
public record FreightRate(
        BigDecimal ratePerKg,
        BigDecimal ratePerM3,
        BigDecimal fuelSurchargePercent,
        BigDecimal handlingFee,
        int baseTransitDays
) {}
