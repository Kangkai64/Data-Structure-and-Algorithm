package control;

import adt.ArrayBucketList;
import entity.Medicine;
import entity.Prescription;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import dao.MedicineDao;
import dao.PrescriptionDao;
import dao.PatientDao;
import dao.DoctorDao;
import dao.ConsultationDao;
import utility.ConsoleUtils;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;

/**
 * @author: Ho Kang Kai
 *          Pharmacy Management Control - Module 5
 *          Manages medicine dispensing after doctor consultation and maintain
 *          medicine stock control
 */

public class PharmacyManagementControl {

    private ArrayBucketList<Medicine> medicines;
    private ArrayBucketList<Prescription> prescriptions;
    private ArrayBucketList<Prescription> dispensedPrescriptions;
    private MedicineDao medicineDao;
    private PrescriptionDao prescriptionDao;
    private PatientDao patientDao;
    private DoctorDao doctorDao;
    private ConsultationDao consultationDao;

    public PharmacyManagementControl() {
        this.medicines = new ArrayBucketList<>();
        this.prescriptions = new ArrayBucketList<>();
        this.dispensedPrescriptions = new ArrayBucketList<>();
        this.medicineDao = new MedicineDao();
        this.prescriptionDao = new PrescriptionDao();
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        this.consultationDao = new ConsultationDao();
        loadMedicineData();
    }

    public void loadMedicineData() {
        try {
            medicines = medicineDao.findAll();
            prescriptions = prescriptionDao.findAll();
            Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
            while (prescriptionIterator.hasNext()) {
                Prescription prescription = prescriptionIterator.next();
                if (prescription.getStatus() == Prescription.PrescriptionStatus.DISPENSED) {
                    dispensedPrescriptions.add(prescription.hashCode(), prescription);
                }
            }
        } catch (Exception exception) {
            System.err.println("Error loading medicine data: " + exception.getMessage());
        }
    }

    // Medicine Management Methods
    public boolean addMedicine(String medicineName, String genericName, String manufacturer,
            String description, String dosageForm, String strength,
            int quantityInStock, int minimumStockLevel, double unitPrice,
            Date expiryDate, String storageLocation, boolean requiresPrescription) {
        try {
            // Get new medicine ID from database
            String medicineId = medicineDao.getNewId();

            // Create new medicine
            Medicine medicine = new Medicine(medicineId, medicineName, genericName, manufacturer,
                    description, dosageForm, strength, quantityInStock,
                    minimumStockLevel, unitPrice, expiryDate, storageLocation,
                    requiresPrescription);

            medicineDao.insert(medicine);

            // Add to medicines list
            medicines.add(medicine.hashCode(), medicine);

            return true;
        } catch (Exception exception) {
            System.err.println("Error adding medicine: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateMedicineStock(String medicineId, int quantity) {
        try {
            Medicine medicine = findMedicineById(medicineId);
            if (medicine != null) {
                medicine.setQuantityInStock(quantity);
                return medicineDao.updateStock(medicineId, quantity);
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating medicine stock: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateMedicinePrice(String medicineId, double newPrice) {
        try {
            Medicine medicine = findMedicineById(medicineId);
            if (medicine != null) {
                medicine.setUnitPrice(newPrice);
                medicineDao.updatePrice(medicineId, newPrice);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating medicine price: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateMedicineDetails(Medicine medicine) {
        try {
            medicineDao.update(medicine);
            return true;
        } catch (Exception exception) {
            System.err.println("Error updating medicine details: " + exception.getMessage());
            return false;
        }
    }

    public boolean discontinueMedicine(String medicineId) {
        try {
            Medicine medicine = findMedicineById(medicineId);
            if (medicine != null) {
                medicine.setStatus(Medicine.MedicineStatus.DISCONTINUED);
                medicineDao.updateStatus(medicineId, Medicine.MedicineStatus.DISCONTINUED);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error discontinuing medicine: " + exception.getMessage());
            return false;
        }
    }

    // Prescription Management Methods
    public boolean createPrescription(String patientId, String doctorId, String consultationId,
            String instructions, Date expiryDate) {
        try {
            // Get new prescription ID from database
            String prescriptionId = prescriptionDao.getNewId();

            Patient patient = patientDao.findById(patientId);
            Doctor doctor = doctorDao.findById(doctorId);
            Consultation consultation = consultationDao.findById(consultationId);

            // Create new prescription
            Prescription prescription = new Prescription(prescriptionId, patient, doctor,
                    consultation, new Date(), instructions, expiryDate);

            // Add to prescriptions list
            prescriptions.add(prescription.hashCode(), prescription);
            prescriptionDao.insert(prescription);

            return true;
        } catch (Exception exception) {
            System.err.println("Error creating prescription: " + exception.getMessage());
            return false;
        }
    }

    public boolean addMedicineToPrescription(String prescriptionId, String medicineId,
            int quantity, String dosage, String frequency,
            int duration) {
        try {
            Prescription prescription = findPrescriptionById(prescriptionId);
            Medicine medicine = findMedicineById(medicineId);

            if (prescription != null && medicine != null) {
                String prescribedMedicineId = prescriptionDao.getNewId();
                Prescription.PrescribedMedicine prescribedMedicine = new Prescription.PrescribedMedicine(
                        prescribedMedicineId, prescriptionId, medicine, quantity, dosage,
                        frequency, duration, medicine.getUnitPrice());

                boolean added = prescription.addPrescribedMedicine(prescribedMedicine);
                prescriptionDao.insertPrescribedMedicine(prescribedMedicine);
                return added;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error adding medicine to prescription: " + exception.getMessage());
            return false;
        }
    }

    public boolean removeMedicineFromPrescription(String prescriptionId, String prescribedMedicineId) {
        try {
            Prescription prescription = findPrescriptionById(prescriptionId);
            ArrayBucketList<Prescription.PrescribedMedicine> prescribedMedicines = prescription
                    .getPrescribedMedicines();
            Prescription.PrescribedMedicine prescribedMedicine = null;
            Iterator<Prescription.PrescribedMedicine> prescribedMedicineIterator = prescribedMedicines.iterator();
            while (prescribedMedicineIterator.hasNext()) {
                Prescription.PrescribedMedicine tempPrescribedMedicine = prescribedMedicineIterator.next();
                if (tempPrescribedMedicine.getPrescribedMedicineId().equals(prescribedMedicineId)) {
                    prescribedMedicine = tempPrescribedMedicine;
                }
            }

            if (prescription != null && prescribedMedicine != null) {
                boolean removed = prescription.removePrescribedMedicine(prescribedMedicine);
                prescriptionDao.deletePrescribedMedicine(prescriptionId, prescribedMedicineId);
                return removed;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error removing medicine from prescription: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateMedicineInPrescription(String prescriptionId, String prescribedMedicineId, String medicineId,
            int quantity, String dosage, String frequency, int duration) {
        try {
            Prescription prescription = findPrescriptionById(prescriptionId);
            ArrayBucketList<Prescription.PrescribedMedicine> prescribedMedicines = prescription
                    .getPrescribedMedicines();
            Prescription.PrescribedMedicine prescribedMedicine = null;
            Iterator<Prescription.PrescribedMedicine> prescribedMedicineIterator = prescribedMedicines.iterator();
            while (prescribedMedicineIterator.hasNext()) {
                Prescription.PrescribedMedicine tempPrescribedMedicine = prescribedMedicineIterator.next();
                if (tempPrescribedMedicine.getPrescribedMedicineId().equals(prescribedMedicineId)) {
                    prescribedMedicine = tempPrescribedMedicine;
                }
            }

            if (prescription != null && prescribedMedicine != null) {
                Medicine medicine = findMedicineById(medicineId);
                if (medicine != null) {
                    boolean updated = prescription.updatePrescribedMedicine(prescribedMedicine, medicine, quantity,
                            dosage, frequency, duration);
                    prescriptionDao.updatePrescribedMedicine(prescription, prescribedMedicine);
                    return updated;
                }
                return false;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating medicine in prescription: " + exception.getMessage());
            return false;
        }
    }

    public boolean dispensePrescription(String prescriptionId) {
        try {
            Prescription prescription = findPrescriptionById(prescriptionId);
            if (prescription != null && prescription.canBeDispensed()) {
                // Check if all medicines are available
                Iterator<Prescription.PrescribedMedicine> prescribedMedicineIterator = prescription
                        .getPrescribedMedicines().iterator();
                while (prescribedMedicineIterator.hasNext()) {
                    Prescription.PrescribedMedicine prescribedMedicine = prescribedMedicineIterator.next();
                    Medicine medicine = prescribedMedicine.getMedicine();

                    if (medicine.getQuantityInStock() < prescribedMedicine.getQuantity()) {
                        System.err.println("Insufficient stock for medicine: " + medicine.getMedicineName());
                        return false;
                    }
                }

                // Dispense medicines
                Iterator<Prescription.PrescribedMedicine> dispenseMedicineIterator = prescription
                        .getPrescribedMedicines().iterator();
                while (dispenseMedicineIterator.hasNext()) {
                    Prescription.PrescribedMedicine prescribedMedicine = dispenseMedicineIterator.next();
                    Medicine medicine = prescribedMedicine.getMedicine();

                    medicineDao.updateStock(medicine.getMedicineId(),
                            medicine.getQuantityInStock() - prescribedMedicine.getQuantity());
                }

                // Update prescription status
                prescription.setStatus(Prescription.PrescriptionStatus.DISPENSED);
                prescriptionDao.update(prescription);

                // Move to dispensed prescriptions
                dispensedPrescriptions.add(prescription.hashCode(), prescription);

                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error dispensing prescription: " + exception.getMessage());
            return false;
        }
    }

    // Search and Retrieval Methods
    public Medicine findMedicineById(String medicineId) {
        return medicines.getEntryByHash(Integer.parseInt(medicineId.replaceAll("[^0-9]", "")));
    }

    public Medicine findMedicineByName(String medicineName) {
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getMedicineName().equalsIgnoreCase(medicineName)) {
                return medicine;
            }
        }
        return null;
    }

    public Medicine findMedicineByGenericName(String genericName) {
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getGenericName().equalsIgnoreCase(genericName)) {
                return medicine;
            }
        }
        return null;
    }

    public ArrayBucketList<Medicine> findMedicineByManufacturer(String manufacturer) {
        ArrayBucketList<Medicine> manufacturerMedicines = new ArrayBucketList<>();
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getManufacturer().equalsIgnoreCase(manufacturer)) {
                manufacturerMedicines.add(medicine.hashCode(), medicine);
            }
        }
        return manufacturerMedicines;
    }

    public Medicine findMedicineByStatus(int statusChoice) {
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getStatus().equals(Medicine.MedicineStatus.values()[statusChoice - 1])) {
                return medicine;
            }
        }
        return null;
    }

    public ArrayBucketList<Medicine> getLowStockMedicines() {
        ArrayBucketList<Medicine> lowStockMedicines = new ArrayBucketList<>();
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.isLowStock()) {
                lowStockMedicines.add(medicine.hashCode(), medicine);
            }
        }
        return lowStockMedicines;
    }

    public ArrayBucketList<Medicine> getExpiredMedicines() {
        ArrayBucketList<Medicine> expiredMedicines = new ArrayBucketList<>();
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.isExpired()) {
                expiredMedicines.add(medicine.hashCode(), medicine);
            }
        }
        return expiredMedicines;
    }

    public Prescription findPrescriptionById(String prescriptionId) {
        return prescriptions.getEntryByHash(Integer.parseInt(prescriptionId.replaceAll("[^0-9]", "")));
    }

    public ArrayBucketList<Prescription> findPrescriptionsByPatient(String patientId) {
        ArrayBucketList<Prescription> patientPrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPatient().getPatientId().equals(patientId)) {
                patientPrescriptions.add(prescription.hashCode(), prescription);
            }
        }

        return patientPrescriptions;
    }

    public ArrayBucketList<Prescription> findPrescriptionsByDoctor(String doctorId) {
        ArrayBucketList<Prescription> doctorPrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getDoctor().getDoctorId().equals(doctorId)) {
                doctorPrescriptions.add(prescription.hashCode(), prescription);
            }
        }
        return doctorPrescriptions;
    }

    public ArrayBucketList<Prescription> findPrescriptionsByStatus(int statusChoice) {
        ArrayBucketList<Prescription> statusPrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getStatus().equals(Prescription.PrescriptionStatus.values()[statusChoice - 1])) {
                statusPrescriptions.add(prescription.hashCode(), prescription);
            }
        }
        return statusPrescriptions;
    }

    public ArrayBucketList<Prescription> findPrescriptionsByDateRange(String startDate, String endDate) {
        ArrayBucketList<Prescription> dateRangePrescriptions = new ArrayBucketList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date start = null;
        Date end = null;
        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (Exception e) {
            System.err.println("Error parsing dates: " + e.getMessage());
        }
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate().after(start) && prescription.getPrescriptionDate().before(end)) {
                dateRangePrescriptions.add(prescription.hashCode(), prescription);
            }
        }
        return dateRangePrescriptions;
    }

    public ArrayBucketList<Prescription> getActivePrescriptions() {
        ArrayBucketList<Prescription> activePrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getStatus() == Prescription.PrescriptionStatus.ACTIVE) {
                activePrescriptions.add(prescription.hashCode(), prescription);
            }
        }
        return activePrescriptions;
    }

    public ArrayBucketList<Medicine> getAllMedicines() {
        return medicines;
    }

    public ArrayBucketList<Prescription> getAllPrescriptions() {
        return prescriptions;
    }

    public int getTotalMedicines() {
        return medicines.getNumberOfEntries();
    }

    public int getTotalPrescriptions() {
        return prescriptions.getNumberOfEntries();
    }

    // Reporting Methods
    public String generateMedicineStockReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== MEDICINE STOCK REPORT ===\n");
        report.append("Total Medicines: ").append(getTotalMedicines()).append("\n");
        report.append("Low Stock Medicines: ").append(getLowStockMedicines().getNumberOfEntries()).append("\n");
        report.append("Expired Medicines: ").append(getExpiredMedicines().getNumberOfEntries()).append("\n");
        report.append("Report Generated: ").append(ConsoleUtils.reportDateTimeFormatter(new Date())).append("\n\n");

        report.append("----------------------------------------\n");
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            report.append(medicine);
            report.append("----------------------------------------\n");
        }

        return report.toString();
    }

    public String generatePrescriptionReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== PRESCRIPTION REPORT ===\n");
        report.append("Total Prescriptions: ").append(getTotalPrescriptions()).append("\n");
        report.append("Active Prescriptions: ").append(getActivePrescriptions().getNumberOfEntries()).append("\n");
        report.append("Dispensed Prescriptions: ").append(dispensedPrescriptions.getNumberOfEntries()).append("\n");
        report.append("Report Generated: ").append(ConsoleUtils.reportDateTimeFormatter(new Date())).append("\n\n");

        report.append("----------------------------------------\n");
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            report.append(prescription);
            report.append("----------------------------------------\n");
        }
        return report.toString();
    }
}