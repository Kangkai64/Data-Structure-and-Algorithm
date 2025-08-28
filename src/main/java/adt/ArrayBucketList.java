package adt;

import java.io.Serializable;
import java.util.Iterator;

/**
 * ArrayBucketList - A custom ADT that combines ArrayList and LinkedList
 * functionality
 * The main array stores LinkedList buckets, and items are added based on hash
 * index
 */
public class ArrayBucketList<K, V> implements DictionaryInterface<K, V>, Serializable, Iterable<V> {
    private LinkedList[] buckets;
    private LinkedList queueData;
    private int numberOfEntries;
    private int bucketCount;
    private static final int DEFAULT_BUCKET_COUNT = 1 << 4;
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    /**
     * Default constructor with 16 buckets
     */
    public ArrayBucketList() {
        this(DEFAULT_BUCKET_COUNT);
    }

    /**
     * Constructor with specified bucket count
     * 
     * @param bucketCount number of buckets in the array
     */
    public ArrayBucketList(int bucketCount) {
        this.bucketCount = bucketCount;
        this.numberOfEntries = 0;
        this.buckets = (LinkedList[]) new ArrayBucketList<?, ?>.LinkedList[bucketCount];
        this.queueData = new LinkedList();

        for (int index = 0; index < bucketCount; index++) {
            buckets[index] = new LinkedList();
        }
    }

    /**
     * Returns the current number of elements in the queue.
     *
     * This method safely checks if the queue's internal data structure (queueData)
     * is initialized
     * before attempting to access its size. This prevents a NullPointerException if
     * the queue has not
     * been instantiated.
     *
     * @return The number of elements in the queue, or 0 if the queue is not
     *         initialized.
     */
    public int getQueueSize() {
        return queueData != null ? queueData.size : 0;
    }

    /**
     * Hash function (moved from HashUtility)
     */
    private int hashEntity(K key, int bucketCount) {
        if (key == null) {
            return 0;
        }

        String keyStr = key.toString();
        int hash = 0;

        // Use a prime multiplier and process each character
        // This helps break up patterns in sequential IDs
        for (int i = 0; i < keyStr.length(); i++) {
            hash = hash * 31 + keyStr.charAt(i);
        }

        // Additional mixing to improve distribution
        hash ^= (hash >>> 16); // XOR with upper bits
        hash *= 0x85ebca6b; // Multiply by a large prime-like number
        hash ^= (hash >>> 13); // More mixing

        return Math.abs(hash) % bucketCount;
    }

    /**
     * Adds a new entry to the dictionary. If the given search key already exists,
     * replaces the value.
     * 
     * @param key   an object search key of the new entry
     * @param value an object associated with the search key
     * @return either null if the new entry was added or the value that was replaced
     */
    @Override
    public V add(K key, V value) {
        if (key == null || value == null) {
            return null;
        }
        int bucketIndex = hashEntity(key, bucketCount);
        LinkedList bucket = buckets[bucketIndex];
        Node node = bucket.getNodeByKey(key);
        if (node != null) {
            V oldValue = node.getValue();
            node.setValue(value);
            return oldValue;
        } else {
            bucket.add(key, value);
            numberOfEntries++;
            if (getLoadFactor() > LOAD_FACTOR_THRESHOLD) {
                resizeBuckets();
            }
            return null;
        }
    }

    /**
     * Adds a key-value pair to the queue if it is valid and not already present.
     * - Ignores insertion if the key or value is null.
     * - Prevents duplicate entries by checking if the key already exists.
     *
     * @param key   the unique identifier (e.g., patient ID)
     * @param value the object associated with the key (e.g., patient record)
     */
    public void addToQueue(K key, V value) {
        if (key == null || value == null) {
            return;
        }
        // Avoid duplicate entries with same key in the queue
        if (queueData.getNodeByKey(key) != null) {
            return;
        }
        queueData.add(key, value);
    }

    /**
     * Checks whether a given key already exists in the queue.
     *
     * @param key the unique identifier to look for
     * @return true if the key exists, false otherwise
     */
    public boolean queueContains(K key) {
        if (queueData == null || key == null) {
            return false;
        }
        return queueData.getNodeByKey(key) != null;
    }

    /**
     * Removes a specific entry from the dictionary.
     * 
     * @param key an object search key of the entry to be removed
     * @return either the value that was associated with the search key or null if
     *         no such object exists
     */
    @Override
    public V remove(K key) {
        int bucketIndex = hashEntity(key, bucketCount);
        LinkedList bucket = buckets[bucketIndex];
        Node node = bucket.getNodeByKey(key);
        if (node != null) {
            V value = node.getValue();
            bucket.remove(key);
            numberOfEntries--;
            return value;
        }
        return null;
    }

    public V removeFront() {
        if (queueData.isEmpty()) {
            return null;
        }
        V value = queueData.head.getValue();
        queueData.remove(queueData.head.getKey());
        return value;
    }

    public V peekFront() {
        if (queueData.isEmpty()) {
            return null;
        }
        return queueData.head.getValue();
    }

    /**
     * Retrieves the value associated with a given search key.
     * 
     * @param key an object search key of the entry to be retrieved
     * @return either the value that is associated with the search key or null if no
     *         such object exists
     */
    @Override
    public V getValue(K key) {
        int bucketIndex = hashEntity(key, bucketCount);
        LinkedList bucket = buckets[bucketIndex];
        Node node = bucket.getNodeByKey(key);
        return node != null ? node.getValue() : null;
    }

    /**
     * Sees whether a specific entry is in the dictionary.
     * 
     * @param key an object search key of the desired entry
     * @return true if key is associated with an entry in the dictionary
     */
    @Override
    public boolean contains(K key) {
        int bucketIndex = hashEntity(key, bucketCount);
        LinkedList bucket = buckets[bucketIndex];
        return bucket.getNodeByKey(key) != null;
    }

    /**
     * Sees whether the dictionary is empty.
     * 
     * @return true if the dictionary is empty
     */
    @Override
    public boolean isEmpty() {
        return numberOfEntries == 0;
    }

    /**
     * Sees whether the dictionary is full (always false for dynamic structure)
     * 
     * @return true if the dictionary is full
     */
    @Override
    public boolean isFull() {
        return numberOfEntries == bucketCount;
    }

    /**
     * Gets the size of the dictionary.
     * 
     * @return the number of entries (key-value pairs) currently in the dictionary
     */
    @Override
    public int getSize() {
        return numberOfEntries;
    }

    /**
     * Removes all entries from the dictionary.
     */
    @Override
    public void clear() {
        for (int index = 0; index < bucketCount; index++) {
            buckets[index].clear();
        }
        numberOfEntries = 0;
    }

    /**
     * Calculate current load factor
     * 
     * @return load factor (total entries / bucket count)
     */
    public double getLoadFactor() {
        return (double) numberOfEntries / bucketCount;
    }

    /**
     * Resize buckets by doubling the array size
     */
    private void resizeBuckets() {
        int oldBucketCount = bucketCount;
        int newBucketCount = oldBucketCount * 2;
        LinkedList[] oldBuckets = buckets;

        buckets = (LinkedList[]) new ArrayBucketList<?, ?>.LinkedList[newBucketCount];
        for (int index = 0; index < newBucketCount; index++) {
            buckets[index] = new LinkedList();
        }

        bucketCount = newBucketCount; // ensure rehashing uses the new size
        numberOfEntries = 0;

        for (LinkedList oldBucket : oldBuckets) {
            for (Node node : oldBucket) {
                this.add((K) node.getKey(), (V) node.getValue());
            }
        }
    }

    /**
     * String representation of the bucket list
     * 
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

    public String parseElementsToString() {
        StringBuilder outputStr = new StringBuilder();
        for (V value : this) {
            outputStr.append(value.toString()).append("\n");
        }
        return outputStr.toString();
    }

    /**
     * Iterator for the main bucket list (values only)
     * 
     * @return iterator for all values
     */
    @Override
    public Iterator<V> iterator() {
        return new BucketListIterator();
    }

    /**
     * Iterator for the main bucket list (sequential access)
     */
    private class BucketListIterator implements Iterator<V> {
        private int currentBucketIndex;
        private Node currentNode;
        private Node bucketHead;

        public BucketListIterator() {
            currentBucketIndex = 0;
            currentNode = null;
            bucketHead = null;
            findNextNode();
        }

        @Override
        public boolean hasNext() {
            return currentNode != null;
        }

        @Override
        public V next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements in the bucket list");
            }
            V data = currentNode.getValue();
            // move to next within current bucket
            Node nextNode = currentNode.getNext();
            // if we have looped back to the head of this bucket, advance to next bucket
            if (nextNode == bucketHead) {
                currentBucketIndex++;
                findNextNode();
            } else {
                currentNode = nextNode;
            }
            return data;
        }

        private void findNextNode() {
            while (currentBucketIndex < bucketCount) {
                LinkedList bucket = buckets[currentBucketIndex];
                if (!bucket.isEmpty()) {
                    currentNode = bucket.head;
                    bucketHead = bucket.head;
                    return;
                } else {
                    currentBucketIndex++;
                }
            }
            currentNode = null;
            bucketHead = null;
        }
    }

    /**
     * Inner LinkedList class for bucket implementation
     */
    private class LinkedList implements Iterable<Node> {
        private Node head;
        private Node tail;
        private int size;
        private static final int MAX_SIZE = 1 << 4;

        public LinkedList() {
            head = null;
            tail = null;
            size = 0;
        }

        /**
         * Add key-value to the linked list
         * 
         * @param key   key to add
         * @param value value to add
         */
        public void add(K key, V value) {
            if (key == null || value == null) {
                return;
            }
            Node newNode = new Node(key, value);
            if (head == null) {
                head = newNode;
                tail = newNode;
                head.setNext(head);
                head.setPrevious(head);
            } else if (size < MAX_SIZE) {
                newNode.setPrevious(tail);
                newNode.setNext(head);
                tail.setNext(newNode);
                head.setPrevious(newNode);
                tail = newNode;
            } else {
                throw new IllegalStateException("Linked list is at max size");
            }
            size++;
        }

        /**
         * Remove by key from the linked list
         * 
         * @param key key to remove
         */
        public void remove(K key) {
            if (head == null || key == null) {
                return;
            }
            Node current = head;
            do {
                if (current.getKey().equals(key)) {
                    if (size == 1) {
                        head = null;
                        tail = null;
                    } else if (current == head) {
                        head = head.getNext();
                        head.setPrevious(tail);
                        tail.setNext(head);
                    } else if (current == tail) {
                        tail = tail.getPrevious();
                        tail.setNext(head);
                        head.setPrevious(tail);
                    } else {
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
         * Get node by key
         * 
         * @param key key to search for
         * @return Node if found, else null
         */
        public Node getNodeByKey(K key) {
            if (head == null || key == null) {
                return null;
            }
            Node current = head;
            do {
                if (current.getKey().equals(key)) {
                    return current;
                }
                current = current.getNext();
            } while (current != head);
            return null;
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
         * 
         * @return true if empty
         */
        public boolean isEmpty() {
            return head == null;
        }

        /**
         * String representation of the linked list
         * 
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
                stringBuilder.append(current.getKey()).append("=").append(current.getValue());
                if (current.getNext() != head) {
                    stringBuilder.append(", ");
                }
                current = current.getNext();
            } while (current != head);
            stringBuilder.append("]");
            return stringBuilder.toString();
        }

        /**
         * Iterator for the linked list (returns Node)
         * 
         * @return iterator for linked list entries
         */
        @Override
        public Iterator<Node> iterator() {
            return new LinkedListIterator();
        }

        private class LinkedListIterator implements Iterator<Node> {
            private Node currentNode;
            private int visitedCount;

            public LinkedListIterator() {
                currentNode = head;
                visitedCount = 0;
            }

            @Override
            public boolean hasNext() {
                return head != null && visitedCount < size;
            }

            @Override
            public Node next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException("No more elements in the linked list");
                }
                Node data = currentNode;
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
        private K key;
        private V value;
        private Node next;
        private Node previous;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
            this.previous = null;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }
    }
}