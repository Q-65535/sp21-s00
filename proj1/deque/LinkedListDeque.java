package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    private int size;
    Node sentinel;

    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        private Node cur;

        public LinkedListIterator() {
            cur = sentinel;
        }

        public boolean hasNext() {
            return cur.next != sentinel;
        }

        public T next() {
            T returnItem = cur.next.item;
            cur = cur.next;
            return returnItem;
        }
    }


    private class Node {
        T item;
        Node pre;
        Node next;

        public Node(){};

        public Node(T item, Node pre, Node next) {
            this.item = item;
            this.pre = pre;
            this.next = next;
        }
    }

    public LinkedListDeque() {
        sentinel = new Node();
        sentinel.next = sentinel;
        sentinel.pre = sentinel;
        size = 0;
    }

    public void addFirst(T item) {
        Node curFirst = sentinel.next;
        Node newFirst = new Node(item, sentinel, curFirst);
        sentinel.next = newFirst;
        curFirst.pre = newFirst;
        size++;
    }

    public void addLast(T item) {
        Node curLast = sentinel.pre;
        Node newLast = new Node(item, curLast, sentinel);
        curLast.next = newLast;
        sentinel.pre = newLast;
        size++;
    }


    public int size() {
        return size;
    }

    public void printDeque() {
        Node curNode = sentinel.next;
        StringBuilder sb = new StringBuilder();
        while (curNode != sentinel) {
            sb.append(curNode.toString());
            sb.append(" ");
            curNode = curNode.next;
        }
        String str = sb.toString();
        System.out.println(str);
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node target = sentinel.next;
        sentinel.next = target.next;
        target.next.pre = sentinel;
        T item = target.item;
        target = null;
        size--;
        return item;
    }

    public T removeLast() {
        if (size==0) {
            return null;
        }
        Node target = sentinel.pre;
        sentinel.pre = target.pre;
        target.pre.next = sentinel;
        T item = target.item;
        target = null;
        size--;
        return item;
    }

    public T get(int index) {
        if (index >= size) {
            return null;
        }
        Node cur = sentinel;
        for (int i = 0; i <= index; i++) {
            cur = cur.next;
        }
        return cur.item;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
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

    public T getRecursive(int index) {
        if (index >= size) {
            return null;
        }
        return helper(sentinel, index);
    }

    private T helper(Node cur, int index) {
        cur = cur.next;
        if (index == 0) {
            return cur.item;
        }
        return helper(cur, index - 1);
    }
}
