package org.karane.restaurant.observer;

import org.karane.restaurant.domain.Order;
import org.karane.restaurant.domain.OrderStatus;

public class ConsoleOrderLogger implements OrderStatusListener {

    @Override
    public void onStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
        System.out.printf("  [EVENT] %s  %s -> %s%n", order, previous, current);
    }
}
