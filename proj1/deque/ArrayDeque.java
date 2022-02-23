package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>{
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] array;

    public ArrayDeque() {
        this.size = 0;
        array = (T[]) new Object[1000];
        nextFirst = 0;
        nextLast = 1;
    }

    public void addFirst(T item) {
        array[nextFirst] = item;
        nextFirst = (nextFirst - 1) % size;
        size++;
    }

    public void addLast(T item) {
        array[nextLast] = item;
        nextLast = (nextLast + 1) % size;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        StringBuilder sb = new StringBuilder();
        int curIndex = (nextFirst + 1) % size;
        while (curIndex != nextLast) {
            sb.append(array[curIndex].toString());
            sb.append(" ");
            curIndex = (curIndex + 1) % size;
        }
        System.out.println(sb.toString());
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }

        nextLast = (nextLast - 1) % size;
        size--;
        // remove the reference in the array
        T returnItem = array[nextLast];
        array[nextLast] = null;
        return returnItem;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        nextFirst = (nextFirst + 1) % size;
        size--;
        // remove the reference in the array
        T returnItem = array[nextFirst];
        array[nextFirst] = null;
        return returnItem;
    }

    public T get(int index) {
        if (index >= size) {
            return null;
        }

        int curIndex = (index + 1) % size;

        return array[curIndex];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        int curIndex = (nextFirst + 1) % size;

        @Override
        public boolean hasNext() {
            return curIndex != nextLast;
        }

        @Override
        public T next() {
            T returnItem = array[curIndex];
            curIndex = (curIndex + 1) % size;
            return returnItem;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o==this) {
            return true;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        ArrayDeque<T> other = (ArrayDeque<T>) o;
        if (this.size != other.size) {
            return false;
        }

        Iterator<T> thisIt = this.iterator();
        Iterator<T> otherIt = other.iterator();
        while (thisIt.hasNext()) {
            if (!thisIt.next().equals(otherIt.next())) {
                return false;
            }
        }

        return true;
    }

}
