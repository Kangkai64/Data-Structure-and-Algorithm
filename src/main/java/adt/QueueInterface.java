package adt;

/**
 * Queue Interface for custom ADT implementation
 * @author Clinic Management System Team
 */
public interface QueueInterface<T> {
    
    /**
     * Adds a new entry to the back of the queue
     * @param newEntry the object to be added
     * @return true if the addition is successful, false otherwise
     */
    public boolean enqueue(T newEntry);
    
    /**
     * Removes and returns the entry at the front of the queue
     * @return the entry at the front of the queue, or null if the queue is empty
     */
    public T dequeue();
    
    /**
     * Retrieves the entry at the front of the queue without removing it
     * @return the entry at the front of the queue, or null if the queue is empty
     */
    public T getFront();
    
    /**
     * Removes all entries from the queue
     */
    public void clear();
    
    /**
     * Gets the number of entries currently in the queue
     * @return the integer number of entries currently in the queue
     */
    public int getNumberOfEntries();
    
    /**
     * Sees whether the queue is empty
     * @return true if the queue is empty, false if not
     */
    public boolean isEmpty();
    
    /**
     * Sees whether the queue is full
     * @return true if the queue is full, false if not
     */
    public boolean isFull();
    
    /**
     * Checks if the queue contains a specific entry
     * @param anEntry the object to search for
     * @return true if the queue contains anEntry, false if not
     */
    public boolean contains(T anEntry);
} 