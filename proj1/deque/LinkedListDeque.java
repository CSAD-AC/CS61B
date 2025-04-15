package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> , Iterable<T>{
    private class Node{
        private Node prev, next;
        private T item;
        public Node(T i, Node p, Node n){
            item = i;
            prev = p;
            next = n;
        }
        public Node(T i){
            item = i;
            prev = null;
            next = null;
        }
        public Node(){
            item = null;
            prev = null;
            next = null;
        }
    }


    private Node sentinel;
    private int size;

    public LinkedListDeque(){
        sentinel = new Node();
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }
    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }
    @Override
    public boolean equals(Object o) {
        if(o != null && o instanceof LinkedListDeque) {
            LinkedListDeque<T> other = (LinkedListDeque<T>) o;
            if(size != other.size){
                return false;
            }
            return true;
        }
        return false;
    }
    @Override
    public void addFirst(T item) {
        Node p = sentinel.next;
        Node n = new Node(item, sentinel, p);
        p.prev = n;
        sentinel.next = n;
        size++;
    }

    @Override
    public void addLast(T item) {
        Node p = sentinel.prev;
        Node n = new Node(item, p, sentinel);
        p.next = n;
        sentinel.prev = n;
        size++;
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        Node p = sentinel.next;
        if (p == sentinel) {
            return null;
        }
        Node n = p.next;
        n.prev = sentinel;
        sentinel.next = n;
        size--;
        return p.item;
    }

    @Override
    public T removeLast() {
        Node p = sentinel.prev;
        if (p == sentinel) {
            return null;
        }
        Node n = p.prev;
        n.next = sentinel;
        sentinel.prev = n;
        size--;
        return p.item;
    }

    @Override
    public T get(int index) {
        Node p = sentinel.next;
        while (index > 0) {
            p = p.next;
            index--;
        }
        return p.item;
    }

    public T getRecursive(int index) {
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(Node p, int index) {
        if (index == 0) {
            return p.item;
        }
        return getRecursiveHelper(p.next, index - 1);
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node p;

        public LinkedListDequeIterator() {
            p = sentinel.next;
        }

        public boolean hasNext() {
            return p != sentinel;
        }

        public T next() {
            T returnItem = p.item;
            p = p.next;
            return returnItem;
        }
    }
}
