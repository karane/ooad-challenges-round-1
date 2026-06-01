package org.karane.restaurant.service;

import org.karane.restaurant.domain.Dish;
import org.karane.restaurant.domain.Order;
import org.karane.restaurant.domain.OrderStatus;
import org.karane.restaurant.domain.StationType;
import org.karane.restaurant.domain.WaitEstimate;
import org.karane.restaurant.observer.OrderStatusListener;
import org.karane.restaurant.station.KitchenStation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class KitchenQueue {

    private final Map<StationType, KitchenStation> stations;
    private final List<OrderStatusListener> listeners = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();

    public KitchenQueue(List<KitchenStation> stationList) {
        stations = new EnumMap<>(StationType.class);
        for (var s : stationList) {
            stations.put(s.type(), s);
        }
    }

    public void addListener(OrderStatusListener listener) {
        listeners.add(listener);
    }

    public WaitEstimate submit(Order order) {
        orders.add(order);
        transition(order, OrderStatus.PREPARING);

        int maxReadyAt   = 0;
        int queueWaitMin = 0;

        for (Dish dish : order.dishes()) {
            var station = stationFor(dish);
            int wait    = station.currentQueueWaitMinutes();
            int readyAt = station.enqueue(dish);
            if (readyAt > maxReadyAt) {
                maxReadyAt   = readyAt;
                queueWaitMin = wait;
            }
        }

        return new WaitEstimate(order, queueWaitMin, maxReadyAt - queueWaitMin, maxReadyAt);
    }

    public void markReady(Order order) {
        transition(order, OrderStatus.READY);
    }

    public void markDelivered(Order order) {
        transition(order, OrderStatus.DELIVERED);
    }

    public List<Order> orders() {
        return List.copyOf(orders);
    }

    private KitchenStation stationFor(Dish dish) {
        var station = stations.get(dish.station());
        if (station == null) {
            throw new IllegalStateException("No station registered for: " + dish.station());
        }
        return station;
    }

    private void transition(Order order, OrderStatus next) {
        var prev = order.status();
        order.setStatus(next);
        for (var l : listeners) {
            l.onStatusChanged(order, prev, next);
        }
    }
}
