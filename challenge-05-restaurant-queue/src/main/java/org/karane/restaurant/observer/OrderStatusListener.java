package org.karane.restaurant.observer;

import org.karane.restaurant.domain.Order;
import org.karane.restaurant.domain.OrderStatus;

public interface OrderStatusListener {
    void onStatusChanged(Order order, OrderStatus previous, OrderStatus current);
}
