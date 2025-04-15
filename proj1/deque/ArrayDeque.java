package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> , Iterable<T> {
    private T[] items;
    int nextFirst, nextLast;
    int size;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        nextFirst = 0;
        nextLast = 1;
        size = 0;
    }

    private int last(int index) {
        return (index - 1 + items.length) % items.length;
    }
    private int next(int index) {
        return (index + 1) % items.length;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int current = next(nextFirst);
        for (int i = 0; i < size; i++) {
            a[i] = items[current];
            current = next(current);
        }
        items = a;
        nextFirst = items.length - 1;
        nextLast = size;
    }
    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize((int) (items.length * 1.5));
        }

        items[nextFirst] = item;
        nextFirst = last(nextFirst);
        size++;
    }
    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize((int) (items.length * 1.5));
        }
        items[nextLast] = item;
        nextLast = next(nextLast);
        size++;
    }
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T item = items[next(nextFirst)];
        items[next(nextFirst)] = null;
        nextFirst = next(nextFirst);
        size--;
        if (size >= 16 && size < items.length * 0.25) {
            resize((int) (items.length * 0.25));
        }
        return item;
    }
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T item = items[last(nextLast)];
        items[last(nextLast)] = null;
        nextLast = last(nextLast);
        size--;
        if (size >= 16 && size < items.length * 0.25) {
            resize((int) (items.length * 0.25));
        }
        return item;
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public void printDeque() {
        int current = next(nextFirst);
        for (int i = 0; i < size; i++) {
            System.out.print(items[current] + " ");
            current = next(current);
        }
        System.out.println();
    }
    @Override
    public T get(int index) {
        int current = next(nextFirst);
        for (int i = 0; i < index; i++) {
            current = next(current);
        }
        return items[current];
    }
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }
    private class ArrayDequeIterator implements Iterator<T> {
        private int index;
        private int current;
        public ArrayDequeIterator() {
            current = ArrayDeque.this.next(nextFirst);
            index = 0;
        }
        public boolean hasNext() {
            return index < size;
        }
        public T next() {
            T item = items[current];
            current = ArrayDeque.this.next(current);
            index++;
            return item;
        }
    }
    public boolean equals(Object o) {
        if(o != null && (o instanceof Deque)) {
            Deque<T> other = (Deque<T>) o;
            if(size != other.size()){
                return false;
            }
            for(int i = 0; i < size; i++) {
                if(!get(i).equals(other.get(i))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }



}
