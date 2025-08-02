# DictionaryInterface ADT Specification

## Overview
The `DictionaryInterface<K, V>` is a generic interface that defines the contract for a dictionary abstract data type. A dictionary stores key-value pairs where each key is unique and associated with a corresponding value. This interface provides the fundamental operations required for dictionary functionality in the Clinic Management System.

## Generic Parameters
- **K**: The type of keys stored in the dictionary (must be comparable and hashable)
- **V**: The type of values associated with the keys

## Core Operations

### 1. add(K key, V value)
**Purpose**: Adds a new key-value pair to the dictionary or updates an existing key's value.

**Preconditions**:
- `key` is not null
- `value` is not null

**Postconditions**:
- If the key does not exist: a new entry is added to the dictionary
- If the key already exists: the existing value is replaced with the new value
- Dictionary size increases by 1 (if new entry) or remains the same (if replacement)

**Returns**:
- `null` if a new entry was added
- The previous value if an existing key was updated

**Exceptions**:
- May throw `IllegalArgumentException` if key or value is null
- May throw `IllegalStateException` if dictionary is full (for fixed-size implementations)

### 2. remove(K key)
**Purpose**: Removes a specific key-value pair from the dictionary.

**Preconditions**:
- `key` is not null

**Postconditions**:
- If the key exists: the key-value pair is removed from the dictionary
- If the key does not exist: dictionary remains unchanged
- Dictionary size decreases by 1 (if key was found) or remains the same (if not found)

**Returns**:
- The value associated with the removed key
- `null` if the key was not found in the dictionary

**Exceptions**:
- May throw `IllegalArgumentException` if key is null

### 3. getValue(K key)
**Purpose**: Retrieves the value associated with a given key without removing it.

**Preconditions**:
- `key` is not null

**Postconditions**:
- Dictionary remains unchanged
- No side effects occur

**Returns**:
- The value associated with the specified key
- `null` if the key is not found in the dictionary

**Exceptions**:
- May throw `IllegalArgumentException` if key is null

### 4. contains(K key)
**Purpose**: Checks whether a specific key exists in the dictionary.

**Preconditions**:
- `key` is not null

**Postconditions**:
- Dictionary remains unchanged
- No side effects occur

**Returns**:
- `true` if the key exists in the dictionary
- `false` if the key is not found

**Exceptions**:
- May throw `IllegalArgumentException` if key is null

### 5. isEmpty()
**Purpose**: Determines whether the dictionary contains any entries.

**Preconditions**:
- None

**Postconditions**:
- Dictionary remains unchanged
- No side effects occur

**Returns**:
- `true` if the dictionary has no entries
- `false` if the dictionary contains at least one entry

### 6. isFull()
**Purpose**: Determines whether the dictionary has reached its maximum capacity.

**Preconditions**:
- None

**Postconditions**:
- Dictionary remains unchanged
- No side effects occur

**Returns**:
- `true` if the dictionary cannot accept more entries
- `false` if the dictionary can accept additional entries

**Note**: For dynamic implementations (like ArrayBucketList), this method typically returns `false` as the structure can grow indefinitely.

### 7. getSize()
**Purpose**: Returns the current number of key-value pairs in the dictionary.

**Preconditions**:
- None

**Postconditions**:
- Dictionary remains unchanged
- No side effects occur

**Returns**:
- The number of entries currently stored in the dictionary (non-negative integer)

### 8. clear()
**Purpose**: Removes all entries from the dictionary.

**Preconditions**:
- None

**Postconditions**:
- All key-value pairs are removed from the dictionary
- Dictionary becomes empty (size = 0)
- All internal structures are reset to initial state

**Returns**:
- `void`

## Implementation Requirements

### Key Requirements
1. **Uniqueness**: Each key must be unique within the dictionary
2. **Null Handling**: Keys and values should not be null (implementation-dependent)
3. **Consistency**: Operations should maintain dictionary integrity
4. **Performance**: Operations should be efficient for typical use cases

### Expected Performance Characteristics
- **add()**: O(1) average case, O(n) worst case
- **remove()**: O(1) average case, O(n) worst case
- **getValue()**: O(1) average case, O(n) worst case
- **contains()**: O(1) average case, O(n) worst case
- **isEmpty()**: O(1)
- **isFull()**: O(1)
- **getSize()**: O(1)
- **clear()**: O(n)

## Usage in Clinic Management System

The DictionaryInterface serves as the foundation for various data structures in the Clinic Management System:

1. **Patient Records**: Store patient information with patient ID as key
2. **Doctor Schedules**: Map doctor IDs to their appointment schedules
3. **Medicine Inventory**: Track medicine stock with medicine ID as key
4. **Appointment Management**: Store appointments with appointment ID as key
5. **Treatment Records**: Map patient IDs to their treatment histories

## Design Patterns

The interface follows these design principles:
- **Separation of Concerns**: Clear distinction between interface and implementation
- **Generic Programming**: Type-safe operations with generic parameters
- **Contract-Based Design**: Well-defined preconditions and postconditions
- **Extensibility**: Easy to extend with additional operations as needed

## Related Classes
- **ArrayBucketList**: Primary implementation of this interface
- **HashUtility**: Provides hash functions for key distribution
- **Entity Classes**: Patient, Doctor, Medicine, etc. that use this interface 