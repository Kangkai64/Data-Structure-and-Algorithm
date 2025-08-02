# ArrayBucketList ADT Specification

## Overview
The `ArrayBucketList<K, V>` is a sophisticated hybrid data structure that combines the efficiency of hash-based storage with the flexibility of linked lists. It implements the `DictionaryInterface<K, V>` and provides additional queue-like functionality for the Clinic Management System. The structure uses an array of linked lists (buckets) to handle hash collisions efficiently while maintaining fast access times.

## Architecture

### Core Components
1. **Bucket Array**: An array of linked lists for hash-based storage
2. **Queue Data**: A separate linked list for queue operations
3. **Hash Function**: Custom hash function for key distribution
4. **Load Factor Management**: Automatic resizing based on load factor threshold

### Internal Classes
- **LinkedList**: Circular doubly-linked list implementation for buckets
- **Node**: Individual node containing key-value pairs
- **BucketListIterator**: Iterator for main bucket list
- **LinkedListIterator**: Iterator for individual linked lists

## Generic Parameters
- **K**: The type of keys (must implement hashCode() and equals())
- **V**: The type of values associated with the keys

## Core Operations

### Dictionary Interface Operations

#### 1. add(K key, V value)
**Purpose**: Adds a new key-value pair to the bucket list or updates an existing key's value.

**Algorithm**:
1. Validate key and value (null check)
2. Calculate hash index using `hashEntity(key, bucketCount)`
3. Access the appropriate bucket (linked list)
4. Search for existing key in the bucket
5. If key exists: update value and return old value
6. If key doesn't exist: add new node and increment size
7. Check load factor and resize if necessary

**Preconditions**:
- `key` is not null
- `value` is not null

**Postconditions**:
- If key doesn't exist: new entry added to appropriate bucket
- If key exists: existing value replaced
- Load factor checked and buckets resized if threshold exceeded
- Size counter updated appropriately

**Returns**:
- `null` if new entry added
- Previous value if existing key updated

**Time Complexity**: O(1) average case, O(n) worst case (when resizing)

#### 2. remove(K key)
**Purpose**: Removes a specific key-value pair from the bucket list.

**Algorithm**:
1. Calculate hash index for the key
2. Access the appropriate bucket
3. Search for the key in the bucket
4. If found: remove node and decrement size
5. If not found: return null

**Preconditions**:
- `key` is not null

**Postconditions**:
- If key exists: node removed from bucket, size decremented
- If key doesn't exist: no changes made

**Returns**:
- Value associated with removed key
- `null` if key not found

**Time Complexity**: O(1) average case, O(n) worst case

#### 3. getValue(K key)
**Purpose**: Retrieves the value associated with a given key without removing it.

**Algorithm**:
1. Calculate hash index for the key
2. Access the appropriate bucket
3. Search for the key in the bucket
4. Return the associated value or null

**Preconditions**:
- `key` is not null

**Postconditions**:
- No changes to the data structure

**Returns**:
- Value associated with the key
- `null` if key not found

**Time Complexity**: O(1) average case, O(n) worst case

#### 4. contains(K key)
**Purpose**: Checks whether a specific key exists in the bucket list.

**Algorithm**:
1. Calculate hash index for the key
2. Access the appropriate bucket
3. Search for the key in the bucket
4. Return true if found, false otherwise

**Preconditions**:
- `key` is not null

**Postconditions**:
- No changes to the data structure

**Returns**:
- `true` if key exists
- `false` if key not found

**Time Complexity**: O(1) average case, O(n) worst case

#### 5. isEmpty()
**Purpose**: Determines whether the bucket list contains any entries.

**Algorithm**:
- Return true if `numberOfEntries == 0`

**Preconditions**:
- None

**Postconditions**:
- No changes to the data structure

**Returns**:
- `true` if no entries exist
- `false` if at least one entry exists

**Time Complexity**: O(1)

#### 6. isFull()
**Purpose**: Determines whether the bucket list has reached capacity.

**Algorithm**:
- Return true if `numberOfEntries == bucketCount`

**Preconditions**:
- None

**Postconditions**:
- No changes to the data structure

**Returns**:
- `true` if bucket list is at capacity
- `false` if more entries can be added

**Time Complexity**: O(1)

#### 7. getSize()
**Purpose**: Returns the current number of entries in the bucket list.

**Algorithm**:
- Return `numberOfEntries`

**Preconditions**:
- None

**Postconditions**:
- No changes to the data structure

**Returns**:
- Number of entries (non-negative integer)

**Time Complexity**: O(1)

#### 8. clear()
**Purpose**: Removes all entries from the bucket list.

**Algorithm**:
1. Clear all buckets (linked lists)
2. Reset `numberOfEntries` to 0

**Preconditions**:
- None

**Postconditions**:
- All entries removed
- Size reset to 0
- All buckets cleared

**Returns**:
- `void`

**Time Complexity**: O(n)

### Queue Operations

#### 1. addToQueue(K key, V value)
**Purpose**: Adds a key-value pair to the queue data structure.

**Algorithm**:
1. Validate key and value
2. Add to `queueData` linked list

**Preconditions**:
- `key` is not null
- `value` is not null

**Postconditions**:
- New entry added to queue
- Queue size increased by 1

**Returns**:
- `void`

**Time Complexity**: O(1)

#### 2. removeFront()
**Purpose**: Removes and returns the first element from the queue.

**Algorithm**:
1. Check if queue is empty
2. If not empty: remove head node and return its value
3. If empty: return null

**Preconditions**:
- None

**Postconditions**:
- If queue not empty: first element removed
- If queue empty: no changes

**Returns**:
- Value of first element
- `null` if queue is empty

**Time Complexity**: O(1)

#### 3. peekFront()
**Purpose**: Returns the first element from the queue without removing it.

**Algorithm**:
1. Check if queue is empty
2. Return head node's value or null

**Preconditions**:
- None

**Postconditions**:
- No changes to the queue

**Returns**:
- Value of first element
- `null` if queue is empty

**Time Complexity**: O(1)

### Utility Operations

#### 1. getLoadFactor()
**Purpose**: Calculates the current load factor of the bucket list.

**Algorithm**:
- Return `(double) numberOfEntries / bucketCount`

**Preconditions**:
- None

**Postconditions**:
- No changes to the data structure

**Returns**:
- Current load factor (double value)

**Time Complexity**: O(1)

#### 2. toString()
**Purpose**: Provides a string representation of the bucket list.

**Algorithm**:
1. Build string with entry count and bucket count
2. Iterate through all buckets
3. Append each bucket's string representation

**Preconditions**:
- None

**Postconditions**:
- No changes to the data structure

**Returns**:
- String representation of the bucket list

**Time Complexity**: O(n)

## Internal Data Structures

### LinkedList Class
**Purpose**: Circular doubly-linked list implementation for bucket storage.

**Key Features**:
- Circular structure (head.next points to head)
- Doubly-linked nodes (previous and next pointers)
- Maximum size limit (16 nodes per bucket)
- Efficient add/remove operations

**Operations**:
- `add(K key, V value)`: Add new node to end
- `remove(K key)`: Remove node by key
- `getNodeByKey(K key)`: Find node by key
- `clear()`: Remove all nodes
- `isEmpty()`: Check if list is empty
- `toString()`: String representation

### Node Class
**Purpose**: Individual node containing key-value pair and navigation pointers.

**Properties**:
- `key`: The key value
- `value`: The associated value
- `next`: Reference to next node
- `previous`: Reference to previous node

**Methods**:
- Getters and setters for all properties
- Constructor for key-value initialization

## Performance Characteristics

### Time Complexity
- **Average Case**: O(1) for most operations
- **Worst Case**: O(n) when hash collisions occur frequently
- **Resizing**: O(n) when load factor threshold is exceeded

### Space Complexity
- **Storage**: O(n) where n is the number of entries
- **Overhead**: Minimal due to efficient linked list implementation

### Load Factor Management
- **Threshold**: 0.75 (75% capacity)
- **Resizing**: Doubles bucket count when threshold exceeded
- **Rehashing**: All entries redistributed after resize

## Hash Function
```java
private int hashEntity(K key, int bucketCount) {
    return Math.abs(key.hashCode()) % bucketCount;
}
```

**Characteristics**:
- Uses Java's built-in hashCode() method
- Applies absolute value to handle negative hash codes
- Modulo operation ensures index within bucket range
- Provides good distribution for typical key types

## Iterator Implementation

### BucketListIterator
**Purpose**: Iterates through all values in the bucket list.

**Algorithm**:
1. Start with first bucket
2. Find first non-empty bucket
3. Iterate through bucket's linked list
4. Move to next bucket when current bucket exhausted
5. Continue until all buckets processed

**Methods**:
- `hasNext()`: Check if more elements exist
- `next()`: Return next value
- `findNextNode()`: Locate next non-empty bucket

### LinkedListIterator
**Purpose**: Iterates through nodes in a single linked list.

**Algorithm**:
1. Start with head node
2. Track visited count to prevent infinite loops
3. Return nodes sequentially
4. Stop when all nodes visited

## Usage in Clinic Management System

### Primary Applications
1. **Patient Registry**: Store patient records with patient ID as key
2. **Doctor Directory**: Map doctor IDs to doctor information
3. **Medicine Inventory**: Track medicine stock with medicine ID
4. **Appointment Queue**: Queue management for patient appointments
5. **Treatment Records**: Store patient treatment histories

### Queue Functionality
- **Patient Queuing**: Manage walk-in patient queues
- **Appointment Scheduling**: Queue-based appointment management
- **Medicine Dispensing**: Queue for pharmacy operations

## Design Patterns

### Implemented Patterns
1. **Hash Table Pattern**: Efficient key-value storage
2. **Linked List Pattern**: Collision resolution
3. **Iterator Pattern**: Sequential access to elements
4. **Factory Pattern**: Node creation and management
5. **Strategy Pattern**: Hash function implementation

### Benefits
- **Efficiency**: O(1) average case operations
- **Flexibility**: Dynamic resizing and queue operations
- **Memory Efficiency**: Minimal overhead per entry
- **Type Safety**: Generic implementation
- **Extensibility**: Easy to add new operations

## Error Handling

### Null Validation
- All public methods validate null keys and values
- Returns null or throws exceptions as appropriate
- Prevents data corruption from null inputs

### Capacity Management
- Automatic resizing prevents overflow
- Load factor monitoring maintains performance
- Graceful handling of maximum bucket sizes

## Related Classes
- **DictionaryInterface**: Contract implementation
- **HashUtility**: Alternative hash functions
- **Entity Classes**: Patient, Doctor, Medicine, etc.
- **Control Classes**: Business logic that uses this ADT 