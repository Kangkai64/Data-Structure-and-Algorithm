package adt;

import java.io.Serializable;
import java.util.Iterator;
import utility.HashUtility;

/**
 * ArrayBucketList - A custom ADT that combines ArrayList and LinkedList functionality
 * The main array stores LinkedList buckets, and items are added based on hash index
 */
public class ArrayBucketList<T> implements ListInterface<T>, Serializable, Iterable<T> {
    public LinkedList[] buckets;
    private int numberOfEntries;
    private int bucketCount;
    private String firstHashString;
    private String lastHashString;
    private static final int DEFAULT_BUCKET_COUNT = 10;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    /**
     * Default constructor with 10 buckets
     */
    public ArrayBucketList() {
        this(DEFAULT_BUCKET_COUNT);
    }

    /**
     * Constructor with specified bucket count
     * @param bucketCount number of buckets in the array
     */
    public ArrayBucketList(int bucketCount) {
        this.bucketCount = bucketCount;
        this.numberOfEntries = 0;
        this.buckets = (LinkedList[]) new ArrayBucketList<?>.LinkedList[bucketCount];
        
        // Initialize all buckets
        for (int index = 0; index < bucketCount; index++) {
            buckets[index] = new LinkedList();
        }
    }

    /**
     * Add an entry to the bucket list based on hash index
     * We need an hashString to add an entry to the bucket list
     * This method is not used in the implementation
     * @param newEntry the entry to add
     * @return true if successfully added
     */
    @Override
    public boolean add(T newEntry) {
        return false;
    }

    /**
     * Add an entry to the bucket list based on hash index
     * @param hashString hash string to add entry for
     * @param newEntry entry to add
     * @return true if successfully added
     */
    public boolean add(String hashString, T newEntry) {
        if (newEntry == null) {
            return false;
        }

        // Check load factor and resize if necessary
        if (getLoadFactor() > LOAD_FACTOR_THRESHOLD) {
            resizeBuckets();
        }

        // Generate hash index for the entry
        int bucketIndex = HashUtility.hashEntity(hashString, bucketCount);
        
        // Add to the appropriate bucket
        buckets[bucketIndex].add(newEntry);
        numberOfEntries++;
        if (numberOfEntries == 1) {
            firstHashString = hashString;
        } else {
            lastHashString = hashString;
        }
        return true;
    }

    /**
     * Method was here because of the interface, but it is not used in the implementation
     * The newPosition is ignored
     */
    @Override
    public boolean add(int newPosition, T newEntry) {
        return add(newEntry);
    }

    /**
     * Method was here because of the interface, but it is not used in the implementation
     * The givenPosition is ignored
     */
    @Override
    public T remove(int givenPosition) {
        return null;
    }

    /**
     * Remove an entry from the bucket list
     * @param entry entry to remove
     * @return removed entry or null if not found
     */
    public T remove(T entry) {
        for (int bucketIndex = 0; bucketIndex < bucketCount; bucketIndex++) {
            LinkedList bucket = buckets[bucketIndex];
            for (T bucketEntry : bucket) {
                if (bucketEntry.equals(entry)) {
                    bucket.remove(entry);
                    numberOfEntries--;
                    return entry;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Remove an entry from the bucket list by hash string
     * @param hashString hash string to remove
     * @return removed entry or null if not found
     */
    public T removeByHash(String hashString) {
        int bucketIndex = HashUtility.hashEntity(hashString, bucketCount);
            LinkedList bucket = buckets[bucketIndex];
            for (T bucketEntry : bucket) {
                if (bucketEntry.equals(hashString)) {
                    bucket.remove(bucketEntry);
                    numberOfEntries--;
                    return bucketEntry;
                } else {
                    return null;
                }
        }
        return null;
    }

    /**
     * Clear all entries from the bucket list
     */
    @Override
    public void clear() {
        for (int index = 0; index < bucketCount; index++) {
            buckets[index].clear();
        }
        numberOfEntries = 0;
    }

    /**
     * Replace entry at given position
     * Linked List cannot be accessed by index, so it is not possible to replace an entry at a given position
     * givenPosition is ignored
     * @param newEntry new entry
     * @return true if successfully replaced
     */
    @Override
    public boolean replace(int givenPosition, T newEntry) {
        return false;
    }

    /**
     * Replace an entry in the bucket list
     * @param oldEntry old entry to replace
     * @param newEntry new entry to replace with
     * @return true if successfully replaced
     */
    public boolean replace(T oldEntry, T newEntry) {
        for (int bucketIndex = 0; bucketIndex < bucketCount; bucketIndex++) {
            LinkedList bucket = buckets[bucketIndex];
            for (T bucketEntry : bucket) {
                if (bucketEntry.equals(oldEntry)) {
                    bucket.remove(bucketEntry);
                    bucket.add(newEntry);
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Get entry at given position
     * Linked List cannot be accessed by index, so it is not possible to get an entry at a given position
     * givenPosition is ignored
     * @return entry at position or null if not found
     */
    @Override
    public T getEntry(int givenPosition) {
        return null;
    }

    /**
     * Get entry by hash string
     * @param hashString hash string to get entry for
     * @return entry or null if not found
     */
    public T getEntryByHash(String hashString) {
        int bucketIndex = HashUtility.hashEntity(hashString, bucketCount);
        LinkedList bucket = buckets[bucketIndex];
        Iterator<T> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            T bucketEntry = iterator.next();
            if (bucketEntry.equals(hashString)) {
                return bucketEntry;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Get the first entry from the bucket list
     * @return first entry or null if not found
     */
    public T getFirstEntry() {
        return getEntryByHash(firstHashString);
    }
    
    /**
     * Check if entry exists in the bucket list
     * @param anEntry entry to search for
     * @return true if found
     */
    @Override
    public boolean contains(T anEntry) {
        for (int bucketIndex = 0; bucketIndex < bucketCount; bucketIndex++) {
            LinkedList bucket = buckets[bucketIndex];
            for (T bucketEntry : bucket) {
                if (bucketEntry.equals(anEntry)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Get total number of entries
     * @return number of entries
     */
    @Override
    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    /**
     * Check if bucket list is empty
     * @return true if empty
     */
    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    /**
     * Check if bucket list is full (always false for dynamic structure)
     * @return false
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /**
     * Calculate current load factor
     * @return load factor (total entries / bucket count)
     */
    public double getLoadFactor() {
        return (double) numberOfEntries / bucketCount;
    }

    /**
     * Get bucket count
     * @return number of buckets
     */
    public int getBucketCount() {
        return bucketCount;
    }

    /**
     * Resize buckets by doubling the array size
     */
    private void resizeBuckets() {
        int newBucketCount = bucketCount * 2;
        LinkedList[] oldBuckets = buckets;
        buckets = (LinkedList[]) new ArrayBucketList<?>.LinkedList[newBucketCount];
        
        // Initialize new buckets
        for (int index = 0; index < newBucketCount; index++) {
            buckets[index] = new LinkedList();
        }
        
        // Rehash all existing entries
        numberOfEntries = 0;
        for (LinkedList oldBucket : oldBuckets) {
            for (T entry : oldBucket) {
                add(entry);
            }
        }
        
        bucketCount = newBucketCount;
    }

    /**
     * Create room for insertion at given position
     * @param newPosition position to make room for
     */
    private void makeRoom(int newPosition) {
        // This method is not typically used in bucket list implementation
        // but is required by the interface
        // Implementation would involve complex reorganization
    }

    /**
     * Remove gap at given position
     * @param givenPosition position to remove gap at
     */
    private void removeGap(int givenPosition) {
        // This method is not typically used in bucket list implementation
        // but is required by the interface
        // Implementation would involve complex reorganization
    }

    /**
     * String representation of the bucket list
     * @return string representation
     */
    @Override
    public String toString() {
        StringBuilder outputStr = new StringBuilder();
        outputStr.append("ArrayBucketList with ").append(numberOfEntries)
                 .append(" entries in ").append(bucketCount).append(" buckets:\n");
        
        for (int bucketIndex = 0; bucketIndex < bucketCount; bucketIndex++) {
            outputStr.append("Bucket ").append(bucketIndex).append(": ");
            outputStr.append(buckets[bucketIndex].toString()).append("\n");
        }
        
        return outputStr.toString();
    }

    /**
     * Iterator for the main bucket list
     * @return iterator for all entries
     */
    @Override
    public Iterator<T> iterator() {
        return new BucketListIterator();
    }

    /**
     * Iterator for the main bucket list (sequential access)
     */
    private class BucketListIterator implements Iterator<T> {
        private int currentBucketIndex;
        private Node currentNode;

        public BucketListIterator() {
            currentBucketIndex = 0;
            currentNode = null;
            findNextNode();
        }

        @Override
        public boolean hasNext() {
            return currentNode != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements in the bucket list");
            }
            
            T data = currentNode.getData();
            currentNode = currentNode.getNext();
            
            // If we've reached the end of current bucket, move to next bucket
            if (currentNode == null) {
                currentBucketIndex++;
                findNextNode();
            }
            
            return data;
        }

        private void findNextNode() {
            // Find the next non-empty bucket
            while (currentBucketIndex < bucketCount) {
                LinkedList bucket = buckets[currentBucketIndex];
                if (!bucket.isEmpty()) {
                    currentNode = bucket.head;
                    return;
                }
                currentBucketIndex++;
            }
            currentNode = null; // No more elements
        }
    }

    /**
     * Inner LinkedList class for bucket implementation
     */
    public class LinkedList implements Iterable<T> {
        private Node head;
        private Node tail;
        private int size;
        private static final int MAX_SIZE = 10;

        public LinkedList() {
            head = null;
            tail = null;
            size = 0;
        }

        /**
         * Add data to the linked list
         * @param data data to add
         */
        public void add(T data) {
            if (data == null) {
                return;
            }

            Node newNode = new Node(data);
            
            if (head == null) {
                // First node
                head = newNode;
                tail = newNode;
                head.setNext(head);
                head.setPrevious(head);
            } else if (size < MAX_SIZE) {
                // Add to end if not at max size
                newNode.setPrevious(tail);
                newNode.setNext(head);
                tail.setNext(newNode);
                head.setPrevious(newNode);
                tail = newNode;
            } else {
                // Throws error if at max size
                throw new IllegalStateException("Linked list is at max size");
            }
            
            size++;
        }

        /**
         * Remove data from the linked list
         * @param data data to remove
         */
        public void remove(T data) {
            if (head == null || data == null) {
                return;
            }

            Node current = head;
            do {
                if (current.getData().equals(data)) {
                    if (size == 1) {
                        // Only one node
                        head = null;
                        tail = null;
                    } else if (current == head) {
                        // Remove head
                        head = head.getNext();
                        head.setPrevious(tail);
                        tail.setNext(head);
                    } else if (current == tail) {
                        // Remove tail
                        tail = tail.getPrevious();
                        tail.setNext(head);
                        head.setPrevious(tail);
                    } else {
                        // Remove middle node
                        current.getPrevious().setNext(current.getNext());
                        current.getNext().setPrevious(current.getPrevious());
                    }
                    size--;
                    return;
                }
                current = current.getNext();
            } while (current != head);
        }

        /**
         * Check if data exists in the linked list
         * @param data data to search for
         * @return true if found
         */
        public boolean contains(T data) {
            if (head == null || data == null) {
                return false;
            }

            Node current = head;
            do {
                if (current.getData().equals(data)) {
                    return true;
                }
                current = current.getNext();
            } while (current != head);
            
            return false;
        }

        /**
         * Clear the linked list
         */
        public void clear() {
            head = null;
            tail = null;
            size = 0;
        }

        /**
         * Check if linked list is empty
         * @return true if empty
         */
        public boolean isEmpty() {
            return head == null;
        }

        /**
         * Get size of linked list
         * @return number of nodes
         */
        public int getSize() {
            return size;
        }

        /**
         * String representation of the linked list
         * @return string representation
         */
        @Override
        public String toString() {
            if (head == null) {
                return "[]";
            }

            StringBuilder stringBuilder = new StringBuilder("[");
            Node current = head;
            do {
                stringBuilder.append(current.getData());
                if (current.getNext() != head) {
                    stringBuilder.append(", ");
                }
                current = current.getNext();
            } while (current != head);
            stringBuilder.append("]");
            
            return stringBuilder.toString();
        }

        /**
         * Iterator for the linked list
         * @return iterator for linked list entries
         */
        @Override
        public Iterator<T> iterator() {
            return new LinkedListIterator();
        }

        /**
         * Iterator for the linked list
         */
        private class LinkedListIterator implements Iterator<T> {
            private Node currentNode;
            private int visitedCount;

            public LinkedListIterator() {
                currentNode = head;
                visitedCount = 0;
            }

            @Override
            public boolean hasNext() {
                return currentNode != null && visitedCount < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException("No more elements in the linked list");
                }
                
                T data = currentNode.getData();
                currentNode = currentNode.getNext();
                visitedCount++;
                
                return data;
            }
        }
    }

    /**
     * Inner Node class for linked list implementation
     */
    private class Node {
        private T data;
        private Node next;
        private Node previous;

        /**
         * Constructor with data
         * @param data data to store
         */
        public Node(T data) {
            this.data = data;
            this.next = null;
            this.previous = null;
        }

        /**
         * Constructor with data and links
         * @param data data to store
         * @param next next node
         * @param previous previous node
         */
        public Node(T data, Node next, Node previous) {
            this.data = data;
            this.next = next;
            this.previous = previous;
        }

        /**
         * Get data from node
         * @return data
         */
        public T getData() {
            return data;
        }

        /**
         * Set data in node
         * @param data data to set
         */
        public void setData(T data) {
            this.data = data;
        }

        /**
         * Get next node
         * @return next node
         */
        public Node getNext() {
            return next;
        }

        /**
         * Set next node
         * @param next next node
         */
        public void setNext(Node next) {
            this.next = next;
        }

        /**
         * Get previous node
         * @return previous node
         */
        public Node getPrevious() {
            return previous;
        }

        /**
         * Set previous node
         * @param previous previous node
         */
        public void setPrevious(Node previous) {
            this.previous = previous;
        }
    }
}