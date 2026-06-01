package org.karane.restaurant.station;

import org.karane.restaurant.domain.Dish;
import org.karane.restaurant.domain.StationType;

import java.util.Arrays;

public class KitchenStation {

    private final StationType type;
    private final int[] slotFreeAt;

    public KitchenStation(StationType type, int maxConcurrent) {
        if (maxConcurrent <= 0) {
            throw new IllegalArgumentException("maxConcurrent must be positive");
        }
        this.type       = type;
        this.slotFreeAt = new int[maxConcurrent];
    }

    public StationType type() {
        return type;
    }

    public int enqueue(Dish dish) {
        int slot        = indexOfEarliestFreeSlot();
        int startMinute = slotFreeAt[slot];
        slotFreeAt[slot] = startMinute + dish.basePrepMinutes();
        return slotFreeAt[slot];
    }

    public int currentQueueWaitMinutes() {
        return slotFreeAt[indexOfEarliestFreeSlot()];
    }

    public void reset() {
        Arrays.fill(slotFreeAt, 0);
    }

    private int indexOfEarliestFreeSlot() {
        int idx = 0;
        for (int i = 1; i < slotFreeAt.length; i++) {
            if (slotFreeAt[i] < slotFreeAt[idx]) {
                idx = i;
            }
        }
        return idx;
    }
}
