package com.example.dsa2_ca1.model;

import java.util.Arrays;

public class MyArrayList<T> implements MyList<T> {

    // fields
    private final int DEFAULT_CAPACITY = 10;
    private int size;
    private Object[] elements;

    // constructor
    public MyArrayList() {
        this.elements = new Object[DEFAULT_CAPACITY];
    }

    public MyArrayList(int initialCapacity) {
        this.elements = new Object[initialCapacity];
    }

    /**
     * Adds an element to the end of the array list
     *
     * @param element the element to be added to the list
     * @return true if the element was added successfully
     */
    @Override
    public boolean add(T element) {
        ensureCapacity();
        elements[size] = element;
        size++;
        return true;
    }

    /**
     * Replaces the element at the specified index with the given element.
     *
     * @param index the index of the element to replace
     * @param element the element to set
     * @return the old element previously at the index
     * @throws IndexOutOfBoundsException if the index is out of range
     */

    @Override
    public T set(int index, T element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        T old = (T) elements[index];
        elements[index] = element;
        return old;
    }

    private boolean add(int index, T element) {
        return false;
    }

    @Override
    public boolean remove(Object object) {
        return false;
    }

    @Override
    public T remove(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();

        Object item = elements[index];
        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }

        size--;
        elements[size] = null;

        return (T) item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException();

        return (T) elements[index];
    }

    /**
     * Returns the number of elements in the array list
     *
     * @return the size of the list
     * */
    @Override
    public int size() {
        return size;
    }

    /**
     * Checks whether the array list is empty
     *
     * @return true if the list contains no elements, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {

    }

    public int capacity() {
        return elements.length;
    }

    private void ensureCapacity() {
        if (size == elements.length) {
            int newSize = elements.length * 2;
            elements = Arrays.copyOf(elements, newSize);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i != size - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
