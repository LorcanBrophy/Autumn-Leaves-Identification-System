package com.example.dsa2_ca1.model;

public interface MyList<T> {

    /**
     * Adds an element to the end of the list
     *
     * @param element the element to be added to the list
     * @return true if the element was added successfully
     */
    boolean add(T element);

    /**
     * Replaces the element at the specified index with the given element.
     *
     * @param index the index of the element to replace
     * @param element the element to set
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    T set(int index, T element);

    /**
     * Removes the first occurrence of the specified object from the list
     *
     * @param object the element to be removed from the list
     * @return true if the element was found and removed, false otherwise
     */
    boolean remove(Object object);



    /**
     * Removes the element at the specified position in the list
     *
     * @param index the position of the element to remove
     * @return the element that was removed
     * @throws IndexOutOfBoundsException if index < 0 or >= size
     */
    T remove(int index);

    /**
     * Returns the element at the specified position in the list
     *
     * @param index the position of the element to retrieve
     * @return the element at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range {@code (index < 0 || index >= size)}
     */
    T get(int index);

    /**
     * Returns the number of elements in the list
     *
     * @return the size of the list
     */
    int size();

    /**
     * Checks whether the list is empty
     *
     * @return true if the list contains no elements, false otherwise
     */
    boolean isEmpty();

    /**
     * Removes all elements from the list
     */
    void clear();
}
