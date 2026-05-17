package org.karane.freight.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The result of a single shipping cost calculation.
 *
 * @param transportationType the mode of transport estimated
 * @param baseCost           cost from weight/volume rates
 * @param fuelSurcharge      dynamic fuel-cost surcharge
 * @param handlingFee        fixed handling fee for the transport type
 * @param estimatedDays      estimated transit days
 */
public record ShippingEstimate(
        TransportationType transportationType,
        BigDecimal baseCost,
        BigDecimal fuelSurcharge,
        BigDecimal handlingFee,
        int estimatedDays
) {
    public BigDecimal totalCost() {
        return baseCost.add(fuelSurcharge).add(handlingFee).setScale(2, RoundingMode.HALF_UP);
    }
}
