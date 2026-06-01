package org.karane.restaurant.domain;

public sealed interface Dish
        permits Dish.Starter, Dish.MainCourse, Dish.Dessert, Dish.Beverage {

    String name();
    StationType station();
    int basePrepMinutes();

    private static void requirePositivePrep(int basePrepMinutes) {
        if (basePrepMinutes <= 0) {
            throw new IllegalArgumentException("prep time must be positive");
        }
    }

    record Starter(String name, int basePrepMinutes) implements Dish {
        public Starter {
            requirePositivePrep(basePrepMinutes);
        }

        @Override
        public StationType station() {
            return StationType.COLD;
        }
    }

    record MainCourse(String name, int basePrepMinutes, StationType station) implements Dish {
        public MainCourse {
            requirePositivePrep(basePrepMinutes);
            if (station == StationType.COLD || station == StationType.BAR) {
                throw new IllegalArgumentException("MainCourse cannot use COLD or BAR station");
            }
        }
    }

    record Dessert(String name, int basePrepMinutes) implements Dish {
        public Dessert {
            requirePositivePrep(basePrepMinutes);
        }

        @Override
        public StationType station() {
            return StationType.COLD;
        }
    }

    record Beverage(String name, int basePrepMinutes) implements Dish {
        public Beverage {
            requirePositivePrep(basePrepMinutes);
        }

        @Override
        public StationType station() {
            return StationType.BAR;
        }
    }
}
