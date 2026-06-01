package org.karane.restaurant.domain;

public record WaitEstimate(Order order, int queueWaitMin, int prepMin, int totalMin) {

    public String summary() {
        return "Order %s | queue wait: %d min | prep: %d min | TOTAL: %d min"
                .formatted(order.id(), queueWaitMin, prepMin, totalMin);
    }
}
