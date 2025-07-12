package adt;

/**
 * Queue implementation using custom ADT
 * @author Clinic Management System Team
 */
public class Queue<T> implements QueueInterface<T> {
    
    private static final int DEFAULT_CAPACITY = 100;
    private T[] queue;
    private int frontIndex;
    private int backIndex;
    private int numberOfEntries;
    private boolean initialized = false;
    
    public Queue() {
        this(DEFAULT_CAPACITY);
    }
    
    public Queue(int initialCapacity) {
        @SuppressWarnings("unchecked")
        T[] tempQueue = (T[]) new Object[initialCapacity + 1];
        queue = tempQueue;
        frontIndex = 0;
        backIndex = initialCapacity;
        numberOfEntries = 0;
        initialized = true;
    }
    
    @Override
    public boolean enqueue(T newEntry) {
        checkInitialization();
        boolean result = true;
        if (isFull()) {
            result = false;
        } else {
            backIndex = (backIndex + 1) % queue.length;
            queue[backIndex] = newEntry;
            numberOfEntries++;
        }
        return result;
    }
    
    @Override
    public T dequeue() {
        checkInitialization();
        T front = null;
        if (!isEmpty()) {
            front = queue[frontIndex];
            queue[frontIndex] = null;
            frontIndex = (frontIndex + 1) % queue.length;
            numberOfEntries--;
        }
        return front;
    }
    
    @Override
    public T getFront() {
        checkInitialization();
        T front = null;
        if (!isEmpty()) {
            front = queue[frontIndex];
        }
        return front;
    }
    
    @Override
    public void clear() {
        while (!isEmpty()) {
            dequeue();
        }
    }
    
    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }
    
    @Override
    public boolean isEmpty() {
        return frontIndex == ((backIndex + 1) % queue.length);
    }
    
    @Override
    public boolean isFull() {
        return frontIndex == ((backIndex + 2) % queue.length);
    }
    
    @Override
    public boolean contains(T anEntry) {
        checkInitialization();
        boolean found = false;
        int index = frontIndex;
        int count = 0;
        
        while (!found && (count < numberOfEntries)) {
            if (anEntry.equals(queue[index])) {
                found = true;
            } else {
                index = (index + 1) % queue.length;
                count++;
            }
        }
        return found;
    }
    
    private void checkInitialization() {
        if (!initialized) {
            throw new SecurityException("Queue object is not initialized properly.");
        }
    }
} 