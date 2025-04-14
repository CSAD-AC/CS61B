package deque;

public class ArrayDeque<T> implements deque<T>{
    private T[] items;
    private int size = 0;

    public ArrayDeque() {
        items = (T[]) new Object[8];
    }
    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        System.arraycopy(items, 0, a, 0, size);
        items = a;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize((int) (size * 1.5));
        }
        for (int i = size; i > 0; i--) {
            items[i] = items[i - 1];
        }
        items[0] = item;
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize((int) (size * 1.5));
        }
        items[size] = item;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(items[i] + " ");
        }
    }

    @Override
    public T removeFirst() {
        if(size == items.length / 4 - 1 && size >= 16){
            resize((int) (items.length * 0.25));
        }
        if(size == 0){
            return null;
        }
        T item = items[0];
        for (int i = 0; i < size-1; i++) {
            items[i] = items[i + 1];
        }
        size--;
        return item;
    }

    @Override
    public T removeLast() {
        if(size == items.length / 4 - 1 && size >= 16){
            resize((int) (items.length * 0.25));
        }
        if(size == 0){
            return null;
        }
        T item = items[size - 1];
        items[size - 1] = null;
        size--;
        return item;
    }

    @Override
    public T get(int index) {
        return items[index];
    }
}
