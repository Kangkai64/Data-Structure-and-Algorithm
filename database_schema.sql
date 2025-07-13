-- Clinic Management System Database Schema
-- TAR UMT Clinic Management System
-- Created for Java 23 Maven Project

-- Drop database if exists and create new one
DROP DATABASE IF EXISTS clinic_management_system;
CREATE DATABASE clinic_management_system;
USE clinic_management_system;

-- Create tables with proper relationships and constraints

-- 1. Address table
CREATE TABLE address (
    addressId VARCHAR(20) PRIMARY KEY,
    street VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    postalCode VARCHAR(10) NOT NULL,
    country VARCHAR(50) DEFAULT 'Malaysia',
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Patient table (with Person fields distributed)
CREATE TABLE patient (
    patientId VARCHAR(20) PRIMARY KEY,
    fullName VARCHAR(100) NOT NULL,
    ICNumber VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phoneNumber VARCHAR(20) NOT NULL,
    addressId VARCHAR(20),
    registrationDate DATE NOT NULL,
    wardNumber VARCHAR(20),
    bloodType ENUM('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 
                   'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE') NOT NULL,
    allergies TEXT,
    emergencyContact VARCHAR(20) NOT NULL,
    isActive BOOLEAN DEFAULT TRUE,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (addressId) REFERENCES address(addressId) ON DELETE SET NULL
);

-- 3. Doctor table (with Person fields distributed)
CREATE TABLE doctor (
    doctorId VARCHAR(20) PRIMARY KEY,
    fullName VARCHAR(100) NOT NULL,
    ICNumber VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phoneNumber VARCHAR(20) NOT NULL,
    addressId VARCHAR(20),
    registrationDate DATE NOT NULL,
    medicalSpecialty VARCHAR(100) NOT NULL,
    licenseNumber VARCHAR(50) UNIQUE NOT NULL,
    expYears INT NOT NULL,
    isAvailable BOOLEAN DEFAULT TRUE,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (addressId) REFERENCES address(addressId) ON DELETE SET NULL
);

-- 4. Schedule table
CREATE TABLE schedule (
    scheduleId VARCHAR(20) PRIMARY KEY,
    doctorId VARCHAR(20) NOT NULL,
    dayOfWeek ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    fromTime TIME NOT NULL,
    toTime TIME NOT NULL,
    isAvailable BOOLEAN DEFAULT TRUE,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctorId) REFERENCES doctor(doctorId) ON DELETE CASCADE
);

-- 5. Consultation table
CREATE TABLE consultation (
    consultationId VARCHAR(20) PRIMARY KEY,
    patientId VARCHAR(20) NOT NULL,
    doctorId VARCHAR(20) NOT NULL,
    consultationDate DATETIME NOT NULL,
    symptoms TEXT,
    diagnosis TEXT,
    treatment TEXT,
    notes TEXT,
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    nextVisitDate DATE,
    consultationFee DECIMAL(10,2) NOT NULL,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patientId) REFERENCES patient(patientId) ON DELETE CASCADE,
    FOREIGN KEY (doctorId) REFERENCES doctor(doctorId) ON DELETE CASCADE
);

-- 6. Medical Treatment table
CREATE TABLE medical_treatment (
    treatmentId VARCHAR(20) PRIMARY KEY,
    patientId VARCHAR(20) NOT NULL,
    doctorId VARCHAR(20) NOT NULL,
    consultationId VARCHAR(20),
    diagnosis TEXT NOT NULL,
    treatmentPlan TEXT,
    prescribedMedications TEXT,
    treatmentNotes TEXT,
    treatmentDate DATETIME NOT NULL,
    followUpDate DATE,
    status ENUM('PRESCRIBED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'PRESCRIBED',
    treatmentCost DECIMAL(10,2) NOT NULL,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patientId) REFERENCES patient(patientId) ON DELETE CASCADE,
    FOREIGN KEY (doctorId) REFERENCES doctor(doctorId) ON DELETE CASCADE,
    FOREIGN KEY (consultationId) REFERENCES consultation(consultationId) ON DELETE SET NULL
);

-- 7. Medicine table
CREATE TABLE medicine (
    medicineId VARCHAR(20) PRIMARY KEY,
    medicineName VARCHAR(100) NOT NULL,
    genericName VARCHAR(100),
    manufacturer VARCHAR(100) NOT NULL,
    description TEXT,
    dosageForm VARCHAR(50) NOT NULL,
    strength VARCHAR(50),
    quantityInStock INT NOT NULL DEFAULT 0,
    minimumStockLevel INT NOT NULL DEFAULT 10,
    unitPrice DECIMAL(10,2) NOT NULL,
    expiryDate DATE NOT NULL,
    storageLocation VARCHAR(100),
    requiresPrescription BOOLEAN DEFAULT TRUE,
    status ENUM('AVAILABLE', 'LOW_STOCK', 'OUT_OF_STOCK', 'DISCONTINUED') DEFAULT 'AVAILABLE',
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Prescription table
CREATE TABLE prescription (
    prescriptionId VARCHAR(20) PRIMARY KEY,
    patientId VARCHAR(20) NOT NULL,
    doctorId VARCHAR(20) NOT NULL,
    consultationId VARCHAR(20),
    prescriptionDate DATETIME NOT NULL,
    instructions TEXT,
    expiryDate DATE NOT NULL,
    status ENUM('ACTIVE', 'DISPENSED', 'EXPIRED', 'CANCELLED') DEFAULT 'ACTIVE',
    totalCost DECIMAL(10,2) DEFAULT 0.00,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patientId) REFERENCES patient(patientId) ON DELETE CASCADE,
    FOREIGN KEY (doctorId) REFERENCES doctor(doctorId) ON DELETE CASCADE,
    FOREIGN KEY (consultationId) REFERENCES consultation(consultationId) ON DELETE SET NULL
);

-- 9. Prescribed Medicine table (junction table for prescription and medicine)
CREATE TABLE prescribed_medicine (
    prescribedMedicineId VARCHAR(20) PRIMARY KEY,
    prescriptionId VARCHAR(20) NOT NULL,
    medicineId VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration INT NOT NULL, -- in days
    unitPrice DECIMAL(10,2) NOT NULL,
    totalCost DECIMAL(10,2) NOT NULL,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prescriptionId) REFERENCES prescription(prescriptionId) ON DELETE CASCADE,
    FOREIGN KEY (medicineId) REFERENCES medicine(medicineId) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_patient_address ON patient(addressId);
CREATE INDEX idx_patient_ic ON patient(ICNumber);
CREATE INDEX idx_patient_email ON patient(email);
CREATE INDEX idx_doctor_address ON doctor(addressId);
CREATE INDEX idx_doctor_ic ON doctor(ICNumber);
CREATE INDEX idx_doctor_email ON doctor(email);
CREATE INDEX idx_consultation_patient ON consultation(patientId);
CREATE INDEX idx_consultation_doctor ON consultation(doctorId);
CREATE INDEX idx_treatment_patient ON medical_treatment(patientId);
CREATE INDEX idx_treatment_doctor ON medical_treatment(doctorId);
CREATE INDEX idx_prescription_patient ON prescription(patientId);
CREATE INDEX idx_prescription_doctor ON prescription(doctorId);
CREATE INDEX idx_prescribed_medicine_prescription ON prescribed_medicine(prescriptionId);
CREATE INDEX idx_prescribed_medicine_medicine ON prescribed_medicine(medicineId);
CREATE INDEX idx_schedule_doctor ON schedule(doctorId);

-- Create triggers for automatic ID generation

-- Trigger for Address ID generation
DELIMITER //
CREATE TRIGGER tr_address_id_generation
BEFORE INSERT ON address
FOR EACH ROW
BEGIN
    IF NEW.addressId IS NULL OR NEW.addressId = '' THEN
        SET NEW.addressId = CONCAT('A', LPAD((SELECT COUNT(*) + 1 FROM address), 9, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Patient ID generation
DELIMITER //
CREATE TRIGGER tr_patient_id_generation
BEFORE INSERT ON patient
FOR EACH ROW
BEGIN
    IF NEW.patientId IS NULL OR NEW.patientId = '' THEN
        SET NEW.patientId = CONCAT('P', LPAD((SELECT COUNT(*) + 1 FROM patient), 9, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Doctor ID generation
DELIMITER //
CREATE TRIGGER tr_doctor_id_generation
BEFORE INSERT ON doctor
FOR EACH ROW
BEGIN
    IF NEW.doctorId IS NULL OR NEW.doctorId = '' THEN
        SET NEW.doctorId = CONCAT('D', LPAD((SELECT COUNT(*) + 1 FROM doctor), 9, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Schedule ID generation
DELIMITER //
CREATE TRIGGER tr_schedule_id_generation
BEFORE INSERT ON schedule
FOR EACH ROW
BEGIN
    IF NEW.scheduleId IS NULL OR NEW.scheduleId = '' THEN
        SET NEW.scheduleId = CONCAT('SCH', LPAD((SELECT COUNT(*) + 1 FROM schedule), 7, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Consultation ID generation
DELIMITER //
CREATE TRIGGER tr_consultation_id_generation
BEFORE INSERT ON consultation
FOR EACH ROW
BEGIN
    IF NEW.consultationId IS NULL OR NEW.consultationId = '' THEN
        SET NEW.consultationId = CONCAT('C', LPAD((SELECT COUNT(*) + 1 FROM consultation), 9, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Medical Treatment ID generation
DELIMITER //
CREATE TRIGGER tr_treatment_id_generation
BEFORE INSERT ON medical_treatment
FOR EACH ROW
BEGIN
    IF NEW.treatmentId IS NULL OR NEW.treatmentId = '' THEN
        SET NEW.treatmentId = CONCAT('T', LPAD((SELECT COUNT(*) + 1 FROM medical_treatment), 9, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Medicine ID generation
DELIMITER //
CREATE TRIGGER tr_medicine_id_generation
BEFORE INSERT ON medicine
FOR EACH ROW
BEGIN
    IF NEW.medicineId IS NULL OR NEW.medicineId = '' THEN
        SET NEW.medicineId = CONCAT('M', LPAD((SELECT COUNT(*) + 1 FROM medicine), 9, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Prescription ID generation
DELIMITER //
CREATE TRIGGER tr_prescription_id_generation
BEFORE INSERT ON prescription
FOR EACH ROW
BEGIN
    IF NEW.prescriptionId IS NULL OR NEW.prescriptionId = '' THEN
        SET NEW.prescriptionId = CONCAT('PR', LPAD((SELECT COUNT(*) + 1 FROM prescription), 8, '0'));
    END IF;
END//
DELIMITER ;

-- Trigger for Prescribed Medicine ID generation
DELIMITER //
CREATE TRIGGER tr_prescribed_medicine_id_generation
BEFORE INSERT ON prescribed_medicine
FOR EACH ROW
BEGIN
    IF NEW.prescribedMedicineId IS NULL OR NEW.prescribedMedicineId = '' THEN
        SET NEW.prescribedMedicineId = CONCAT('PM', LPAD((SELECT COUNT(*) + 1 FROM prescribed_medicine), 8, '0'));
    END IF;
END//
DELIMITER ;

-- Create triggers for automatic status updates

-- Trigger to update medicine status based on stock level
DELIMITER //
CREATE TRIGGER tr_medicine_status_update
BEFORE UPDATE ON medicine
FOR EACH ROW
BEGIN
    IF NEW.quantityInStock = 0 THEN
        SET NEW.status = 'OUT_OF_STOCK';
    ELSEIF NEW.quantityInStock <= NEW.minimumStockLevel THEN
        SET NEW.status = 'LOW_STOCK';
    ELSE
        SET NEW.status = 'AVAILABLE';
    END IF;
END//
DELIMITER ;

-- Trigger to update prescription total cost when prescribed medicine is added
DELIMITER //
CREATE TRIGGER tr_prescription_total_cost_update
AFTER INSERT ON prescribed_medicine
FOR EACH ROW
BEGIN
    UPDATE prescription 
    SET totalCost = (
        SELECT SUM(totalCost) 
        FROM prescribed_medicine 
        WHERE prescriptionId = NEW.prescriptionId
    )
    WHERE prescriptionId = NEW.prescriptionId;
END//
DELIMITER ;

-- Trigger to update prescription total cost when prescribed medicine is updated
DELIMITER //
CREATE TRIGGER tr_prescription_total_cost_update_on_update
AFTER UPDATE ON prescribed_medicine
FOR EACH ROW
BEGIN
    UPDATE prescription 
    SET totalCost = (
        SELECT SUM(totalCost) 
        FROM prescribed_medicine 
        WHERE prescriptionId = NEW.prescriptionId
    )
    WHERE prescriptionId = NEW.prescriptionId;
END//
DELIMITER ;

-- Trigger to update prescription total cost when prescribed medicine is deleted
DELIMITER //
CREATE TRIGGER tr_prescription_total_cost_update_on_delete
AFTER DELETE ON prescribed_medicine
FOR EACH ROW
BEGIN
    UPDATE prescription 
    SET totalCost = (
        SELECT COALESCE(SUM(totalCost), 0) 
        FROM prescribed_medicine 
        WHERE prescriptionId = OLD.prescriptionId
    )
    WHERE prescriptionId = OLD.prescriptionId;
END//
DELIMITER ;

-- Create views for common queries

-- View for active patients with their details
CREATE VIEW v_active_patients AS
SELECT 
    p.patientId,
    p.fullName,
    p.ICNumber,
    p.email,
    p.phoneNumber,
    p.wardNumber,
    p.bloodType,
    p.allergies,
    p.emergencyContact,
    p.registrationDate
FROM patient p
WHERE p.isActive = TRUE;

-- View for available doctors with their schedules
CREATE VIEW v_available_doctors AS
SELECT 
    d.doctorId,
    d.fullName,
    d.email,
    d.phoneNumber,
    d.medicalSpecialty,
    d.licenseNumber,
    d.expYears,
    COUNT(s.scheduleId) as scheduleCount
FROM doctor d
LEFT JOIN schedule s ON d.doctorId = s.doctorId AND s.isAvailable = TRUE
WHERE d.isAvailable = TRUE
GROUP BY d.doctorId, d.fullName, d.email, d.phoneNumber, d.medicalSpecialty, d.licenseNumber, d.expYears;

-- View for medicine stock status
CREATE VIEW v_medicine_stock_status AS
SELECT 
    medicineId,
    medicineName,
    genericName,
    manufacturer,
    quantityInStock,
    minimumStockLevel,
    unitPrice,
    status,
    expiryDate,
    CASE 
        WHEN quantityInStock = 0 THEN 'OUT_OF_STOCK'
        WHEN quantityInStock <= minimumStockLevel THEN 'LOW_STOCK'
        WHEN expiryDate <= CURDATE() THEN 'EXPIRED'
        ELSE 'AVAILABLE'
    END as stockStatus
FROM medicine;

-- View for consultation summary
CREATE VIEW v_consultation_summary AS
SELECT 
    c.consultationId,
    p.fullName as patientName,
    d.fullName as doctorName,
    c.consultationDate,
    c.status,
    c.consultationFee,
    c.diagnosis,
    c.nextVisitDate
FROM consultation c
JOIN patient p ON c.patientId = p.patientId
JOIN doctor d ON c.doctorId = d.doctorId;

-- Insert sample data for testing

-- Sample Addresses
INSERT INTO address (addressId, street, city, state, postalCode) VALUES
('A000000001', '123 Jalan Utama', 'Kuala Lumpur', 'WP Kuala Lumpur', '50000'),
('A000000002', '456 Taman Melati', 'Petaling Jaya', 'Selangor', '47400'),
('A000000003', '789 Bandar Baru', 'Shah Alam', 'Selangor', '40000'),
('A000000004', '321 Taman Universiti', 'Skudai', 'Johor', '81300'),
('A000000005', '654 Jalan Hospital', 'Kuala Lumpur', 'WP Kuala Lumpur', '50586');

-- Sample Patients (with Person fields distributed)
INSERT INTO patient (patientId, fullName, ICNumber, email, phoneNumber, addressId, registrationDate, wardNumber, bloodType, allergies, emergencyContact) VALUES
('P000000001', 'Ahmad bin Abdullah', '900101015432', 'ahmad.abdullah@email.com', '0123456789', 'A000000001', '2024-01-15', 'W001', 'A_POSITIVE', 'Penicillin,Peanuts', '0198765432'),
('P000000002', 'Siti binti Mohamed', '920202025678', 'siti.mohamed@email.com', '0123456790', 'A000000002', '2024-01-16', 'W002', 'O_POSITIVE', 'Shellfish', '0198765433'),
('P000000003', 'Mohammed bin Ali', '940505058901', 'mohammed.ali@email.com', '0123456793', 'A000000005', '2024-01-18', 'W003', 'B_POSITIVE', 'None', '0198765434');

-- Sample Doctors (with Person fields distributed)
INSERT INTO doctor (doctorId, fullName, ICNumber, email, phoneNumber, addressId, registrationDate, medicalSpecialty, licenseNumber, expYears) VALUES
('D000000001', 'Dr. Lim Wei Chen', '850303036789', 'dr.lim@clinic.com', '0123456791', 'A000000003', '2024-01-10', 'General Medicine', 'MD001234', 8),
('D000000002', 'Dr. Sarah Johnson', '880404047890', 'dr.sarah@clinic.com', '0123456792', 'A000000004', '2024-01-12', 'Cardiology', 'MD001235', 12);

-- Sample Schedules
INSERT INTO schedule (scheduleId, doctorId, dayOfWeek, fromTime, toTime) VALUES
('SCH0000001', 'D000000001', 'MONDAY', '09:00:00', '17:00:00'),
('SCH0000002', 'D000000001', 'TUESDAY', '09:00:00', '17:00:00'),
('SCH0000003', 'D000000001', 'WEDNESDAY', '09:00:00', '17:00:00'),
('SCH0000004', 'D000000002', 'MONDAY', '08:00:00', '16:00:00'),
('SCH0000005', 'D000000002', 'THURSDAY', '08:00:00', '16:00:00');

-- Sample Medicines
INSERT INTO medicine (medicineId, medicineName, genericName, manufacturer, description, dosageForm, strength, quantityInStock, minimumStockLevel, unitPrice, expiryDate, storageLocation, requiresPrescription) VALUES
('M000000001', 'Paracetamol', 'Acetaminophen', 'PharmaCorp', 'Pain reliever and fever reducer', 'Tablet', '500mg', 1000, 100, 0.50, '2025-12-31', 'Shelf A1', FALSE),
('M000000002', 'Amoxicillin', 'Amoxicillin', 'MediPharm', 'Antibiotic for bacterial infections', 'Capsule', '250mg', 500, 50, 2.00, '2025-06-30', 'Shelf B2', TRUE),
('M000000003', 'Ibuprofen', 'Ibuprofen', 'HealthCare Ltd', 'Anti-inflammatory pain reliever', 'Tablet', '400mg', 750, 75, 0.75, '2025-09-30', 'Shelf A2', FALSE),
('M000000004', 'Omeprazole', 'Omeprazole', 'GastroMed', 'Proton pump inhibitor for acid reflux', 'Capsule', '20mg', 300, 30, 3.50, '2025-03-31', 'Shelf C1', TRUE);

-- Sample Consultations
INSERT INTO consultation (consultationId, patientId, doctorId, consultationDate, symptoms, consultationFee) VALUES
('C000000001', 'P000000001', 'D000000001', '2024-01-20 10:00:00', 'Fever and headache', 50.00),
('C000000002', 'P000000002', 'D000000002', '2024-01-21 14:00:00', 'Chest pain and shortness of breath', 80.00),
('C000000003', 'P000000003', 'D000000001', '2024-01-22 11:00:00', 'Stomach pain and nausea', 50.00);

-- Sample Medical Treatments
INSERT INTO medical_treatment (treatmentId, patientId, doctorId, consultationId, diagnosis, treatmentPlan, treatmentCost) VALUES
('T000000001', 'P000000001', 'D000000001', 'C000000001', 'Common cold with fever', 'Rest, fluids, and paracetamol for fever', 25.00),
('T000000002', 'P000000002', 'D000000002', 'C000000002', 'Hypertension', 'Lifestyle changes and medication monitoring', 45.00),
('T000000003', 'P000000003', 'D000000001', 'C000000003', 'Gastritis', 'Diet modification and omeprazole', 35.00);

-- Sample Prescriptions
INSERT INTO prescription (prescriptionId, patientId, doctorId, consultationId, prescriptionDate, instructions, expiryDate) VALUES
('PR00000001', 'P000000001', 'D000000001', 'C000000001', '2024-01-20 10:30:00', 'Take 2 tablets every 6 hours for fever', '2024-02-20'),
('PR00000002', 'P000000002', 'D000000002', 'C000000002', '2024-01-21 14:30:00', 'Take 1 capsule daily with food', '2024-03-21'),
('PR00000003', 'P000000003', 'D000000001', 'C000000003', '2024-01-22 11:30:00', 'Take 1 capsule daily before breakfast', '2024-02-22');

-- Sample Prescribed Medicines
INSERT INTO prescribed_medicine (prescribedMedicineId, prescriptionId, medicineId, quantity, dosage, frequency, duration, unitPrice, totalCost) VALUES
('PM00000001', 'PR00000001', 'M000000001', 20, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PM00000002', 'PR00000002', 'M000000002', 14, '250mg', 'Twice daily', 7, 2.00, 28.00),
('PM00000003', 'PR00000003', 'M000000004', 30, '20mg', 'Once daily', 30, 3.50, 105.00);

-- Update consultation status to completed
UPDATE consultation SET status = 'COMPLETED', diagnosis = 'Common cold with fever', treatment = 'Rest and medication' WHERE consultationId = 'C000000001';
UPDATE consultation SET status = 'COMPLETED', diagnosis = 'Hypertension', treatment = 'Lifestyle changes and monitoring' WHERE consultationId = 'C000000002';
UPDATE consultation SET status = 'COMPLETED', diagnosis = 'Gastritis', treatment = 'Diet modification and medication' WHERE consultationId = 'C000000003';

-- Update treatment status to completed
UPDATE medical_treatment SET status = 'COMPLETED' WHERE treatmentId = 'T000000001';
UPDATE medical_treatment SET status = 'COMPLETED' WHERE treatmentId = 'T000000002';
UPDATE medical_treatment SET status = 'COMPLETED' WHERE treatmentId = 'T000000003';

-- Update prescription status to dispensed
UPDATE prescription SET status = 'DISPENSED' WHERE prescriptionId = 'PR00000001';
UPDATE prescription SET status = 'DISPENSED' WHERE prescriptionId = 'PR00000002';
UPDATE prescription SET status = 'DISPENSED' WHERE prescriptionId = 'PR00000003';

-- Display database creation confirmation
SELECT 'Clinic Management System Database Created Successfully!' as Status;
SELECT COUNT(*) as TotalTables FROM information_schema.tables WHERE table_schema = 'clinic_management_system';
SELECT COUNT(*) as TotalTriggers FROM information_schema.triggers WHERE trigger_schema = 'clinic_management_system';
SELECT COUNT(*) as TotalViews FROM information_schema.views WHERE table_schema = 'clinic_management_system'; 