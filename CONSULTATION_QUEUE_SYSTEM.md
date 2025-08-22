# Consultation Queue System

## Overview

The Consultation Queue System implements a queue-based approach for managing patient consultations in the Clinic Management System. The system ensures that consultations are processed in the correct order based on scheduled time slots and prevents multiple consultations from running simultaneously for the same doctor.

## Key Features

### 1. Queue-Based Consultation Management
- **Separate Queues per Doctor**: Each doctor has their own consultation queue
- **Slot-Based Ordering**: Consultations are ordered by appointment time (ascending order)
- **System Date Integration**: Only consultations scheduled for the current system date are processed

### 2. Consultation State Management
- **Single Consultation per Doctor**: Only one consultation can be active per doctor at any time
- **Automatic Queue Processing**: The system automatically selects the next consultation based on slot time
- **State Tracking**: Tracks which doctors are currently in consultation

### 3. Queue Operations
- **Enqueue**: New consultations are automatically added to the appropriate doctor's queue
- **Dequeue**: Consultations are removed from the queue when started or cancelled
- **Peek**: View the next consultation without removing it from the queue

## Implementation Details

### Data Structures Used
- **ArrayBucketList**: The existing ADT is used for queue management
- **Doctor Queues**: `ArrayBucketList<String, ArrayBucketList<String, Consultation>>` - Maps doctor IDs to their consultation queues
- **Active Consultations**: `ArrayBucketList<String, String>` - Tracks which doctor is currently in consultation

### Key Methods

#### `startConsultation(String doctorId)`
- Checks if the doctor is already in consultation
- Retrieves scheduled consultations for the current date
- Orders consultations by slot time (ascending)
- Starts the earliest consultation and marks the doctor as busy
- Returns status message indicating success or failure

#### `getQueueStatus()`
- Displays queue status for all doctors working today
- Shows number of scheduled consultations per doctor
- Indicates which doctors are currently in consultation
- Shows the next consultation details for each doctor

#### `getDoctorConsultationStatus(String doctorId)`
- Returns current status for a specific doctor
- Shows if the doctor is in consultation or available
- Displays number of scheduled consultations

### Slot Key Generation
Consultations are ordered using a slot key generated from the consultation date and time:
```java
private String generateSlotKey(LocalDateTime consultationDate) {
    // Format: YYYYMMDDHHMM for proper sorting
    return String.format("%04d%02d%02d%02d%02d", 
        consultationDate.getYear(),
        consultationDate.getMonthValue(),
        consultationDate.getDayOfMonth(),
        consultationDate.getHour(),
        consultationDate.getMinute());
}
```

## User Interface Integration

### Menu Options
1. **Start Consultation (Queue-based)**: Uses the queue system to start consultations
2. **Start Consultation (By ID)**: Traditional method of starting consultations by ID
3. **View Queue Status**: Shows current queue status for all doctors

### Queue-Based Consultation Flow
1. User selects "Start Consultation (Queue-based)"
2. System prompts for doctor ID
3. System checks if doctor is available
4. If available, starts the earliest scheduled consultation
5. If busy, shows current consultation status
6. If no consultations scheduled, shows appropriate message

## Business Rules

### Queue Processing Rules
1. **Time-Based Ordering**: Consultations are processed in ascending order of appointment time
2. **Date Filtering**: Only consultations scheduled for the current system date are considered
3. **Single Active Consultation**: Only one consultation can be active per doctor at any time
4. **Automatic Selection**: The system automatically selects the next consultation based on slot time

### State Management Rules
1. **SCHEDULED**: Consultation is in the queue waiting to be processed
2. **IN_PROGRESS**: Consultation is currently active, doctor is busy
3. **COMPLETED**: Consultation is finished, doctor is available again
4. **CANCELLED**: Consultation is removed from the queue

## Error Handling

### Common Scenarios
1. **No Scheduled Consultations**: Returns "No scheduled consultations for today"
2. **Doctor Already Busy**: Returns "Doctor is already in consultation. Please complete the current consultation first"
3. **Database Errors**: Logs error and returns appropriate error message

## Testing

A test class `TestConsultationQueue.java` is provided to demonstrate the queue functionality:
- Creates test patients and doctors
- Schedules consultations for different time slots
- Tests queue processing and state management
- Demonstrates multiple doctor scenarios

## Benefits

1. **Efficient Processing**: Ensures consultations are processed in the correct order
2. **Resource Management**: Prevents overbooking and resource conflicts
3. **User-Friendly**: Clear status messages and queue visibility
4. **Scalable**: Supports multiple doctors with separate queues
5. **Consistent**: Uses existing ADT without modifications

## Future Enhancements

1. **Priority Queue**: Support for emergency consultations
2. **Queue Persistence**: Save queue state across system restarts
3. **Real-time Updates**: Live queue status updates
4. **Notification System**: Alert patients when their turn approaches
5. **Analytics**: Queue performance metrics and reporting
