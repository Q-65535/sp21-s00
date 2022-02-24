package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] array;

    public ArrayDeque() {
        this.size = 0;
        array = (T[]) new Object[20];
        nextFirst = 0;
        nextLast = 1;
    }

    private void resize(int capacity) {
        T[] newArray = ((T[]) new Object[capacity]);

        if (nextFirst < nextLast) {
            System.arraycopy(array, nextFirst, newArray, 0, size + 1);
            nextFirst = 0;
            nextLast = nextFirst + size + 1;
        } else {
            // copy first part
            System.arraycopy(array, 0, newArray, 0, nextLast);

            // copy second part
            int oldEndIndex = array.length - 1;
            int newEndIndex = newArray.length - 1;
            while (oldEndIndex != nextFirst) {
                newArray[newEndIndex--] = array[oldEndIndex--];
            }
            // reposition the "nextFirst" index
            nextFirst = newEndIndex;
        }

        array = newArray;
    }

    public void addFirst(T item) {
        if (size >= array.length - 2) {
            resize(size * 2 + 10);
        }
        array[nextFirst] = item;
        nextFirst = Math.floorMod(nextFirst - 1, array.length);
        size++;
    }

    public void addLast(T item) {
        if (size >= array.length - 2) {
            resize(size * 2 + 10);
        }
        array[nextLast] = item;
        nextLast = Math.floorMod(nextLast + 1, array.length);
        size++;
    }


    public int size() {
        return size;
    }

    public void printDeque() {
        StringBuilder sb = new StringBuilder();
        int curIndex = Math.floorMod(nextFirst + 1, array.length);
        while (curIndex != nextLast) {
            sb.append(array[curIndex].toString());
            sb.append(" ");
            curIndex = Math.floorMod(curIndex + 1, array.length);
        }
        System.out.println(sb.toString());
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }

        nextLast = Math.floorMod(nextLast - 1, array.length);
        size--;
        // remove the reference in the array
        T returnItem = array[nextLast];
        array[nextLast] = null;

        // resizing
        if (size < array.length / 4 && array.length > 10) {
            resize(array.length / 2);
        }

        return returnItem;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        nextFirst = Math.floorMod(nextFirst + 1, array.length);
        size--;
        // remove the reference in the array
        T returnItem = array[nextFirst];
        array[nextFirst] = null;

        // resizing
        if (size < array.length / 4 && array.length > 10) {
            resize(array.length / 2);
        }

        return returnItem;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }

        int curIndex = Math.floorMod(nextFirst + index + 1, array.length);

        return array[curIndex];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        int curIndex = Math.floorMod(nextFirst + 1, array.length);

        @Override
        public boolean hasNext() {
            return curIndex != nextLast;
        }

        @Override
        public T next() {
            T returnItem = array[curIndex];
            curIndex = Math.floorMod(curIndex + 1, array.length);
            return returnItem;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }

        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<T> other = (Deque<T>) o;
        if (this.size != other.size()) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }

}
