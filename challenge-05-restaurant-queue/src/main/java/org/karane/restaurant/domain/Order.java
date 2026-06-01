package org.karane.restaurant.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Order {

    private final String id;
    private final String customerName;
    private final int tableNumber;
    private final List<Dish> dishes;
    private OrderStatus status;

    private Order(Builder builder) {
        this.id           = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customerName = builder.customerName;
        this.tableNumber  = builder.tableNumber;
        this.dishes       = Collections.unmodifiableList(new ArrayList<>(builder.dishes));
        this.status       = OrderStatus.PENDING;
    }

    public String id() {
        return id;
    }

    public String customerName() {
        return customerName;
    }

    public int tableNumber() {
        return tableNumber;
    }

    public List<Dish> dishes() {
        return dishes;
    }

    public OrderStatus status() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order[%s | table=%d | %s | %d dish(es)]"
                .formatted(id, tableNumber, customerName, dishes.size());
    }

    public static Builder forTable(int tableNumber, String customerName) {
        return new Builder(tableNumber, customerName);
    }

    public static final class Builder {
        private final String customerName;
        private final int tableNumber;
        private final List<Dish> dishes = new ArrayList<>();

        private Builder(int tableNumber, String customerName) {
            this.tableNumber  = tableNumber;
            this.customerName = customerName;
        }

        public Builder add(Dish dish) {
            dishes.add(dish);
            return this;
        }

        public Order build() {
            if (dishes.isEmpty()) {
                throw new IllegalStateException("An order must contain at least one dish");
            }
            return new Order(this);
        }
    }
}
