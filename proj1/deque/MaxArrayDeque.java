package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
    Comparator<T> c;

    public MaxArrayDeque(Comparator<T> c) {
        this.c = c;
    }

    public T max() {
        if (isEmpty()) {
            return null;
        }

        T maxItem = get(0);
        for (T item : this) {
            if (c.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        T maxItem = get(0);
        for (T item : this) {
            if (c.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }
}
