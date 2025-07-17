package adt;

/**
 * @author Frank M. Carrano
 * @version 2.0
 */

import java.io.Serializable;
import java.util.Iterator;

public class ArrayList<T> implements ListInterface<T>, Serializable, Iterable<T> {

  private T[] array;
  private int numberOfEntries;
  private static final int DEFAULT_CAPACITY = 5;
  private int frontIndex;
  private int backIndex;

  public ArrayList() {
    this(DEFAULT_CAPACITY);
  }

  public ArrayList(int initialCapacity) {
    numberOfEntries = 0;
    array = (T[]) new Object[initialCapacity];
    frontIndex = 0;
    backIndex = 0;
  }

  @Override
  public boolean add(T newEntry) {
    if (isArrayFull()) {
      doubleArray();
    }

    array[numberOfEntries] = newEntry;
    numberOfEntries++;
    return true;
  }

  @Override
  public boolean add(int newPosition, T newEntry) {
    boolean isSuccessful = true;

    if ((newPosition >= 1) && (newPosition <= numberOfEntries + 1)) {
      if (isArrayFull()) {
        doubleArray();
      }
      makeRoom(newPosition);
      array[newPosition - 1] = newEntry;
      numberOfEntries++;
    } else {
      isSuccessful = false;
    }

    return isSuccessful;
  }

  @Override
  public T remove(int givenPosition) {
    T result = null;

    if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
      result = array[givenPosition - 1];

      if (givenPosition < numberOfEntries) {
        removeGap(givenPosition);
      }

      numberOfEntries--;
    }

    return result;
  }

  @Override
  public void clear() {
    numberOfEntries = 0;
  }

  @Override
  public boolean replace(int givenPosition, T newEntry) {
    boolean isSuccessful = true;

    if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
      array[givenPosition - 1] = newEntry;
    } else {
      isSuccessful = false;
    }

    return isSuccessful;
  }

  @Override
  public T getEntry(int givenPosition) {
    T result = null;

    if ((givenPosition >= 1) && (givenPosition <= numberOfEntries)) {
      result = array[givenPosition - 1];
    }

    return result;
  }

  @Override
  public boolean contains(T anEntry) {
    boolean found = false;
    for (int index = 0; !found && (index < numberOfEntries); index++) {
      if (anEntry.equals(array[index])) {
        found = true;
      }
    }
    return found;
  }

  @Override
  public int getNumberOfEntries() {
    return numberOfEntries;
  }

  @Override
  public boolean isEmpty() {
    return numberOfEntries == 0;
  }

  @Override
  public boolean isFull() {
    return false;
  }

  public boolean append(T newEntry) {
    if (getQueueSize() == array.length - 1) {
      doubleQueue();
    }
    array[backIndex] = newEntry;
    backIndex = (backIndex + 1) % array.length;
    return true;
  }

  public T removeFront() {
    if (isQueueEmpty()) {
      return null;
    }
    T item = array[frontIndex];
    array[frontIndex] = null;
    frontIndex = (frontIndex + 1) % array.length;
    return item;
  }

  public int getQueueSize() {
    if (backIndex >= frontIndex) {
      return backIndex - frontIndex;
    } else {
      return array.length - frontIndex + backIndex;
    }
  }

  public boolean inQueue(T item) {
    for (int index = frontIndex; index != backIndex; index = (index + 1) % array.length) {
      if (array[index].equals(item)) {
        return true;
      }
    }
    return false;
  }

  public void clearQueue() {
    frontIndex = 0;
    backIndex = 0;
  }

  private void doubleArray() {
    T[] oldArray = array;
    array = (T[]) new Object[oldArray.length * 2];
    for (int i = 0; i < oldArray.length; i++) {
      array[i] = oldArray[i];
    }
  }

  private boolean isArrayFull() {
    return numberOfEntries == array.length;
  }

  @Override
  public String toString() {
    String outputStr = "";
    for (int index = 0; index < numberOfEntries; ++index) {
      outputStr += array[index] + "\n";
    }

    return outputStr;
  }

  /**
   * Task: Makes room for a new entry at newPosition. Precondition: 1 <=
   * newPosition <= numberOfEntries + 1; numberOfEntries is array's
   * numberOfEntries before addition.
   */
  private void makeRoom(int newPosition) {
    int newIndex = newPosition - 1;
    int lastIndex = numberOfEntries - 1;

    // move each entry to next higher index, starting at end of
    // array and continuing until the entry at newIndex is moved
    for (int index = lastIndex; index >= newIndex; index--) {
      array[index + 1] = array[index];
    }
  }

  /**
   * Task: Shifts entries that are beyond the entry to be removed to the next
   * lower position. Precondition: array is not empty; 1 <= givenPosition <
   * numberOfEntries; numberOfEntries is array's numberOfEntries before removal.
   */
  private void removeGap(int givenPosition) {
    // move each entry to next lower position starting at entry after the
    // one removed and continuing until end of array
    int removedIndex = givenPosition - 1;
    int lastIndex = numberOfEntries - 1;

    for (int index = removedIndex; index < lastIndex; index++) {
      array[index] = array[index + 1];
    }
  }

  private void doubleQueue() {
    T[] oldArray = array;
    array = (T[]) new Object[oldArray.length * 2];

    // Copy elements in correct order for circular queue
    int oldSize = getQueueSize();
    for (int index = 0; index < oldSize; index++) {
      array[index] = oldArray[(frontIndex + index) % oldArray.length];
    }

    frontIndex = 0;
    backIndex = oldSize;
  }

  private boolean isQueueEmpty() {
    return frontIndex == backIndex;
  }

  @Override
  public Iterator<T> iterator() {
    return new ArrayListIterator();
  }

  // Method to get queue iterator explicitly
  public Iterator<T> queueIterator() {
    return new QueueIterator();
  }

  // Iterator for ArrayList functionality (sequential from index 0)
  private class ArrayListIterator implements Iterator<T> {
    private int currentIndex;

    public ArrayListIterator() {
      currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
      return currentIndex < numberOfEntries;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new java.util.NoSuchElementException("No more elements in the list");
      }
      return array[currentIndex++];
    }
  }

  // Iterator for Queue functionality (circular from frontIndex)
  private class QueueIterator implements Iterator<T> {
    private int currentIndex;
    private int elementsReturned;

    public QueueIterator() {
      currentIndex = frontIndex;
      elementsReturned = 0;
    }

    @Override
    public boolean hasNext() {
      return elementsReturned < getQueueSize();
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new java.util.NoSuchElementException("No more elements in the queue");
      }

      T nextEntry = array[currentIndex];
      currentIndex = (currentIndex + 1) % array.length;
      elementsReturned++;
      return nextEntry;
    }
  }
}