package org.karane.ticket.pricing;

import org.karane.ticket.domain.Show;
import org.karane.ticket.domain.Zone;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal price(Show show, Zone zone);
}
