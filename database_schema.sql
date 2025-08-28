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
    cancellationReason TEXT,
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
    totalCost DECIMAL(10,2) DEFAULT 0.00,
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

-- Create sequence tables for ID generation (no initialization needed)
CREATE TABLE address_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE patient_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE doctor_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE schedule_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE consultation_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE medical_treatment_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE medicine_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE prescription_seq (id INT AUTO_INCREMENT PRIMARY KEY);
CREATE TABLE prescribed_medicine_seq (id INT AUTO_INCREMENT PRIMARY KEY);

-- Trigger for Address ID generation
DELIMITER //
CREATE TRIGGER tr_address_id_generation
BEFORE INSERT ON address
FOR EACH ROW
BEGIN
    IF NEW.addressId IS NULL OR NEW.addressId = '' THEN
        INSERT INTO address_seq VALUES ();
        SET NEW.addressId = CONCAT('A', LPAD(LAST_INSERT_ID(), 9, '0'));
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
        INSERT INTO patient_seq VALUES ();
        SET NEW.patientId = CONCAT('P', LPAD(LAST_INSERT_ID(), 9, '0'));
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
        INSERT INTO doctor_seq VALUES ();
        SET NEW.doctorId = CONCAT('D', LPAD(LAST_INSERT_ID(), 9, '0'));
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
        INSERT INTO schedule_seq VALUES ();
        SET NEW.scheduleId = CONCAT('SCH', LPAD(LAST_INSERT_ID(), 8, '0'));
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
        INSERT INTO consultation_seq VALUES ();
        SET NEW.consultationId = CONCAT('C', LPAD(LAST_INSERT_ID(), 9, '0'));
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
        INSERT INTO medical_treatment_seq VALUES ();
        SET NEW.treatmentId = CONCAT('T', LPAD(LAST_INSERT_ID(), 9, '0'));
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
        INSERT INTO medicine_seq VALUES ();
        SET NEW.medicineId = CONCAT('M', LPAD(LAST_INSERT_ID(), 9, '0'));
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
        INSERT INTO prescription_seq VALUES ();
        SET NEW.prescriptionId = CONCAT('PR', LPAD(LAST_INSERT_ID(), 8, '0'));
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
        INSERT INTO prescribed_medicine_seq VALUES ();
        SET NEW.prescribedMedicineId = CONCAT('PM', LPAD(LAST_INSERT_ID(), 8, '0'));
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


-- Trigger to calculate totalCost for prescribed_medicine on INSERT
DELIMITER //
CREATE TRIGGER tr_prescribed_medicine_calculate_total_insert
BEFORE INSERT ON prescribed_medicine
FOR EACH ROW
BEGIN
    SET NEW.totalCost = NEW.quantity * NEW.unitPrice;
END//
DELIMITER ;

-- Trigger to calculate totalCost for prescribed_medicine on UPDATE
DELIMITER //
CREATE TRIGGER tr_prescribed_medicine_calculate_total_update
BEFORE UPDATE ON prescribed_medicine
FOR EACH ROW
BEGIN
    SET NEW.totalCost = NEW.quantity * NEW.unitPrice;
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

-- Insert comprehensive data for testing

-- Sample Addresses (expanded for more patients)
INSERT INTO address (street, city, state, postalCode) VALUES
('123 Jalan Utama', 'Kuala Lumpur', 'WP Kuala Lumpur', '50000'),
('456 Taman Melati', 'Petaling Jaya', 'Selangor', '47400'),
('789 Bandar Baru', 'Shah Alam', 'Selangor', '40000'),
('Jalan Melati Utama 1', 'Kuala Lumpur', 'WP Kuala Lumpur', '53100'),
('654 Jalan Hospital', 'Kuala Lumpur', 'WP Kuala Lumpur', '50586'),
('6, Jln Langkawi, Taman Danau Kota', 'Kuala Lumpur', 'WP Kuala Lumpur', '53300'),
('9, Jalan Danau Saujana 1, Taman Danau Kota', 'Kuala Lumpur', 'WP Kuala Lumpur', '53300'),
('19, Jalan Kampung Wira Jaya, Taman Melati', 'Kuala Lumpur', 'WP Kuala Lumpur', '53300'),
('PV15 Platinum Lake Condominium, Danau Kota', 'Kuala Lumpur', 'WP Kuala Lumpur', '53300'),
('2, Jalan Danau Saujana, Taman Danau Kota', 'Kuala Lumpur', 'WP Kuala Lumpur', '53300'),
('Jalan Genting Kelang, Taman Danau Kota', 'Kuala Lumpur', 'WP Kuala Lumpur', '53100');

-- Sample Patients (50 patients with realistic Malaysian names and data)
INSERT INTO patient (fullName, ICNumber, email, phoneNumber, addressId, registrationDate, bloodType, allergies, emergencyContact) VALUES
('Ahmad bin Abdullah', '011130-14-5347', 'ahmad.abdullah@email.com', '012-3456789', 'A000000001', '2021-03-15', 'A_POSITIVE', 'Penicillin,Peanuts', '019-8765432'),
('Siti binti Mohamed', '920220-06-5678', 'siti.mohamed@email.com', '012-3456790', 'A000000002', '2021-04-16', 'O_POSITIVE', 'Shellfish', '019-8765433'),
('Mohammed bin Ali', '940503-08-8901', 'mohammed.ali@email.com', '012-3456793', 'A000000006', '2021-05-18', 'B_POSITIVE', 'None', '019-8765434'),
('Fatimah binti Hassan', '950715-12-3456', 'fatimah.hassan@email.com', '012-3456794', 'A000000007', '2021-06-20', 'AB_POSITIVE', 'Dairy', '019-8765435'),
('Rahman bin Ismail', '930822-10-7890', 'rahman.ismail@email.com', '012-3456795', 'A000000008', '2021-07-22', 'A_NEGATIVE', 'Eggs', '019-8765436'),
('Nurul Ain binti Zainal', '960310-04-1234', 'nurul.ain@email.com', '012-3456796', 'A000000009', '2021-08-25', 'O_NEGATIVE', 'Nuts', '019-8765437'),
('Khairul bin Ahmad', '920511-16-5678', 'khairul.ahmad@email.com', '012-3456797', 'A000000010', '2021-09-28', 'B_NEGATIVE', 'Wheat', '019-8765438'),
('Aisyah binti Omar', '940625-08-9012', 'aisyah.omar@email.com', '012-3456798', 'A000000011', '2021-10-30', 'AB_NEGATIVE', 'Soy', '019-8765439'),
('Hafiz bin Yusof', '950918-12-3456', 'hafiz.yusof@email.com', '012-3456799', 'A000000006', '2021-11-02', 'A_POSITIVE', 'None', '019-8765440'),
('Nurul Huda binti Kamal', '930427-06-7890', 'nurul.huda@email.com', '012-3456800', 'A000000007', '2021-12-05', 'O_POSITIVE', 'Fish', '019-8765441'),
('Azman bin Hashim', '960112-14-1234', 'azman.hashim@email.com', '012-3456801', 'A000000008', '2022-01-08', 'B_POSITIVE', 'Shellfish', '019-8765442'),
('Salmah binti Ibrahim', '920703-02-5678', 'salmah.ibrahim@email.com', '012-3456802', 'A000000009', '2022-02-10', 'A_NEGATIVE', 'Peanuts', '019-8765443'),
('Rizal bin Mustafa', '940915-18-9012', 'rizal.mustafa@email.com', '012-3456803', 'A000000010', '2022-03-15', 'O_NEGATIVE', 'None', '019-8765444'),
('Noraini binti Sulaiman', '950628-10-3456', 'noraini.sulaiman@email.com', '012-3456804', 'A000000011', '2022-04-18', 'B_NEGATIVE', 'Dairy', '019-8765445'),
('Faizal bin Razak', '930321-04-7890', 'faizal.razak@email.com', '012-3456805', 'A000000006', '2022-05-20', 'AB_POSITIVE', 'Eggs', '019-8765446'),
('Maznah binti Aziz', '960811-16-1234', 'maznah.aziz@email.com', '012-3456806', 'A000000007', '2022-06-22', 'A_POSITIVE', 'Wheat', '019-8765447'),
('Shahrul bin Nordin', '920412-08-5678', 'shahrul.nordin@email.com', '012-3456807', 'A000000008', '2022-07-25', 'O_POSITIVE', 'Nuts', '019-8765448'),
('Rohana binti Mansor', '940625-12-9012', 'rohana.mansor@email.com', '012-3456808', 'A000000009', '2022-08-28', 'B_POSITIVE', 'Soy', '019-8765449'),
('Zulkifli bin Jaafar', '950318-06-3456', 'zulkifli.jaafar@email.com', '012-3456809', 'A000000010', '2022-09-30', 'AB_NEGATIVE', 'Fish', '019-8765450'),
('Norhafizah binti Zainudin', '930709-14-7890', 'norhafizah.zainudin@email.com', '012-3456810', 'A000000011', '2022-10-02', 'A_NEGATIVE', 'None', '019-8765451'),
('Amir bin Hamzah', '960204-02-1234', 'amir.hamzah@email.com', '012-3456811', 'A000000006', '2022-11-05', 'O_POSITIVE', 'Shellfish', '019-8765452'),
('Zarina binti Osman', '920516-18-5678', 'zarina.osman@email.com', '012-3456812', 'A000000007', '2022-12-08', 'B_NEGATIVE', 'Peanuts', '019-8765453'),
('Ridzuan bin Abdullah', '940827-10-9012', 'ridzuan.abdullah@email.com', '012-3456813', 'A000000008', '2023-01-10', 'AB_POSITIVE', 'Dairy', '019-8765454'),
('Norazlina binti Mohamed', '950610-04-3456', 'norazlina.mohamed@email.com', '012-3456814', 'A000000009', '2023-02-12', 'A_POSITIVE', 'Eggs', '019-8765455'),
('Shahrizal bin Ismail', '930323-16-7890', 'shahrizal.ismail@email.com', '012-3456815', 'A000000010', '2023-03-15', 'O_POSITIVE', 'Wheat', '019-8765456'),
('Nurul Ain binti Hassan', '960715-08-1234', 'nurul.ain.hassan@email.com', '012-3456816', 'A000000011', '2023-04-18', 'B_POSITIVE', 'Nuts', '019-8765457'),
('Khairul Anuar bin Ahmad', '920428-12-5678', 'khairul.anuar@email.com', '012-3456817', 'A000000006', '2023-05-20', 'AB_POSITIVE', 'Soy', '019-8765458'),
('Aisyah binti Omar', '940925-06-9012', 'aisyah.omar.2@email.com', '012-3456818', 'A000000007', '2023-06-22', 'A_POSITIVE', 'Shellfish', '019-8765459'),
('Hafiz bin Yusof', '950618-14-3456', 'hafiz.yusof.2@email.com', '012-3456819', 'A000000008', '2023-07-25', 'O_POSITIVE', 'Peanuts', '019-8765460'),
('Nurul Huda binti Kamal', '930521-02-7890', 'nurul.huda.2@email.com', '012-3456820', 'A000000009', '2023-08-28', 'B_POSITIVE', 'Dairy', '019-8765461'),
('Azman bin Hashim', '960812-18-1234', 'azman.hashim.2@email.com', '012-3456821', 'A000000010', '2023-09-30', 'AB_POSITIVE', 'Peanuts', '019-8765462'),
('Salmah binti Ibrahim', '920603-10-5678', 'salmah.ibrahim.2@email.com', '012-3456822', 'A000000011', '2023-10-02', 'A_POSITIVE', 'Dairy', '019-8765463'),
('Rizal bin Mustafa', '940916-04-9012', 'rizal.mustafa.2@email.com', '012-3456823', 'A000000006', '2023-11-05', 'O_POSITIVE', 'Eggs', '019-8765464'),
('Noraini binti Sulaiman', '950709-16-3456', 'noraini.sulaiman.2@email.com', '012-3456824', 'A000000007', '2023-12-08', 'B_POSITIVE', 'Wheat', '019-8765465'),
('Faizal bin Razak', '930420-08-7890', 'faizal.razak.2@email.com', '012-3456825', 'A000000008', '2024-01-10', 'AB_NEGATIVE', 'Nuts', '019-8765466'),
('Maznah binti Aziz', '960111-12-1234', 'maznah.aziz.2@email.com', '012-3456826', 'A000000009', '2024-02-12', 'A_NEGATIVE', 'Soy', '019-8765467'),
('Shahrul bin Nordin', '920702-06-5678', 'shahrul.nordin.2@email.com', '012-3456827', 'A000000010', '2024-03-15', 'O_NEGATIVE', 'Fish', '019-8765468'),
('Rohana binti Mansor', '940825-14-9012', 'rohana.mansor.2@email.com', '012-3456828', 'A000000011', '2024-04-18', 'B_NEGATIVE', 'None', '019-8765469'),
('Zulkifli bin Jaafar', '950618-02-3456', 'zulkifli.jaafar.2@email.com', '012-3456829', 'A000000006', '2024-05-20', 'AB_POSITIVE', 'Shellfish', '019-8765470'),
('Norhafizah binti Zainudin', '930511-18-7890', 'norhafizah.zainudin.2@email.com', '012-3456830', 'A000000007', '2024-06-22', 'A_POSITIVE', 'Peanuts', '019-8765471'),
('Amir bin Hamzah', '960304-10-1234', 'amir.hamzah.2@email.com', '012-3456831', 'A000000008', '2024-07-25', 'O_POSITIVE', 'Dairy', '019-8765472'),
('Zarina binti Osman', '920617-04-5678', 'zarina.osman.2@email.com', '012-3456832', 'A000000009', '2024-08-28', 'B_POSITIVE', 'Eggs', '019-8765473'),
('Ridzuan bin Abdullah', '940928-16-9012', 'ridzuan.abdullah.2@email.com', '012-3456833', 'A000000010', '2024-09-30', 'AB_NEGATIVE', 'Wheat', '019-8765474'),
('Norazlina binti Mohamed', '950721-08-3456', 'norazlina.mohamed.2@email.com', '012-3456834', 'A000000011', '2024-10-02', 'A_NEGATIVE', 'Nuts', '019-8765475'),
('Shahrizal bin Ismail', '930414-12-7890', 'shahrizal.ismail.2@email.com', '012-3456835', 'A000000006', '2024-11-05', 'O_NEGATIVE', 'Soy', '019-8765476'),
('Nurul Ain binti Hassan', '960107-06-1234', 'nurul.ain.hassan.2@email.com', '012-3456836', 'A000000007', '2024-12-08', 'B_NEGATIVE', 'Fish', '019-8765477'),
('Khairul Anuar bin Ahmad', '920530-14-5678', 'khairul.anuar.2@email.com', '012-3456837', 'A000000008', '2025-01-10', 'AB_POSITIVE', 'None', '019-8765478'),
('Aisyah binti Omar', '940823-02-9012', 'aisyah.omar.3@email.com', '012-3456838', 'A000000009', '2025-02-12', 'A_POSITIVE', 'Shellfish', '019-8765479'),
('Hafiz bin Yusof', '950516-18-3456', 'hafiz.yusof.3@email.com', '012-3456839', 'A000000010', '2025-03-15', 'O_POSITIVE', 'Peanuts', '019-8765480'),
('Nurul Huda binti Kamal', '930709-10-7890', 'nurul.huda.3@email.com', '012-3456840', 'A000000011', '2025-04-18', 'B_POSITIVE', 'Dairy', '019-8765481');

-- Sample Doctors (2 doctors with expanded schedules)
INSERT INTO doctor (fullName, ICNumber, email, phoneNumber, addressId, registrationDate, medicalSpecialty, licenseNumber, expYears) VALUES
('Dr. Lim Wei Chen', '850322-04-6789', 'dr.lim@clinic.com', '012-3456791', 'A000000003', '2021-01-10', 'General Medicine', 'MD001234', 8),
('Dr. Sarah Johnson', '880404-04-7890', 'dr.sarah@clinic.com', '012-3456792', 'A000000004', '2021-02-12', 'Cardiology', 'MD001235', 12);

-- Sample Schedules (expanded for both doctors)
INSERT INTO schedule (doctorId, dayOfWeek, fromTime, toTime) VALUES
('D000000001', 'MONDAY', '09:00:00', '17:00:00'),
('D000000001', 'TUESDAY', '09:00:00', '17:00:00'),
('D000000001', 'WEDNESDAY', '09:00:00', '17:00:00'),
('D000000001', 'THURSDAY', '09:00:00', '17:00:00'),
('D000000001', 'FRIDAY', '09:00:00', '17:00:00'),
('D000000002', 'MONDAY', '08:00:00', '16:00:00'),
('D000000002', 'TUESDAY', '08:00:00', '16:00:00'),
('D000000002', 'WEDNESDAY', '08:00:00', '16:00:00'),
('D000000002', 'THURSDAY', '08:00:00', '16:00:00'),
('D000000002', 'FRIDAY', '08:00:00', '16:00:00');

-- Sample Medicines (20 medicines with realistic data for small campus clinic)
INSERT INTO `medicine` (`medicineName`, `genericName`, `manufacturer`, `description`, `dosageForm`, `strength`, `quantityInStock`, `minimumStockLevel`, `unitPrice`, `expiryDate`, `storageLocation`, `requiresPrescription`, `status`, `createdDate`) VALUES
('Paracetamol', 'Acetaminophen', 'PharmaCorp', 'Pain reliever and fever reducer', 'Tablet', '500mg', 100, 20, 0.50, '2026-12-31', 'Shelf A1', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Amoxicillin', 'Amoxicillin', 'MediPharm', 'Antibiotic for bacterial infections', 'Capsule', '250mg', 50, 10, 2.00, '2026-06-30', 'Shelf B2', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Ibuprofen', 'Ibuprofen', 'HealthCare Ltd', 'Anti-inflammatory pain reliever', 'Tablet', '400mg', 75, 15, 0.75, '2026-09-30', 'Shelf A2', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Omeprazole', 'Omeprazole', 'GastroMed', 'Proton pump inhibitor for acid reflux', 'Capsule', '20mg', 30, 5, 3.50, '2026-03-31', 'Shelf C1', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Cetirizine', 'Cetirizine', 'AllergyCare', 'Antihistamine for allergies', 'Tablet', '10mg', 60, 10, 1.20, '2026-08-31', 'Shelf A3', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Metformin', 'Metformin', 'DiabeCare', 'Oral diabetes medication', 'Tablet', '500mg', 40, 5, 2.50, '2026-05-31', 'Shelf B3', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Lisinopril', 'Lisinopril', 'CardioMed', 'ACE inhibitor for hypertension', 'Tablet', '10mg', 35, 5, 4.00, '2026-07-31', 'Shelf C2', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Simvastatin', 'Simvastatin', 'CholesterolCare', 'Statin for high cholesterol', 'Tablet', '20mg', 30, 5, 3.80, '2026-04-30', 'Shelf C3', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Aspirin', 'Acetylsalicylic Acid', 'HeartCare', 'Blood thinner and pain reliever', 'Tablet', '100mg', 80, 15, 0.30, '2026-10-31', 'Shelf A4', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Folic Acid', 'Folic Acid', 'VitaminCorp', 'Vitamin B9 supplement', 'Tablet', '5mg', 30, 10, 0.80, '2026-11-30', 'Shelf B4', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Calcium Carbonate', 'Calcium Carbonate', 'BoneCare', 'Calcium supplement for bones', 'Tablet', '500mg', 60, 10, 1.50, '2026-12-31', 'Shelf A5', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Vitamin D3', 'Cholecalciferol', 'VitaminCorp', 'Vitamin D supplement', 'Capsule', '1000IU', 50, 10, 2.20, '2026-06-30', 'Shelf B5', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Iron Sulfate', 'Ferrous Sulfate', 'AnemiaCare', 'Iron supplement for anemia', 'Tablet', '325mg', 40, 5, 1.80, '2026-08-31', 'Shelf C4', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Loratadine', 'Loratadine', 'AllergyCare', 'Non-drowsy antihistamine', 'Tablet', '10mg', 55, 10, 1.50, '2026-09-30', 'Shelf A6', 0, 'AVAILABLE', '2021-01-01 08:00:00'),
('Ranitidine', 'Ranitidine', 'GastroMed', 'H2 blocker for acid reflux', 'Tablet', '150mg', 30, 5, 2.80, '2026-05-31', 'Shelf C5', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Diclofenac', 'Diclofenac', 'PainCare', 'NSAID for pain and inflammation', 'Tablet', '50mg', 25, 5, 3.20, '2026-07-31', 'Shelf B6', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Ciprofloxacin', 'Ciprofloxacin', 'MediPharm', 'Broad-spectrum antibiotic', 'Tablet', '500mg', 20, 3, 4.50, '2026-04-30', 'Shelf C6', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Amlodipine', 'Amlodipine', 'CardioMed', 'Calcium channel blocker for hypertension', 'Tablet', '5mg', 35, 5, 3.90, '2026-08-31', 'Shelf D1', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Atorvastatin', 'Atorvastatin', 'CholesterolCare', 'Statin for high cholesterol', 'Tablet', '10mg', 30, 5, 4.20, '2026-06-30', 'Shelf D2', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Sertraline', 'Sertraline', 'MentalCare', 'SSRI antidepressant', 'Tablet', '50mg', 20, 3, 5.00, '2026-09-30', 'Shelf D3', 1, 'AVAILABLE', '2021-01-01 08:00:00'),
('Melatonin', 'Melatonin', 'SleepCare', 'Natural sleep hormone supplement', 'Tablet', '3mg', 80, 15, 1.80, '2026-12-31', 'Shelf D4', 0, 'AVAILABLE', '2021-01-01 08:00:00');

-- Sample Consultations (50 consultations spanning 2021-2025)
INSERT INTO consultation (patientId, doctorId, consultationDate, symptoms, diagnosis, treatment, notes, nextVisitDate, consultationFee, status) VALUES
('P000000001', 'D000000001', '2021-03-20 10:00:00', 'Fever and headache', 'Common cold with fever', 'Rest, fluids, and paracetamol for fever', 'Patient presents with fever and headache. No serious complications detected.', '2021-03-27', 50.00, 'COMPLETED'),
('P000000002', 'D000000002', '2021-04-21 14:00:00', 'Chest pain and shortness of breath', 'Hypertension', 'Lifestyle changes and medication monitoring', 'Patient shows signs of hypertension. ECG and blood pressure monitoring required.', '2021-05-21', 80.00, 'COMPLETED'),
('P000000003', 'D000000001', '2021-05-22 11:00:00', 'Stomach pain and nausea', 'Gastritis', 'Diet modification and omeprazole', 'Patient has gastritis symptoms. Dietary changes and medication prescribed.', '2021-06-22', 50.00, 'COMPLETED'),
('P000000004', 'D000000002', '2021-06-25 09:00:00', 'High blood pressure', 'Essential hypertension', 'Blood pressure medication and diet', 'Essential hypertension diagnosed. Medication and low-sodium diet recommended.', '2021-07-25', 80.00, 'COMPLETED'),
('P000000005', 'D000000001', '2021-07-28 15:00:00', 'Cough and sore throat', 'Upper respiratory infection', 'Antibiotics and rest', 'Upper respiratory infection confirmed. Antibiotics prescribed for 7 days.', '2021-08-28', 50.00, 'COMPLETED'),
('P000000006', 'D000000002', '2021-08-30 13:00:00', 'Irregular heartbeat', 'Atrial fibrillation', 'Heart rhythm medication', 'Atrial fibrillation detected. Beta-blocker medication prescribed.', '2021-09-30', 80.00, 'COMPLETED'),
('P000000007', 'D000000001', '2021-09-02 10:30:00', 'Back pain', 'Lower back strain', 'Pain medication and physiotherapy', 'Lower back strain from heavy lifting. Pain medication and physiotherapy recommended.', '2021-10-02', 50.00, 'COMPLETED'),
('P000000008', 'D000000002', '2021-10-05 16:00:00', 'Swelling in legs', 'Congestive heart failure', 'Diuretics and heart medication', 'Congestive heart failure confirmed. Diuretics and heart medication prescribed.', '2021-11-05', 80.00, 'COMPLETED'),
('P000000009', 'D000000001', '2021-11-08 11:30:00', 'Skin rash', 'Contact dermatitis', 'Topical steroids and avoidance', 'Contact dermatitis from chemical exposure. Topical steroids and avoidance of irritants advised.', '2021-12-08', 50.00, 'COMPLETED'),
('P000000010', 'D000000002', '2021-12-10 14:30:00', 'Chest discomfort', 'Angina pectoris', 'Nitroglycerin and lifestyle changes', 'Stable angina diagnosed. Nitroglycerin for acute attacks and lifestyle changes recommended.', '2022-01-10', 80.00, 'COMPLETED'),
('P000000011', 'D000000001', '2022-01-15 09:00:00', 'Fever and body aches', 'Influenza', 'Antiviral medication and rest', 'Influenza A confirmed. Antiviral medication prescribed for 5 days.', '2022-02-15', 50.00, 'COMPLETED'),
('P000000012', 'D000000002', '2022-02-18 15:30:00', 'Shortness of breath', 'Dyspnea', 'Bronchodilators and oxygen therapy', 'Dyspnea due to bronchospasm. Bronchodilator inhaler prescribed.', '2022-03-18', 80.00, 'COMPLETED'),
('P000000013', 'D000000001', '2022-03-22 12:00:00', 'Stomach upset', 'Gastroenteritis', 'Anti-nausea medication and hydration', 'Viral gastroenteritis diagnosed. Anti-nausea medication and hydration therapy.', '2022-04-22', 50.00, 'COMPLETED'),
('P000000014', 'D000000002', '2022-04-25 10:00:00', 'Chest tightness', 'Coronary artery disease', 'Statin therapy and aspirin', 'Coronary artery disease confirmed. Statin and aspirin therapy initiated.', '2022-05-25', 80.00, 'COMPLETED'),
('P000000015', 'D000000001', '2022-05-28 13:30:00', 'Headache and dizziness', 'Tension headache', 'Pain relievers and stress management', 'Tension headache due to stress. Pain relievers and stress management techniques.', '2022-06-28', 50.00, 'COMPLETED'),
('P000000016', 'D000000002', '2022-06-30 16:00:00', 'Palpitations', 'Supraventricular tachycardia', 'Anti-arrhythmic medication', 'Supraventricular tachycardia confirmed. Calcium channel blocker prescribed.', '2022-07-30', 80.00, 'COMPLETED'),
('P000000017', 'D000000001', '2022-07-05 11:00:00', 'Joint pain', 'Osteoarthritis', 'Anti-inflammatory medication and exercise', 'Osteoarthritis of knee joints. Anti-inflammatory medication and exercise program.', '2022-08-05', 50.00, 'COMPLETED'),
('P000000018', 'D000000002', '2022-08-08 14:00:00', 'Fatigue and weakness', 'Cardiomyopathy', 'Heart failure medication and monitoring', 'Dilated cardiomyopathy diagnosed. Beta-blocker therapy and close monitoring.', '2022-09-08', 80.00, 'COMPLETED'),
('P000000019', 'D000000001', '2022-09-12 09:30:00', 'Allergic reaction', 'Allergic rhinitis', 'Antihistamines and nasal sprays', 'Seasonal allergic rhinitis. Antihistamine and nasal steroid spray prescribed.', '2022-10-12', 50.00, 'COMPLETED'),
('P000000020', 'D000000002', '2022-10-15 15:00:00', 'Chest pain on exertion', 'Stable angina', 'Beta-blockers and nitrates', 'Stable angina confirmed. Beta-blocker and nitrate therapy prescribed.', '2022-11-15', 80.00, 'COMPLETED'),
('P000000021', 'D000000001', '2022-11-18 12:30:00', 'Fever and chills', 'Viral fever', 'Antipyretics and rest', 'Viral fever diagnosed. Symptomatic treatment with antipyretics.', '2022-12-18', 50.00, 'COMPLETED'),
('P000000022', 'D000000002', '2022-12-20 10:30:00', 'Irregular pulse', 'Sinus arrhythmia', 'Monitoring and lifestyle changes', 'Sinus arrhythmia detected. Regular monitoring and lifestyle modifications advised.', '2023-01-20', 80.00, 'COMPLETED'),
('P000000023', 'D000000001', '2023-01-25 13:00:00', 'Stomach cramps', 'Irritable bowel syndrome', 'Diet modification and antispasmodics', 'IBS symptoms confirmed. Dietary changes and antispasmodic medication prescribed.', '2023-02-25', 50.00, 'COMPLETED'),
('P000000024', 'D000000002', '2023-02-28 16:30:00', 'Chest heaviness', 'Anxiety-related chest pain', 'Anti-anxiety medication and counseling', 'Anxiety-related chest pain diagnosed. Anti-anxiety medication and counseling recommended.', '2023-03-28', 80.00, 'COMPLETED'),
('P000000025', 'D000000001', '2023-03-05 11:30:00', 'Sore throat', 'Pharyngitis', 'Antibiotics and throat lozenges', 'Bacterial pharyngitis confirmed. Antibiotics and symptomatic treatment prescribed.', '2023-04-05', 50.00, 'COMPLETED'),
('P000000026', 'D000000002', '2023-04-08 14:30:00', 'Shortness of breath at rest', 'Asthma exacerbation', 'Bronchodilators and inhaled steroids', 'Asthma exacerbation confirmed. Bronchodilators and inhaled steroids prescribed.', '2023-05-08', 80.00, 'COMPLETED'),
('P000000027', 'D000000001', '2023-05-12 09:00:00', 'Body aches', 'Viral myalgia', 'Pain relievers and rest', 'Viral myalgia diagnosed. Pain relievers and adequate rest recommended.', '2023-06-12', 50.00, 'COMPLETED'),
('P000000028', 'D000000002', '2023-06-15 15:30:00', 'Chest pressure', 'Non-cardiac chest pain', 'Pain management and follow-up', 'Non-cardiac chest pain diagnosed. Pain management and close follow-up required.', '2023-07-15', 80.00, 'COMPLETED'),
('P000000029', 'D000000001', '2023-07-18 12:00:00', 'Nausea and vomiting', 'Gastritis flare-up', 'Proton pump inhibitors and diet', 'Gastritis flare-up confirmed. Proton pump inhibitors and dietary modifications.', '2023-08-18', 50.00, 'COMPLETED'),
('P000000030', 'D000000002', '2023-08-22 10:00:00', 'Heart racing', 'Sinus tachycardia', 'Beta-blockers and stress management', 'Sinus tachycardia diagnosed. Beta-blockers and stress management techniques.', '2023-09-22', 80.00, 'COMPLETED'),
('P000000031', 'D000000001', '2023-09-25 13:30:00', 'Fever and cough', 'Upper respiratory infection', 'Cough suppressants and rest', 'Upper respiratory infection confirmed. Cough suppressants and adequate rest.', '2023-10-25', 50.00, 'COMPLETED'),
('P000000032', 'D000000002', '2023-10-28 16:00:00', 'Chest discomfort', 'Costochondritis', 'Anti-inflammatory medication and rest', 'Costochondritis diagnosed. Anti-inflammatory medication and rest recommended.', '2023-11-28', 80.00, 'COMPLETED'),
('P000000033', 'D000000001', '2023-11-30 11:00:00', 'Stomach pain', 'Functional dyspepsia', 'Prokinetics and diet modification', 'Functional dyspepsia diagnosed. Prokinetics and dietary modifications prescribed.', '2023-12-30', 50.00, 'COMPLETED'),
('P000000034', 'D000000002', '2023-12-05 14:00:00', 'Irregular heartbeat', 'Premature ventricular contractions', 'Monitoring and lifestyle changes', 'PVCs detected. Regular monitoring and lifestyle modifications advised.', '2024-01-05', 80.00, 'COMPLETED'),
('P000000035', 'D000000001', '2024-01-08 09:30:00', 'Headache', 'Migraine', 'Triptans and pain management', 'Migraine diagnosed. Triptans and comprehensive pain management plan.', '2024-02-08', 50.00, 'COMPLETED'),
('P000000036', 'D000000002', '2024-02-12 15:00:00', 'Chest tightness', 'Pericarditis', 'Anti-inflammatory medication and monitoring', 'Pericarditis confirmed. Anti-inflammatory medication and close monitoring required.', '2024-03-12', 80.00, 'COMPLETED'),
('P000000037', 'D000000001', '2024-03-15 12:30:00', 'Fever', 'Viral fever', 'Antipyretics and supportive care', 'Viral fever diagnosed. Symptomatic treatment with antipyretics and supportive care.', '2024-04-15', 50.00, 'COMPLETED'),
('P000000038', 'D000000002', '2024-04-18 10:30:00', 'Shortness of breath', 'Dyspnea on exertion', 'Cardiac evaluation and monitoring', 'Dyspnea on exertion confirmed. Cardiac evaluation and regular monitoring required.', '2024-05-18', 80.00, 'COMPLETED'),
('P000000039', 'D000000001', '2024-05-22 13:00:00', 'Stomach upset', 'Gastritis', 'Proton pump inhibitors and diet', 'Gastritis confirmed. Proton pump inhibitors and dietary modifications prescribed.', '2024-06-22', 50.00, 'COMPLETED'),
('P000000040', 'D000000002', '2024-06-25 16:30:00', 'Chest pain', 'Stable angina', 'Nitrates and beta-blockers', 'Stable angina confirmed. Nitrates and beta-blocker therapy prescribed.', '2024-07-25', 80.00, 'COMPLETED'),
('P000000041', 'D000000001', '2024-07-28 11:30:00', 'Body aches', 'Fibromyalgia', 'Pain management and physical therapy', 'Fibromyalgia diagnosed. Comprehensive pain management and physical therapy plan.', '2024-08-28', 50.00, 'COMPLETED'),
('P000000042', 'D000000002', '2024-08-30 14:00:00', 'Palpitations', 'Atrial flutter', 'Anti-arrhythmic medication', 'Atrial flutter detected. Anti-arrhythmic medication and monitoring prescribed.', '2024-09-30', 80.00, 'COMPLETED'),
('P000000043', 'D000000001', '2024-09-02 09:00:00', 'Sore throat', 'Viral pharyngitis', 'Symptomatic treatment and rest', 'Viral pharyngitis confirmed. Symptomatic treatment and adequate rest recommended.', '2024-10-02', 50.00, 'COMPLETED'),
('P000000044', 'D000000002', '2024-10-05 15:30:00', 'Chest heaviness', 'Non-cardiac chest pain', 'Pain management and follow-up', 'Non-cardiac chest pain diagnosed. Pain management and regular follow-up required.', '2024-11-05', 80.00, 'COMPLETED'),
('P000000045', 'D000000001', '2024-11-08 12:00:00', 'Fever and chills', 'Viral infection', 'Antipyretics and supportive care', 'Viral infection confirmed. Symptomatic treatment with antipyretics and supportive care.', '2024-12-08', 50.00, 'COMPLETED'),
('P000000046', 'D000000002', '2024-12-10 10:00:00', 'Irregular pulse', 'Sinus arrhythmia', 'Monitoring and lifestyle changes', 'Sinus arrhythmia detected. Regular monitoring and lifestyle modifications advised.', '2025-01-10', 80.00, 'COMPLETED'),
('P000000047', 'D000000001', '2025-01-15 13:30:00', 'Stomach cramps', 'Functional abdominal pain', 'Pain management and diet modification', 'Functional abdominal pain diagnosed. Pain management and dietary modifications prescribed.', '2025-02-15', 50.00, 'COMPLETED'),
('P000000048', 'D000000002', '2025-02-18 16:00:00', 'Chest pressure', 'Anxiety-related symptoms', 'Anti-anxiety medication and counseling', 'Anxiety-related chest pressure diagnosed. Anti-anxiety medication and counseling recommended.', '2025-03-18', 80.00, 'COMPLETED'),
('P000000049', 'D000000001', '2025-03-22 11:30:00', 'Headache and dizziness', 'Tension headache', 'Pain relievers and stress management', 'Tension headache due to stress. Pain relievers and stress management techniques.', '2025-04-22', 50.00, 'COMPLETED'),
('P000000050', 'D000000002', '2025-04-25 14:30:00', 'Shortness of breath', 'Dyspnea on exertion', 'Cardiac evaluation and monitoring', 'Dyspnea on exertion confirmed. Cardiac evaluation and regular monitoring required.', '2025-05-25', 80.00, 'COMPLETED');

-- Sample Medical Treatments (20 treatments)
INSERT INTO medical_treatment (patientId, doctorId, consultationId, diagnosis, treatmentPlan, prescribedMedications, treatmentNotes, treatmentDate, followUpDate, treatmentCost, status) VALUES
('P000000001', 'D000000001', 'C000000001', 'Common cold with fever', 'Rest, fluids, and paracetamol for fever', 'Paracetamol 500mg', 'Patient shows symptoms of common cold with fever. Advised rest and increased fluid intake.', '2021-03-20 10:30:00', '2021-03-27', 25.00, 'COMPLETED'),
('P000000002', 'D000000002', 'C000000002', 'Hypertension', 'Lifestyle changes and medication monitoring', 'Amlodipine 10mg', 'Patient diagnosed with stage 1 hypertension. Lifestyle modifications and medication prescribed.', '2021-04-21 14:30:00', '2021-05-21', 45.00, 'COMPLETED'),
('P000000003', 'D000000001', 'C000000003', 'Gastritis', 'Diet modification and omeprazole', 'Omeprazole 20mg', 'Patient has gastritis due to stress and poor diet. Medication and dietary changes recommended.', '2021-05-22 11:30:00', '2021-06-22', 35.00, 'COMPLETED'),
('P000000004', 'D000000002', 'C000000004', 'Essential hypertension', 'Blood pressure medication and diet', 'Lisinopril 10mg', 'Essential hypertension diagnosed. Medication and low-sodium diet prescribed.', '2021-06-25 09:30:00', '2021-07-25', 50.00, 'COMPLETED'),
('P000000005', 'D000000001', 'C000000005', 'Upper respiratory infection', 'Antibiotics and rest', 'Amoxicillin 250mg', 'Upper respiratory infection confirmed. Antibiotics prescribed for 7 days.', '2021-07-28 15:30:00', '2021-08-28', 30.00, 'COMPLETED'),
('P000000006', 'D000000002', 'C000000006', 'Atrial fibrillation', 'Heart rhythm medication', 'Metoprolol 25mg', 'Atrial fibrillation detected. Beta-blocker medication prescribed for rhythm control.', '2021-08-30 13:30:00', '2021-09-30', 60.00, 'COMPLETED'),
('P000000007', 'D000000001', 'C000000007', 'Lower back strain', 'Pain medication and physiotherapy', 'Ibuprofen 400mg', 'Lower back strain from heavy lifting. Pain medication and physiotherapy recommended.', '2021-09-02 10:30:00', '2021-10-02', 40.00, 'COMPLETED'),
('P000000008', 'D000000002', 'C000000008', 'Congestive heart failure', 'Diuretics and heart medication', 'Furosemide 40mg', 'Congestive heart failure confirmed. Diuretics and heart medication prescribed.', '2021-10-05 16:30:00', '2021-11-05', 70.00, 'COMPLETED'),
('P000000009', 'D000000001', 'C000000009', 'Contact dermatitis', 'Topical steroids and avoidance', 'Hydrocortisone 1% cream', 'Contact dermatitis from chemical exposure. Topical steroids and avoidance of irritants advised.', '2021-11-08 11:30:00', '2021-12-08', 25.00, 'COMPLETED'),
('P000000010', 'D000000002', 'C000000010', 'Angina pectoris', 'Nitroglycerin and lifestyle changes', 'Nitroglycerin 0.4mg', 'Stable angina diagnosed. Nitroglycerin for acute attacks and lifestyle changes recommended.', '2021-12-10 14:30:00', '2022-01-10', 55.00, 'COMPLETED'),
('P000000011', 'D000000001', 'C000000011', 'Influenza', 'Antiviral medication and rest', 'Oseltamivir 75mg', 'Influenza A confirmed. Antiviral medication prescribed for 5 days.', '2022-01-15 09:30:00', '2022-02-15', 35.00, 'COMPLETED'),
('P000000012', 'D000000002', 'C000000012', 'Dyspnea', 'Bronchodilators and oxygen therapy', 'Salbutamol inhaler', 'Dyspnea due to bronchospasm. Bronchodilator inhaler prescribed.', '2022-02-18 15:30:00', '2022-03-18', 65.00, 'COMPLETED'),
('P000000013', 'D000000001', 'C000000013', 'Gastroenteritis', 'Anti-nausea medication and hydration', 'Ondansetron 4mg', 'Viral gastroenteritis diagnosed. Anti-nausea medication and hydration therapy.', '2022-03-22 12:30:00', '2022-04-22', 30.00, 'COMPLETED'),
('P000000014', 'D000000002', 'C000000014', 'Coronary artery disease', 'Statin therapy and aspirin', 'Atorvastatin 20mg, Aspirin 100mg', 'Coronary artery disease confirmed. Statin and aspirin therapy initiated.', '2022-04-25 10:30:00', '2022-05-25', 75.00, 'COMPLETED'),
('P000000015', 'D000000001', 'C000000015', 'Tension headache', 'Pain relievers and stress management', 'Paracetamol 500mg', 'Tension headache due to stress. Pain relievers and stress management techniques.', '2022-05-28 13:30:00', '2022-06-28', 20.00, 'COMPLETED'),
('P000000016', 'D000000002', 'C000000016', 'Supraventricular tachycardia', 'Anti-arrhythmic medication', 'Verapamil 80mg', 'Supraventricular tachycardia confirmed. Calcium channel blocker prescribed.', '2022-06-30 16:30:00', '2022-07-30', 60.00, 'COMPLETED'),
('P000000017', 'D000000001', 'C000000017', 'Osteoarthritis', 'Anti-inflammatory medication and exercise', 'Celecoxib 200mg', 'Osteoarthritis of knee joints. Anti-inflammatory medication and exercise program.', '2022-07-05 11:30:00', '2022-08-05', 45.00, 'COMPLETED'),
('P000000018', 'D000000002', 'C000000018', 'Cardiomyopathy', 'Heart failure medication and monitoring', 'Carvedilol 6.25mg', 'Dilated cardiomyopathy diagnosed. Beta-blocker therapy and close monitoring.', '2022-08-08 14:30:00', '2022-09-08', 80.00, 'COMPLETED'),
('P000000019', 'D000000001', 'C000000019', 'Allergic rhinitis', 'Antihistamines and nasal sprays', 'Cetirizine 10mg, Fluticasone nasal spray', 'Seasonal allergic rhinitis. Antihistamine and nasal steroid spray prescribed.', '2022-09-12 09:30:00', '2022-10-12', 25.00, 'COMPLETED'),
('P000000020', 'D000000002', 'C000000020', 'Stable angina', 'Beta-blockers and nitrates', 'Metoprolol 50mg, Isosorbide mononitrate 20mg', 'Stable angina confirmed. Beta-blocker and nitrate therapy prescribed.', '2022-10-15 15:30:00', '2022-11-15', 50.00, 'COMPLETED');

-- Sample Prescriptions (50 prescriptions)
INSERT INTO prescription (patientId, doctorId, consultationId, prescriptionDate, instructions, expiryDate, status) VALUES
('P000000001', 'D000000001', 'C000000001', '2021-03-20 10:30:00', 'Take 2 tablets every 6 hours for fever', '2021-04-20', 'DISPENSED'),
('P000000002', 'D000000002', 'C000000002', '2021-04-21 14:30:00', 'Take 1 tablet daily with food', '2021-05-21', 'DISPENSED'),
('P000000003', 'D000000001', 'C000000003', '2021-05-22 11:30:00', 'Take 1 capsule daily before breakfast', '2021-06-22', 'DISPENSED'),
('P000000004', 'D000000002', 'C000000004', '2021-06-25 09:30:00', 'Take 1 tablet twice daily', '2021-07-25', 'DISPENSED'),
('P000000005', 'D000000001', 'C000000005', '2021-07-28 15:30:00', 'Take 1 tablet every 8 hours', '2021-08-28', 'DISPENSED'),
('P000000006', 'D000000002', 'C000000006', '2021-08-30 13:30:00', 'Take 1 tablet daily', '2021-09-30', 'DISPENSED'),
('P000000007', 'D000000001', 'C000000007', '2021-09-02 10:30:00', 'Take 1 tablet every 12 hours', '2021-10-02', 'DISPENSED'),
('P000000008', 'D000000002', 'C000000008', '2021-10-05 16:30:00', 'Take 1 tablet twice daily', '2021-11-05', 'DISPENSED'),
('P000000009', 'D000000001', 'C000000009', '2021-11-08 11:30:00', 'Apply cream twice daily', '2021-12-08', 'DISPENSED'),
('P000000010', 'D000000002', 'C000000010', '2021-12-10 14:30:00', 'Take 1 tablet as needed', '2022-01-10', 'DISPENSED'),
('P000000011', 'D000000001', 'C000000011', '2022-01-15 09:30:00', 'Take 1 tablet every 6 hours', '2022-02-15', 'DISPENSED'),
('P000000012', 'D000000002', 'C000000012', '2022-02-18 15:30:00', 'Take 1 tablet daily', '2022-03-18', 'DISPENSED'),
('P000000013', 'D000000001', 'C000000013', '2022-03-22 12:30:00', 'Take 1 tablet every 8 hours', '2022-04-22', 'DISPENSED'),
('P000000014', 'D000000002', 'C000000014', '2022-04-25 10:30:00', 'Take 1 tablet daily', '2022-05-25', 'DISPENSED'),
('P000000015', 'D000000001', 'C000000015', '2022-05-28 13:30:00', 'Take 1 tablet every 6 hours', '2022-06-28', 'DISPENSED'),
('P000000016', 'D000000002', 'C000000016', '2022-06-30 16:30:00', 'Take 1 tablet daily', '2022-07-30', 'DISPENSED'),
('P000000017', 'D000000001', 'C000000017', '2022-07-05 11:30:00', 'Take 1 tablet every 12 hours', '2022-08-05', 'DISPENSED'),
('P000000018', 'D000000002', 'C000000018', '2022-08-08 14:30:00', 'Take 1 tablet twice daily', '2022-09-08', 'DISPENSED'),
('P000000019', 'D000000001', 'C000000019', '2022-09-12 09:30:00', 'Take 1 tablet daily', '2022-10-12', 'DISPENSED'),
('P000000020', 'D000000002', 'C000000020', '2022-10-15 15:30:00', 'Take 1 tablet as needed', '2022-11-15', 'DISPENSED'),
('P000000021', 'D000000001', 'C000000021', '2022-11-18 12:30:00', 'Take 1 tablet every 6 hours', '2022-12-18', 'DISPENSED'),
('P000000022', 'D000000002', 'C000000022', '2022-12-20 10:30:00', 'Take 1 tablet daily', '2023-01-20', 'DISPENSED'),
('P000000023', 'D000000001', 'C000000023', '2023-01-25 13:30:00', 'Take 1 tablet every 8 hours', '2023-02-25', 'DISPENSED'),
('P000000024', 'D000000002', 'C000000024', '2023-02-28 16:30:00', 'Take 1 tablet daily', '2023-03-28', 'DISPENSED'),
('P000000025', 'D000000001', 'C000000025', '2023-03-05 11:30:00', 'Take 1 tablet every 6 hours', '2023-04-05', 'DISPENSED'),
('P000000026', 'D000000002', 'C000000026', '2023-04-08 14:30:00', 'Take 1 tablet daily', '2023-05-08', 'DISPENSED'),
('P000000027', 'D000000001', 'C000000027', '2023-05-12 09:30:00', 'Take 1 tablet every 12 hours', '2023-06-12', 'DISPENSED'),
('P000000028', 'D000000002', 'C000000028', '2023-06-15 15:30:00', 'Take 1 tablet as needed', '2023-07-15', 'DISPENSED'),
('P000000029', 'D000000001', 'C000000029', '2023-07-18 12:30:00', 'Take 1 tablet every 8 hours', '2023-08-18', 'DISPENSED'),
('P000000030', 'D000000002', 'C000000030', '2023-08-22 10:30:00', 'Take 1 tablet daily', '2023-09-22', 'DISPENSED'),
('P000000031', 'D000000001', 'C000000031', '2023-09-25 13:30:00', 'Take 1 tablet every 6 hours', '2023-10-25', 'DISPENSED'),
('P000000032', 'D000000002', 'C000000032', '2023-10-28 16:30:00', 'Take 1 tablet daily', '2023-11-28', 'DISPENSED'),
('P000000033', 'D000000001', 'C000000033', '2023-11-30 11:30:00', 'Take 1 tablet every 12 hours', '2023-12-30', 'DISPENSED'),
('P000000034', 'D000000002', 'C000000034', '2023-12-05 14:30:00', 'Take 1 tablet twice daily', '2024-01-05', 'DISPENSED'),
('P000000035', 'D000000001', 'C000000035', '2024-01-08 09:30:00', 'Take 1 tablet every 6 hours', '2024-02-08', 'DISPENSED'),
('P000000036', 'D000000002', 'C000000036', '2024-02-12 15:30:00', 'Take 1 tablet daily', '2024-03-12', 'DISPENSED'),
('P000000037', 'D000000001', 'C000000037', '2024-03-15 12:30:00', 'Take 1 tablet every 8 hours', '2024-04-15', 'DISPENSED'),
('P000000038', 'D000000002', 'C000000038', '2024-04-18 10:30:00', 'Take 1 tablet daily', '2024-05-18', 'DISPENSED'),
('P000000039', 'D000000001', 'C000000039', '2024-05-22 13:30:00', 'Take 1 tablet every 12 hours', '2024-06-22', 'DISPENSED'),
('P000000040', 'D000000002', 'C000000040', '2024-06-25 16:30:00', 'Take 1 tablet as needed', '2024-07-25', 'DISPENSED'),
('P000000041', 'D000000001', 'C000000041', '2024-07-28 11:30:00', 'Take 1 tablet every 6 hours', '2024-08-28', 'DISPENSED'),
('P000000042', 'D000000002', 'C000000042', '2024-08-30 14:30:00', 'Take 1 tablet daily', '2024-09-30', 'DISPENSED'),
('P000000043', 'D000000001', 'C000000043', '2024-09-02 09:30:00', 'Take 1 tablet every 8 hours', '2024-10-02', 'DISPENSED'),
('P000000044', 'D000000002', 'C000000044', '2024-10-05 15:30:00', 'Take 1 tablet daily', '2024-11-05', 'DISPENSED'),
('P000000045', 'D000000001', 'C000000045', '2024-11-08 12:30:00', 'Take 1 tablet every 12 hours', '2024-12-08', 'DISPENSED'),
('P000000046', 'D000000002', 'C000000046', '2024-12-10 10:30:00', 'Take 1 tablet twice daily', '2025-01-10', 'DISPENSED'),
('P000000047', 'D000000001', 'C000000047', '2025-01-15 13:30:00', 'Take 1 tablet every 6 hours', '2025-02-15', 'DISPENSED'),
('P000000048', 'D000000002', 'C000000048', '2025-02-18 16:30:00', 'Take 1 tablet daily', '2025-03-18', 'DISPENSED'),
('P000000049', 'D000000001', 'C000000049', '2025-03-22 11:30:00', 'Take 1 tablet every 8 hours', '2025-04-22', 'DISPENSED'),
('P000000050', 'D000000002', 'C000000050', '2025-04-25 14:30:00', 'Take 1 tablet daily', '2025-05-25', 'DISPENSED');

-- Sample Prescribed Medicines (comprehensive data for all prescriptions)
INSERT INTO prescribed_medicine (prescriptionId, medicineId, quantity, dosage, frequency, duration, unitPrice, totalCost) VALUES
('PR00000001', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000002', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000003', 'M000000004', 3, '20mg', 'Once daily', 30, 3.50, 105.00),
('PR00000004', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000005', 'M000000002', 2, '250mg', 'Every 8 hours', 7, 2.00, 42.00),
('PR00000006', 'M000000008', 3, '20mg', 'Once daily', 30, 3.80, 114.00),
('PR00000007', 'M000000003', 2, '400mg', 'Every 12 hours', 10, 0.75, 15.00),
('PR00000008', 'M000000009', 3, '100mg', 'Once daily', 30, 0.30, 9.00),
('PR00000009', 'M000000005', 3, '10mg', 'Twice daily', 15, 1.20, 36.00),
('PR00000010', 'M000000010', 3, '5mg', 'Once daily', 30, 0.80, 24.00),
('PR00000011', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000012', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000013', 'M000000003', 2, '400mg', 'Every 8 hours', 7, 0.75, 15.75),
('PR00000014', 'M000000008', 3, '20mg', 'Once daily', 30, 3.80, 114.00),
('PR00000015', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000016', 'M000000008', 3, '20mg', 'Once daily', 30, 3.80, 114.00),
('PR00000017', 'M000000003', 2, '400mg', 'Every 12 hours', 10, 0.75, 15.00),
('PR00000018', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000019', 'M000000005', 3, '10mg', 'Once daily', 30, 1.20, 36.00),
('PR00000020', 'M000000010', 3, '5mg', 'Once daily', 30, 0.80, 24.00),
('PR00000021', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000022', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000023', 'M000000003', 2, '400mg', 'Every 8 hours', 7, 0.75, 15.75),
('PR00000024', 'M000000008', 3, '20mg', 'Once daily', 30, 3.80, 114.00),
('PR00000025', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000026', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000027', 'M000000003', 2, '400mg', 'Every 12 hours', 10, 0.75, 15.00),
('PR00000028', 'M000000010', 3, '5mg', 'Once daily', 30, 0.80, 24.00),
('PR00000029', 'M000000003', 2, '400mg', 'Every 8 hours', 7, 0.75, 15.75),
('PR00000030', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000031', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000032', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000033', 'M000000003', 2, '400mg', 'Every 12 hours', 10, 0.75, 15.00),
('PR00000034', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000035', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000036', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000037', 'M000000003', 2, '400mg', 'Every 8 hours', 7, 0.75, 15.75),
('PR00000038', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000039', 'M000000003', 2, '400mg', 'Every 12 hours', 10, 0.75, 15.00),
('PR00000040', 'M000000010', 3, '5mg', 'Once daily', 30, 0.80, 24.00),
('PR00000041', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000042', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000043', 'M000000003', 2, '400mg', 'Every 8 hours', 7, 0.75, 15.75),
('PR00000044', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000045', 'M000000003', 2, '400mg', 'Every 12 hours', 10, 0.75, 15.00),
('PR00000046', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000047', 'M000000001', 2, '500mg', 'Every 6 hours', 5, 0.50, 10.00),
('PR00000048', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00),
('PR00000049', 'M000000003', 2, '400mg', 'Every 8 hours', 7, 0.75, 15.75),
('PR00000050', 'M000000007', 3, '10mg', 'Once daily', 30, 4.00, 120.00);

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