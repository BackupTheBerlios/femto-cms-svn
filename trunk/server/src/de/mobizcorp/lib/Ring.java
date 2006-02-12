package de.mobizcorp.lib;

import java.util.NoSuchElementException;

public class Ring<T> {
    public static interface Node<T> {
        public T getSelf();
        public Node<T> getNext();
        public void setNext(Node<T> next);
    }
    
    private Node<T> tail;
    
    public void insert(Node<T> elem) {
        if (tail == null) {
            elem.setNext(tail = elem);
        } else {
            elem.setNext(tail.getNext());
            tail.setNext(elem);
        }
    }
    
    public void append(Node<T> elem) {
        insert(elem);
        tail = elem;
    }
    
    public void remove(Node<T> elem) {
        Node<T> scan = tail;
        Node<T> prev = null;
        while ((scan = scan.getNext()) != elem) {
            if (scan == tail) {
                throw new NoSuchElementException();
            } else {
                prev = scan;
            }
        }
        if (prev != null) {
            prev.setNext(elem.getNext());
            if (elem == tail) {
                tail = prev;
            }
        } else if (scan != tail) {
            // removing the head
            tail.setNext(elem.getNext());
        } else {
            // removing last node
            tail = null;
        }
        elem.setNext(null);
    }
    
    public Node<T> removeTail() {
        final Node<T> result = tail;
        if (result != null) {
            remove(result);
        }
        return result;
    }
    
    public void clear() {
        tail = null;
    }
}
