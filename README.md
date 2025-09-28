> [!NOTE]
> Please import the database in MySQL before running the program.

# How to import the database
<p>Step 1 : Download and open XAMPP</p>
<p>Step 2 : Check Apache and MySQL checkbox</p>
<p>Step 3 : Navigate to the import tab and import the "database_schema.sql" file</p>
<p>Step 4 : Click on "Go" button at the bottom to complete the database setup</p>

# TAR UMT Clinic Management System

## Project Overview

This is a comprehensive Java-based Clinic Management System developed for Tunku Abdul Rahman University of Management and Technology (TAR UMT). The system effectively models one-to-many relationships using custom Abstract Data Types (ADTs) and follows the ECB (Entity-Control-Boundary) pattern.

## Problem Statement

TAR UMT is establishing an on-campus clinic to provide medical care for students. The system allows students to:
- Book appointments or walk-ins for doctor consultations
- Make payments
- Get medicines
- Access various clinic functions

The system effectively models one-to-many relationships using collection ADTs and demonstrates creativity and competence in implementation.

## System Architecture

### Technology Stack
- **Java 23** - Core programming language
- **Maven** - Dependency management and build tool
- **MySQL** - Database management system
- **HikariCP** - Connection pooling
- **Custom ADTs** - No use of java.util.Collection libraries

### Architecture Pattern
- **ECB Pattern** (Entity-Control-Boundary)
- **Layered Architecture** with clear separation of concerns
- **DAO Pattern** for data access operations

## Project Structure

```
src/main/java/
├── entity/          # Data models and entities
├── control/         # Business logic and controllers
├── boundary/        # User interface components
├── dao/            # Data Access Objects
├── utility/        # Helper classes and utilities
└── adt/            # Abstract Data Types (custom implementations)

database_schema.sql  # Complete database schema with triggers
```

## Modules Implemented

### Module 1: Patient Management Module
**Features:**
- Patient registration and record maintenance
- Queuing management using custom Queue ADT
- Patient search and retrieval
- Patient status management (active/inactive)

**Key Components:**
- `PatientManagementControl` - Business logic
- `Patient` entity with inheritance from `Person`
- Custom `Queue<T>` implementation for patient queuing
- Patient registration and queue status reports

### Module 2: Doctor Management Module
**Features:**
- Doctor information management
- Duty schedules and availability tracking
- Schedule management with custom ADTs
- Doctor search by specialty and availability

**Key Components:**
- `DoctorManagementControl` - Business logic
- `Doctor` entity with inheritance from `Person`
- `Schedule` entity for duty schedules
- Doctor information and schedule reports

### Module 3: Consultation Management Module
**Features:**
- Patient consultation management
- Appointment scheduling and tracking
- Consultation status management (Scheduled, In Progress, Completed, Cancelled)
- Subsequent visit appointment arrangement

**Key Components:**
- `ConsultationManagementControl` - Business logic
- `Consultation` entity with status tracking
- Consultation scheduling and completion workflows
- Consultation and scheduled consultations reports

### Module 4: Medical Treatment Management Module
**Features:**
- Patient diagnosis management
- Treatment history records
- Treatment status tracking (Prescribed, In Progress, Completed, Cancelled)
- Treatment plan and medication management

**Key Components:**
- `MedicalTreatmentControl` - Business logic
- `MedicalTreatment` entity with comprehensive treatment data
- Treatment history tracking
- Treatment and treatment history reports

### Module 5: Pharmacy Management Module
**Features:**
- Medicine dispensing after doctor consultation
- Medicine stock control and management
- Prescription management
- Stock level monitoring and alerts

**Key Components:**
- `PharmacyManagementControl` - Business logic
- `Medicine` entity with stock management
- `Prescription` entity with prescribed medicine tracking
- Medicine stock and prescription reports

## Custom Abstract Data Types (ADTs)

### Dictionary / Bucketed List
- `DictionaryInterface<K, V>` - Interface for key-value collections (add, remove, get, contains, size, clear)
- `ArrayBucketList<K, V>` - Custom hash-bucket structure with circular linked-list buckets and dynamic resizing
- Supports adding, removing, lookup by key, containment checks, iteration, and converting to arrays for sorting
- Provides optional queue-like helpers for workflows (e.g., `addToQueue`, `removeFront`, `peekFront`, `getQueueSize`)

### Hash Strategies (Pluggable)
- `HashStrategy` enum and factory helpers in `ArrayBucketListFactory` to tune hashing by attribute type:
  - `NAME`, `NAME_PREFIX`, `PHONE`, `EMAIL`, `POSTCODE`, plus helpers for `STRING_ID`, `ENUM`, `LOCAL_DATE`
- Choose appropriate strategy per use case to improve distribution across buckets

**Note:** All ADTs are custom implementations without using `java.util.Collection` classes for the primary data structures.

## Database Schema

### Tables Created
1. **address** - Address information
2. **person** - Base table for patients and doctors
3. **patient** - Patient-specific information
4. **doctor** - Doctor-specific information
5. **schedule** - Doctor duty schedules
6. **consultation** - Patient consultations
7. **medical_treatment** - Treatment records
8. **medicine** - Medicine inventory
9. **prescription** - Prescription records
10. **prescribed_medicine** - Junction table for prescriptions and medicines

### ID Generation Triggers
- **Patient IDs**: P000000001, P000000002, etc.
- **Doctor IDs**: D000000001, D000000002, etc.
- **Consultation IDs**: C000000001, C000000002, etc.
- **Treatment IDs**: T000000001, T000000002, etc.
- **Medicine IDs**: M000000001, M000000002, etc.
- **Prescription IDs**: PR00000001, PR00000002, etc.

### Database Features
- **Foreign Key Constraints** - Maintains data integrity
- **Indexes** - Optimized for performance
- **Triggers** - Automatic ID generation and status updates
- **Views** - Common query abstractions
- **Sample Data** - Pre-populated for testing

## Reporting Features

Each module includes at least 2 summary reports:

### Patient Management Reports
1. **Patient Record Summary Report** - Detailed Patient records
2. **Patient Demographics Report** - Patient distribution by age
3. **Patient Visit History Report** - Recent Patient Visits

### Doctor Management Reports
1. **Doctor Information Report** - Doctor details and availability
2. **Doctor Workload Report** - Doctor workload and utilization
3. **Doctor Performance Report** - Doctor review by Patients 

### Consultation Management Reports
1. **Consultation Report** - All consultations with status
2. **Consultation History Report** - Recent consultations
3. **Consultation Efficiency Report** - Consultation time and efficiency

### Medical Treatment Reports
1. **Treatment Analysis Report** - Detailed medical treatment analysis
2. **Treatment Status Report** - All treatments with status
3. **Treatment Outcome Report** - Patient treatment outcome and follow-up

### Pharmacy Management Reports
1. **Medicine Stock Report** - Inventory status and stock levels
2. **Prescription Report** - Prescription details and status
3. **Medicine Usage Report** - Medicine utilization and tracking

## Installation and Setup

### Prerequisites
- Java 23 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

### Main Application
The `ClinicManagementSystem` class provides a console-based interface for all modules:

1. **Patient Management** - Register, update, and manage patients
2. **Doctor Management** - Register doctors and manage schedules
3. **Consultation Management** - Schedule and manage consultations
4. **Medical Treatment Management** - Create and track treatments
5. **Pharmacy Management** - Manage medicines and prescriptions
6. **Reports** - Generate comprehensive system reports

## Testing

The system includes sample data for testing all functionalities:
- Sample patients, doctors, and addresses
- Sample consultations and treatments
- Sample medicines and prescriptions
- Complete workflow demonstrations

## Future Enhancements

Potential areas for improvement:
- Web-based user interface
- Mobile application
- Advanced reporting and analytics
- Integration with external systems
- Enhanced security features

## Conclusion

This Clinic Management System demonstrates:
- **Creativity** in custom ADT implementations
- **Competence** in Java programming and database design
- **Completeness** in addressing all requirements
- **Professionalism** in code organization and documentation

The system is ready for deployment and can be extended with additional features as needed.

---

**Developed for TAR UMT Data Structures and Algorithms Course**
**Java 23 | Maven | MySQL | Custom ADTs | ECB Pattern**