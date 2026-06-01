package org.karane.restaurant;

import org.karane.restaurant.domain.*;
import org.karane.restaurant.observer.ConsoleOrderLogger;
import org.karane.restaurant.service.KitchenQueue;
import org.karane.restaurant.station.KitchenStation;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        var caesarSalad  = new Dish.Starter("Caesar Salad", 5);
        var garlicBread  = new Dish.Starter("Garlic Bread", 4);
        var ribeye       = new Dish.MainCourse("Ribeye Steak", 18, StationType.GRILL);
        var chickenRoast = new Dish.MainCourse("Roast Chicken", 25, StationType.OVEN);
        var fishAndChips = new Dish.MainCourse("Fish & Chips", 12, StationType.FRYER);
        var cheesecake   = new Dish.Dessert("Cheesecake", 3);
        var iceCream     = new Dish.Dessert("Ice Cream", 2);
        var espresso     = new Dish.Beverage("Espresso", 2);
        var cocktail     = new Dish.Beverage("Mojito", 5);

        var queue = new KitchenQueue(List.of(
                new KitchenStation(StationType.GRILL, 2),
                new KitchenStation(StationType.FRYER, 2),
                new KitchenStation(StationType.OVEN,  1),
                new KitchenStation(StationType.COLD,  3),
                new KitchenStation(StationType.BAR,   2)
        ));
        queue.addListener(new ConsoleOrderLogger());

        var order1 = Order.forTable(1, "Alice").add(caesarSalad).add(ribeye).add(cheesecake).add(espresso).build();
        var order2 = Order.forTable(2, "Bob").add(garlicBread).add(fishAndChips).add(cocktail).build();
        var order3 = Order.forTable(3, "Carol").add(chickenRoast).add(iceCream).build();
        var order4 = Order.forTable(4, "Dave").add(ribeye).add(cheesecake).add(cocktail).build();

        System.out.println();
        printEstimate(queue.submit(order1));
        printEstimate(queue.submit(order2));
        printEstimate(queue.submit(order3));
        printEstimate(queue.submit(order4));

        System.out.println();
        queue.markReady(order2);
        queue.markDelivered(order2);
    }

    private static void printEstimate(WaitEstimate e) {
        System.out.printf("  %s%n", e.summary());
        System.out.printf("    Dishes: %s%n%n", e.order().dishes().stream().map(Dish::name).toList());
    }
}
