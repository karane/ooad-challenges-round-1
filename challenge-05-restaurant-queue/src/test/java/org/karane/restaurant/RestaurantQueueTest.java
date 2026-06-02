package org.karane.restaurant;

import org.junit.jupiter.api.*;
import org.karane.restaurant.domain.Dish;
import org.karane.restaurant.domain.Order;
import org.karane.restaurant.domain.OrderStatus;
import org.karane.restaurant.domain.StationType;
import org.karane.restaurant.domain.WaitEstimate;
import org.karane.restaurant.observer.OrderStatusListener;
import org.karane.restaurant.service.KitchenQueue;
import org.karane.restaurant.station.KitchenStation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantQueueTest {

    private static final Dish SALAD      = new Dish.Starter("Caesar Salad", 5);
    private static final Dish STEAK      = new Dish.MainCourse("Steak", 18, StationType.GRILL);
    private static final Dish CHICKEN    = new Dish.MainCourse("Chicken", 25, StationType.OVEN);
    private static final Dish FRIES      = new Dish.MainCourse("Fries", 10, StationType.FRYER);
    private static final Dish CHEESECAKE = new Dish.Dessert("Cheesecake", 3);
    private static final Dish ESPRESSO   = new Dish.Beverage("Espresso", 2);

    private KitchenQueue queue;

    @BeforeEach
    void setUp() {
        queue = buildQueue();
    }

    private static KitchenQueue buildQueue() {
        return new KitchenQueue(List.of(
                new KitchenStation(StationType.GRILL, 2),
                new KitchenStation(StationType.FRYER, 1),
                new KitchenStation(StationType.OVEN,  1),
                new KitchenStation(StationType.COLD,  2),
                new KitchenStation(StationType.BAR,   1)
        ));
    }

    @Nested
    @DisplayName("Dish")
    class DishTests {

        @Test
        @DisplayName("Starter always routes to COLD station")
        void starterRoutesCold() {
            assertEquals(StationType.COLD, SALAD.station());
        }

        @Test
        @DisplayName("Beverage always routes to BAR station")
        void beverageRoutesBar() {
            assertEquals(StationType.BAR, ESPRESSO.station());
        }

        @Test
        @DisplayName("Dessert always routes to COLD station")
        void dessertRoutesCold() {
            assertEquals(StationType.COLD, CHEESECAKE.station());
        }

        @Test
        @DisplayName("MainCourse with invalid station is rejected")
        void mainCourseRejectsBarStation() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Dish.MainCourse("Bad dish", 10, StationType.BAR));
        }

        @Test
        @DisplayName("Dish with zero prep time is rejected")
        void zeroPrepTimeRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Dish.Starter("Ghost starter", 0));
        }
    }

    @Nested
    @DisplayName("Order Builder")
    class OrderBuilderTests {

        @Test
        @DisplayName("empty order is rejected")
        void emptyOrderRejected() {
            assertThrows(IllegalStateException.class,
                    () -> Order.forTable(1, "Alice").build());
        }

        @Test
        @DisplayName("order captures all added dishes")
        void orderCapturesDishes() {
            var order = Order.forTable(1, "Alice").add(STEAK).add(ESPRESSO).build();
            assertEquals(2, order.dishes().size());
        }

        @Test
        @DisplayName("new order starts in PENDING status")
        void startsAsPending() {
            var order = Order.forTable(1, "Alice").add(SALAD).build();
            assertEquals(OrderStatus.PENDING, order.status());
        }
    }

    @Nested
    @DisplayName("KitchenStation")
    class KitchenStationTests {

        @Test
        @DisplayName("first dish on an idle station starts immediately")
        void idleStationNoWait() {
            var station = new KitchenStation(StationType.GRILL, 2);
            assertEquals(0, station.currentQueueWaitMinutes());
        }

        @Test
        @DisplayName("enqueue returns ready-at minute equal to base prep for idle station")
        void enqueueIdleStation() {
            var station = new KitchenStation(StationType.GRILL, 1);
            assertEquals(18, station.enqueue(STEAK));
        }

        @Test
        @DisplayName("second dish queues behind first on single-slot station")
        void secondDishQueuesOnSingleSlot() {
            var station = new KitchenStation(StationType.GRILL, 1);
            station.enqueue(STEAK);
            assertEquals(36, station.enqueue(STEAK));
        }

        @Test
        @DisplayName("two-slot station runs two dishes in parallel")
        void twoSlotParallel() {
            var station = new KitchenStation(StationType.GRILL, 2);
            assertEquals(18, station.enqueue(STEAK));
            assertEquals(18, station.enqueue(STEAK));
        }

        @Test
        @DisplayName("reset clears all slot reservations")
        void resetClearsSlots() {
            var station = new KitchenStation(StationType.GRILL, 1);
            station.enqueue(STEAK);
            station.reset();
            assertEquals(0, station.currentQueueWaitMinutes());
        }
    }

    @Nested
    @DisplayName("KitchenQueue")
    class KitchenQueueTests {

        @Test
        @DisplayName("submit transitions order from PENDING to PREPARING")
        void submitTransitionsToPreparing() {
            var order = Order.forTable(1, "Alice").add(STEAK).build();
            queue.submit(order);
            assertEquals(OrderStatus.PREPARING, order.status());
        }

        @Test
        @DisplayName("totalMin equals prep time of slowest dish in the order")
        void totalMinReflectsSlowestDish() {
            var order = Order.forTable(1, "Alice").add(STEAK).add(ESPRESSO).build();
            assertEquals(18, queue.submit(order).totalMin());
        }

        @Test
        @DisplayName("second order on same single-slot station waits for first")
        void secondOrderWaitsForFirst() {
            var q = buildQueue();
            var o1 = Order.forTable(1, "A").add(FRIES).build();
            var o2 = Order.forTable(2, "B").add(FRIES).build();
            q.submit(o1);
            assertEquals(20, q.submit(o2).totalMin());
        }

        @Test
        @DisplayName("markReady transitions order to READY")
        void markReadyTransition() {
            var order = Order.forTable(1, "Alice").add(STEAK).build();
            queue.submit(order);
            queue.markReady(order);
            assertEquals(OrderStatus.READY, order.status());
        }

        @Test
        @DisplayName("markDelivered transitions order to DELIVERED")
        void markDeliveredTransition() {
            var order = Order.forTable(1, "Alice").add(STEAK).build();
            queue.submit(order);
            queue.markReady(order);
            queue.markDelivered(order);
            assertEquals(OrderStatus.DELIVERED, order.status());
        }

        @Test
        @DisplayName("listeners are notified on every status transition")
        void listenersNotified() {
            var log = new ArrayList<String>();
            queue.addListener((order, prev, curr) -> log.add(prev + "->" + curr));

            var order = Order.forTable(1, "Alice").add(STEAK).build();
            queue.submit(order);
            queue.markReady(order);

            assertEquals(List.of("PENDING->PREPARING", "PREPARING->READY"), log);
        }

        @Test
        @DisplayName("missing station registration throws at submit time")
        void missingStationThrows() {
            var limitedQueue = new KitchenQueue(List.of(new KitchenStation(StationType.GRILL, 1)));
            var order = Order.forTable(1, "Alice").add(ESPRESSO).build();
            assertThrows(IllegalStateException.class, () -> limitedQueue.submit(order));
        }
    }
}
