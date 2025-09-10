package control;

import adt.ArrayBucketList;
import adt.ArrayBucketListFactory;
import adt.IndexingUtility;
import utility.ConsoleUtils;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.time.format.DateTimeFormatter;

/**
 * @author: Ho Kang Kai
 *          Pharmacy Management Control - Module 5
 *          Manages prescription, medicine dispensing after doctor consultation
 *          and maintain medicine stock control
 */

public class PharmacyManagementControl {

    private ArrayBucketList<String, Medicine> medicineIndexById;
    private ArrayBucketList<String, Prescription> prescriptionIndexById;
    private ArrayBucketList<String, Prescription> dispensedPrescriptions;

    // Medicine indices
    private ArrayBucketList<String, ArrayBucketList<String, Medicine>> medicineIndexByName;
    private ArrayBucketList<String, ArrayBucketList<String, Medicine>> medicineIndexByGenericName;
    private ArrayBucketList<String, ArrayBucketList<String, Medicine>> medicineIndexByManufacturer;
    // Split by status into separate lists
    private ArrayBucketList<String, Medicine> medicinesAvailable;
    private ArrayBucketList<String, Medicine> medicinesLowStock;
    private ArrayBucketList<String, Medicine> medicinesOutOfStock;
    private ArrayBucketList<String, Medicine> medicinesDiscontinued;
    private ArrayBucketList<String, Medicine> medicinesExpired;
    private final MedicineDao medicineDao;

    // Prescription indices
    private ArrayBucketList<String, ArrayBucketList<String, Prescription>> prescriptionIndexByPatientId;
    private ArrayBucketList<String, ArrayBucketList<String, Prescription>> prescriptionIndexByDoctorId;
    private ArrayBucketList<String, ArrayBucketList<String, Prescription>> prescriptionIndexByPatientName;
    private ArrayBucketList<String, ArrayBucketList<String, Prescription>> prescriptionIndexByDoctorName;
    // Split by prescription status into separate lists
    private ArrayBucketList<String, Prescription> prescriptionsActive;
    private ArrayBucketList<String, Prescription> prescriptionsDispensed;
    private ArrayBucketList<String, Prescription> prescriptionsExpired;
    private ArrayBucketList<String, Prescription> prescriptionsCancelled;
    // Split by payment status into separate lists
    private ArrayBucketList<String, Prescription> paymentsPaid;
    private ArrayBucketList<String, Prescription> paymentsPending;
    private ArrayBucketList<String, Prescription> paymentsCancelled;
    private ArrayBucketList<LocalDate, ArrayBucketList<String, Prescription>> prescriptionIndexByDate;

    private final PrescriptionDao prescriptionDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final ConsultationDao consultationDao;

    public PharmacyManagementControl() {
        this.medicineIndexById = new ArrayBucketList<>();
        this.prescriptionIndexById = new ArrayBucketList<>();
        this.dispensedPrescriptions = new ArrayBucketList<>();
        this.prescriptionIndexById = ArrayBucketListFactory.createForStringIds(256);
        // Initialize indices with suitable strategies
        this.medicineIndexById = ArrayBucketListFactory.createForStringIds(256);
        this.medicineIndexByName = ArrayBucketListFactory.createForNamePrefix(26);
        this.medicineIndexByGenericName = ArrayBucketListFactory.createForNamePrefix(26);
        this.medicineIndexByManufacturer = ArrayBucketListFactory.createForNamePrefix(26);
        this.medicinesAvailable = ArrayBucketListFactory.createForStringIds(64);
        this.medicinesLowStock = ArrayBucketListFactory.createForStringIds(64);
        this.medicinesOutOfStock = ArrayBucketListFactory.createForStringIds(64);
        this.medicinesDiscontinued = ArrayBucketListFactory.createForStringIds(64);
        this.medicinesExpired = ArrayBucketListFactory.createForStringIds(64);
        this.prescriptionIndexByPatientId = ArrayBucketListFactory.createForStringIds(128);
        this.prescriptionIndexByDoctorId = ArrayBucketListFactory.createForStringIds(128);
        this.prescriptionIndexByPatientName = ArrayBucketListFactory.createForNamePrefix(26);
        this.prescriptionIndexByDoctorName = ArrayBucketListFactory.createForNamePrefix(26);
        this.prescriptionsActive = ArrayBucketListFactory.createForStringIds(128);
        this.prescriptionsDispensed = ArrayBucketListFactory.createForStringIds(128);
        this.prescriptionsExpired = ArrayBucketListFactory.createForStringIds(128);
        this.prescriptionsCancelled = ArrayBucketListFactory.createForStringIds(128);
        this.paymentsPaid = ArrayBucketListFactory.createForStringIds(128);
        this.paymentsPending = ArrayBucketListFactory.createForStringIds(128);
        this.paymentsCancelled = ArrayBucketListFactory.createForStringIds(128);
        this.prescriptionIndexByDate = ArrayBucketListFactory.createForLocalDates(64);
        this.medicineDao = new MedicineDao();
        this.prescriptionDao = new PrescriptionDao();
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        this.consultationDao = new ConsultationDao();
    }

    // ---------------- Indexing helpers ----------------
    private void indexMedicine(Medicine medicine) {
        if (medicine == null) {
            return;
        }
        IndexingUtility.addToIndexGroup(medicineIndexByName, medicine.getMedicineName(), medicine.getMedicineId(), medicine);
        IndexingUtility.addToIndexGroup(medicineIndexByGenericName, medicine.getGenericName(), medicine.getMedicineId(), medicine);
        IndexingUtility.addToIndexGroup(medicineIndexByManufacturer, medicine.getManufacturer(), medicine.getMedicineId(), medicine);
        addMedicineToStatusList(medicine);
    }

    private void reindexMedicine(Medicine oldMedicine, Medicine newMedicine) {
        if (newMedicine == null) {
            return;
        }
        if (oldMedicine != null) {
            IndexingUtility.removeFromIndexGroup(medicineIndexByName, oldMedicine.getMedicineName(), oldMedicine.getMedicineId());
            IndexingUtility.removeFromIndexGroup(medicineIndexByGenericName, oldMedicine.getGenericName(), oldMedicine.getMedicineId());
            IndexingUtility.removeFromIndexGroup(medicineIndexByManufacturer, oldMedicine.getManufacturer(), oldMedicine.getMedicineId());
            removeMedicineFromStatusList(oldMedicine);
        }
        indexMedicine(newMedicine);
    }
    private void indexPrescription(Prescription prescription) {
        if (prescription == null) {
            return;
        }
        // By patient ID
        IndexingUtility.addToIndexGroup(prescriptionIndexByPatientId, prescription.getPatient().getPatientId(), prescription.getPrescriptionId(), prescription);
        // By doctor ID
        IndexingUtility.addToIndexGroup(prescriptionIndexByDoctorId, prescription.getDoctor().getDoctorId(), prescription.getPrescriptionId(), prescription);
        // By patient name (prefix strategy)
        IndexingUtility.addToIndexGroup(prescriptionIndexByPatientName, prescription.getPatient().getFullName(), prescription.getPrescriptionId(), prescription);
        // By doctor name (prefix strategy)
        IndexingUtility.addToIndexGroup(prescriptionIndexByDoctorName, prescription.getDoctor().getFullName(), prescription.getPrescriptionId(), prescription);
        // By status (split lists)
        addPrescriptionToStatusList(prescription);
        // By payment status (split lists)
        addPrescriptionToPaymentList(prescription);
        // By prescription date
        IndexingUtility.addToIndexGroup(prescriptionIndexByDate, prescription.getPrescriptionDate(), prescription.getPrescriptionId(), prescription);
    }

    private void reindexPrescription(Prescription oldPrescription,
                                     Prescription newPrescription,
                                     Prescription.PrescriptionStatus oldStatus,
                                     Prescription.PaymentStatus oldPaymentStatus) {
        if (newPrescription == null) {
            return;
        }
        // Remove from old groups if keys changed
        if (oldPrescription != null) {
            IndexingUtility.removeFromIndexGroup(prescriptionIndexByPatientId, oldPrescription.getPatient().getPatientId(), oldPrescription.getPrescriptionId());
            IndexingUtility.removeFromIndexGroup(prescriptionIndexByDoctorId, oldPrescription.getDoctor().getDoctorId(), oldPrescription.getPrescriptionId());
            IndexingUtility.removeFromIndexGroup(prescriptionIndexByPatientName, oldPrescription.getPatient().getFullName(), oldPrescription.getPrescriptionId());
            IndexingUtility.removeFromIndexGroup(prescriptionIndexByDoctorName, oldPrescription.getDoctor().getFullName(), oldPrescription.getPrescriptionId());
            removePrescriptionFromStatusList(oldPrescription, oldStatus);
            removePrescriptionFromPaymentList(oldPrescription, oldPaymentStatus);
            IndexingUtility.removeFromIndexGroup(prescriptionIndexByDate, oldPrescription.getPrescriptionDate(), oldPrescription.getPrescriptionId());
        }
        // Add to new groups
        indexPrescription(newPrescription);
    }

    // ---------------- Status list helpers ----------------
    private void addMedicineToStatusList(Medicine medicine) {
        ArrayBucketList<String, Medicine> list = getMedicineStatusList(medicine.getStatus());
        if (list != null) {
            list.add(medicine.getMedicineId(), medicine);
        }
        if (medicine.getStatus() == Medicine.MedicineStatus.EXPIRED || medicine.isExpired()) {
            medicinesExpired.add(medicine.getMedicineId(), medicine);
        }
    }

    private void removeMedicineFromStatusList(Medicine medicine) {
        ArrayBucketList<String, Medicine> list = getMedicineStatusList(medicine.getStatus());
        if (list != null) {
            list.remove(medicine.getMedicineId());
        }
        if (medicine.getStatus() == Medicine.MedicineStatus.EXPIRED || medicine.isExpired()) {
            medicinesExpired.remove(medicine.getMedicineId());
        }
    }

    private ArrayBucketList<String, Medicine> getMedicineStatusList(Medicine.MedicineStatus status) {
        if (status == null) return null;
        return switch (status) {
            case AVAILABLE -> medicinesAvailable;
            case LOW_STOCK -> medicinesLowStock;
            case OUT_OF_STOCK -> medicinesOutOfStock;
            case DISCONTINUED -> medicinesDiscontinued;
            case EXPIRED -> medicinesExpired;
        };
    }

    private void addPrescriptionToStatusList(Prescription prescription) {
        ArrayBucketList<String, Prescription> list = getPrescriptionStatusList(prescription.getStatus());
        if (list != null) {
            list.add(prescription.getPrescriptionId(), prescription);
        }
        if (prescription.getStatus() == Prescription.PrescriptionStatus.DISPENSED) {
            // keep legacy list in sync
            dispensedPrescriptions.add(prescription.getPrescriptionId(), prescription);
        }
    }

    private void removePrescriptionFromStatusList(Prescription prescription, Prescription.PrescriptionStatus status) {
        ArrayBucketList<String, Prescription> list = getPrescriptionStatusList(status);
        if (list != null) {
            list.remove(prescription.getPrescriptionId());
        }
        if (status == Prescription.PrescriptionStatus.DISPENSED) {
            dispensedPrescriptions.remove(prescription.getPrescriptionId());
        }
    }

    private ArrayBucketList<String, Prescription> getPrescriptionStatusList(Prescription.PrescriptionStatus status) {
        if (status == null) return null;
        return switch (status) {
            case ACTIVE -> prescriptionsActive;
            case DISPENSED -> prescriptionsDispensed;
            case EXPIRED -> prescriptionsExpired;
            case CANCELLED -> prescriptionsCancelled;
        };
    }

    private void addPrescriptionToPaymentList(Prescription prescription) {
        ArrayBucketList<String, Prescription> list = getPaymentStatusList(prescription.getPaymentStatus());
        if (list != null) {
            list.add(prescription.getPrescriptionId(), prescription);
        }
    }

    private void removePrescriptionFromPaymentList(Prescription prescription, Prescription.PaymentStatus status) {
        ArrayBucketList<String, Prescription> list = getPaymentStatusList(status);
        if (list != null) {
            list.remove(prescription.getPrescriptionId());
        }
    }

    private ArrayBucketList<String, Prescription> getPaymentStatusList(Prescription.PaymentStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PAID -> paymentsPaid;
            case PENDING -> paymentsPending;
            case CANCELLED -> paymentsCancelled;
        };
    }

    public void loadPharmacyData() {
        try {
            medicineIndexById = medicineDao.findAll();
            prescriptionIndexById = prescriptionDao.findAll();
            // Index medicines
            Iterator<Medicine> medIterator = medicineIndexById.iterator();
            while (medIterator.hasNext()) {
                Medicine medicine = medIterator.next();
                indexMedicine(medicine);
                medicineIndexById.add(medicine.getMedicineId(), medicine);
            }
            Iterator<Prescription> prescriptionIterator = prescriptionIndexById.iterator();
            while (prescriptionIterator.hasNext()) {
                Prescription prescription = prescriptionIterator.next();
                indexPrescription(prescription);
                // Also index by ID for fast lookups
                prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);
            }
        } catch (Exception exception) {
            System.err.println("Error loading medicine data: " + exception.getMessage());
        }
    }

    // Medicine Management Methods
    public boolean addMedicine(Medicine medicine) {
        try {
            // Create new medicine
            medicineDao.insertAndReturnId(medicine);

            // Add to medicines list
            medicineIndexById.add(medicine.getMedicineId(), medicine);
            indexMedicine(medicine);
            medicineIndexById.add(medicine.getMedicineId(), medicine);

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
                medicineDao.updateStock(medicineId, quantity);
                // no index keys changed
                return true;
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
                // no index keys changed
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
            Medicine old = findMedicineById(medicine.getMedicineId());
            medicineDao.update(medicine);
            medicineIndexById.add(medicine.getMedicineId(), medicine);
            reindexMedicine(old, medicine);
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
                medicineIndexById.add(medicine.getMedicineId(), medicine);
                reindexMedicine(medicine, medicine);
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
            String instructions, LocalDate expiryDate) {
        try {
            Patient patient = patientDao.findById(patientId);
            Doctor doctor = doctorDao.findById(doctorId);
            Consultation consultation = consultationDao.findById(consultationId);

            // Create new prescription
            Prescription prescription = new Prescription(null, patient, doctor,
                    consultation, LocalDate.now(), instructions, expiryDate);

            // Add to prescriptions list
            prescriptionDao.insertAndReturnId(prescription);
            prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);
            indexPrescription(prescription);
            prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);

            return true;
        } catch (Exception exception) {
            System.err.println("Error creating prescription: " + exception.getMessage());
            return false;
        }
    }

    public boolean addMedicineToPrescription(Prescription.PrescribedMedicine prescribedMedicine) {
        try {
            Prescription prescription = findPrescriptionById(prescribedMedicine.getPrescriptionId());
            Medicine medicine = findMedicineById(prescribedMedicine.getMedicine().getMedicineId());

            if (prescription != null && medicine != null) {
                prescriptionDao.insertPrescribedMedicineAndReturnId(prescribedMedicine);
                prescription.addPrescribedMedicine(prescribedMedicine);
                prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);
                // No need to re-index by patient/doctor; prescription identity unchanged
                return true;
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
            ArrayBucketList<String, Prescription.PrescribedMedicine> prescribedMedicines = prescription
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
                prescription.removePrescribedMedicine(prescribedMedicine);
                prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);
                return removed;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error removing medicine from prescription: " + exception.getMessage());
            return false;
        }
    }

    public boolean updatePrescription(Prescription prescription) {
        Prescription.PrescriptionStatus oldStatus = null;
        try {
            Prescription oldPrescription = findPrescriptionById(prescription.getPrescriptionId());
            if (oldPrescription != null) {
                oldStatus = oldPrescription.getStatus();
            }

            prescriptionDao.update(prescription);
            prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);
            if (prescription.getStatus() == Prescription.PrescriptionStatus.DISPENSED) {
                dispensedPrescriptions.add(prescription.getPrescriptionId(), prescription);
            }

            if (oldStatus == Prescription.PrescriptionStatus.DISPENSED
                    && prescription.getStatus() != Prescription.PrescriptionStatus.DISPENSED) {
                dispensedPrescriptions.remove(prescription.getPrescriptionId());
            }

            Prescription.PaymentStatus oldPaymentStatus = oldPrescription != null ? oldPrescription.getPaymentStatus() : null;
            reindexPrescription(oldPrescription, prescription, oldStatus, oldPaymentStatus);

            return true;
        } catch (Exception exception) {
            System.err.println("Error updating prescription: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateMedicineInPrescription(Prescription.PrescribedMedicine prescribedMedicine) {
        try {
            Prescription prescription = findPrescriptionById(prescribedMedicine.getPrescriptionId());
            if (prescription != null && prescribedMedicine != null) {
                Medicine medicine = prescribedMedicine.getMedicine();
                if (medicine != null) {
                    boolean updated = prescription.updatePrescribedMedicine(prescribedMedicine, medicine,
                            prescribedMedicine.getQuantity(),
                            prescribedMedicine.getDosage(), prescribedMedicine.getFrequency(),
                            prescribedMedicine.getDuration());
                    prescriptionDao.updatePrescribedMedicine(prescription, prescribedMedicine);
                    prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);
                    medicineIndexById.add(medicine.getMedicineId(), medicine);
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
                // Mark as paid on successful dispensing
                prescription.setPaymentStatus(Prescription.PaymentStatus.PAID);
                prescriptionDao.update(prescription);
                dispensedPrescriptions.add(prescription.getPrescriptionId(), prescription);
                prescriptionIndexById.add(prescription.getPrescriptionId(), prescription);
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
        Medicine byIndex = medicineIndexById.getValue(medicineId);
        return byIndex != null ? byIndex : medicineIndexById.getValue(medicineId);
    }

    public ArrayBucketList<String, Medicine> findMedicineByName(String medicineName) {
        ArrayBucketList<String, Medicine> results = ArrayBucketListFactory.createForStringIds(16);
        if (medicineName == null) {
            return results;
        }
        String query = medicineName.trim();
        if (query.isEmpty()) {
            return results;
        }

        // Use name-prefix aware lookup within the hashed bucket only
        ArrayBucketList<String, ArrayBucketList<String, Medicine>> groups = medicineIndexByName.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Medicine>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Medicine> group = groupIterator.next();
            Iterator<Medicine> iterator = group.iterator();
            while (iterator.hasNext()) {
                Medicine medicine = iterator.next();
                results.add(medicine.getMedicineId(), medicine);
            }
        }

        return results;
    }

    public ArrayBucketList<String, Medicine> findMedicineByGenericName(String genericName) {
        ArrayBucketList<String, Medicine> results = ArrayBucketListFactory.createForStringIds(16);
        if (genericName == null) {
            return results;
        }
        String query = genericName.trim();
        if (query.isEmpty()) {
            return results;
        }

        ArrayBucketList<String, ArrayBucketList<String, Medicine>> groups = medicineIndexByGenericName.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Medicine>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Medicine> group = groupIterator.next();
            Iterator<Medicine> iterator = group.iterator();
            while (iterator.hasNext()) {
                Medicine medicine = iterator.next();
                results.add(medicine.getMedicineId(), medicine);
            }
        }

        return results;
    }

    public ArrayBucketList<String, Medicine> findMedicineByManufacturer(String manufacturer) {
        ArrayBucketList<String, Medicine> results = ArrayBucketListFactory.createForStringIds(16);
        if (manufacturer == null) {
            return results;
        }
        String query = manufacturer.trim();
        if (query.isEmpty()) {
            return results;
        }

        ArrayBucketList<String, ArrayBucketList<String, Medicine>> groups = medicineIndexByManufacturer
                .getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Medicine>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Medicine> group = groupIterator.next();
            Iterator<Medicine> iterator = group.iterator();
            while (iterator.hasNext()) {
                Medicine medicine = iterator.next();
                results.add(medicine.getMedicineId(), medicine);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Medicine> findMedicineByStatus(int statusChoice) {
        return getMedicineStatusList(Medicine.MedicineStatus.values()[statusChoice - 1]);
    }

    public ArrayBucketList<String, Medicine> getLowStockMedicines() {
        return medicinesLowStock;
    }

    public ArrayBucketList<String, Medicine> getExpiredMedicines() {
        return medicinesExpired;
    }

    public ArrayBucketList<String, Medicine> getDiscontinuedMedicines() {
        return medicinesDiscontinued;
    }

    public ArrayBucketList<String, Medicine> getAvailableMedicines() {
        return medicinesAvailable;
    }

    public Prescription findPrescriptionById(String prescriptionId) {
        return prescriptionIndexById.getValue(prescriptionId);
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByPatient(String patientId) {
        ArrayBucketList<String, Prescription> group = prescriptionIndexByPatientId.getValue(patientId);
        return group != null ? group : new ArrayBucketList<>();
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByDoctor(String doctorId) {
        ArrayBucketList<String, Prescription> group = prescriptionIndexByDoctorId.getValue(doctorId);
        return group != null ? group : new ArrayBucketList<>();
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByPatientName(String patientName) {
        ArrayBucketList<String, Prescription> results = ArrayBucketListFactory.createForStringIds(16);
        if (patientName == null) {
            return results;
        }
        String query = patientName.trim();
        if (query.isEmpty()) {
            return results;
        }
        ArrayBucketList<String, ArrayBucketList<String, Prescription>> groups = prescriptionIndexByPatientName
                .getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Prescription>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Prescription> group = groupIterator.next();
            Iterator<Prescription> iterator = group.iterator();
            while (iterator.hasNext()) {
                Prescription prescription = iterator.next();
                results.add(prescription.getPrescriptionId(), prescription);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByDoctorName(String doctorName) {
        ArrayBucketList<String, Prescription> results = ArrayBucketListFactory.createForStringIds(16);
        if (doctorName == null) {
            return results;
        }
        String query = doctorName.trim();
        if (query.isEmpty()) {
            return results;
        }
        ArrayBucketList<String, ArrayBucketList<String, Prescription>> groups = prescriptionIndexByDoctorName
                .getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Prescription>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Prescription> group = groupIterator.next();
            Iterator<Prescription> iterator = group.iterator();
            while (iterator.hasNext()) {
                Prescription prescription = iterator.next();
                results.add(prescription.getPrescriptionId(), prescription);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByStatus(int statusChoice) {
        return getPrescriptionStatusList(Prescription.PrescriptionStatus.values()[statusChoice - 1]);
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByPaymentStatus(
            Prescription.PaymentStatus paymentStatus) {
        return getPaymentStatusList(paymentStatus);
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        ArrayBucketList<String, Prescription> group = prescriptionIndexByDate.getValue(startDate);
        return group != null ? group : new ArrayBucketList<>();
    }

    public ArrayBucketList<String, Prescription> getActivePrescriptions() {
        return prescriptionsActive;
    }

    public ArrayBucketList<String, Medicine> getAllMedicines() {
        return medicineIndexById;
    }

    public ArrayBucketList<String, Prescription> getAllPrescriptions() {
        return prescriptionIndexById;
    }

    public int getTotalMedicines() {
        return medicineIndexById.getSize();
    }

    public int getTotalPrescriptions() {
        return prescriptionIndexById.getSize();
    }

    // Sorted display for search results (Medicines)
    public String displaySortedMedicineSearchResults(ArrayBucketList<String, Medicine> list, String searchCriteria,
            String sortBy, String sortOrder) {
        if (list == null || list.isEmpty()) {
            return "No medicines found.";
        }

        Medicine[] items = list.toArray(Medicine.class);

        Comparator<Medicine> comparator = getMedicineComparator(sortBy);
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        utility.QuickSort.sort(items, comparator);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n=== Medicine Search Results ===\n");
        stringBuilder.append("Criteria: ").append(searchCriteria).append("\n");
        stringBuilder.append(String.format("Sorted by: %s (%s)\n\n", getSortFieldDisplayName(sortBy),
                (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) ? "DESC" : "ASC"));
        stringBuilder.append(String.format("%-10s | %-20s | %-16s | %6s | %-10s | %13s | %-10s\n",
                "ID", "Name", "Generic", "Stock", "Status", "Price", "Expiry"));
        stringBuilder.append("-".repeat(105)).append("\n");

        for (Medicine medicine : items) {
            if (medicine == null)
                continue;
            String id = medicine.getMedicineId() == null ? "-" : medicine.getMedicineId();
            String name = medicine.getMedicineName() == null ? "-" : medicine.getMedicineName();
            String generic = medicine.getGenericName() == null ? "-" : medicine.getGenericName();
            String status = medicine.getStatus() == null ? "-" : medicine.getStatus().toString();
            String expiry = medicine.getExpiryDate() == null ? "-"
                    : medicine.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            if (name.length() > 20)
                name = name.substring(0, 19) + "…";
            if (generic.length() > 16)
                generic = generic.substring(0, 15) + "…";
            stringBuilder.append(
                    String.format("%-10s | %-20s | %-16s | %,6d | %-10s | RM %,10.2f | %-10s\n", id, name, generic,
                            medicine.getQuantityInStock(), status, medicine.getUnitPrice(), expiry));
        }

        stringBuilder.append("-".repeat(105)).append("\n");
        return stringBuilder.toString();
    }

    // Sorted display for search results (Prescriptions)
    public String displaySortedPrescriptionSearchResults(ArrayBucketList<String, Prescription> list,
            String searchCriteria, String sortBy, String sortOrder) {
        if (list == null || list.isEmpty()) {
            return "No prescriptions found.";
        }

        Prescription[] items = list.toArray(Prescription.class);

        Comparator<Prescription> comparator = getPrescriptionComparator(sortBy);
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        utility.QuickSort.sort(items, comparator);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n=== Prescription Search Results ===\n");
        stringBuilder.append("Criteria: ").append(searchCriteria).append("\n");
        stringBuilder.append(String.format("Sorted by: %s (%s)\n\n", getSortFieldDisplayName(sortBy),
                (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) ? "DESC" : "ASC"));
        stringBuilder.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | %14s\n",
                "ID", "Patient", "Doctor", "Date", "Status", "Total"));
        stringBuilder.append("-".repeat(110)).append("\n");

        for (Prescription prescription : items) {
            if (prescription == null)
                continue;
            String id = prescription.getPrescriptionId() == null ? "-" : prescription.getPrescriptionId();
            String patientName = prescription.getPatient() == null ? "-" : prescription.getPatient().getFullName();
            String doctorName = prescription.getDoctor() == null ? "-" : prescription.getDoctor().getFullName();
            String date = prescription.getPrescriptionDate() == null ? "-"
                    : prescription.getPrescriptionDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String status = prescription.getStatus() == null ? "-" : prescription.getStatus().toString();
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";
            stringBuilder.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | RM %,10.2f\n", id, patientName,
                    doctorName, date, status, prescription.getTotalCost()));
        }

        stringBuilder.append("-".repeat(110)).append("\n");
        return stringBuilder.toString();
    }

    // Display Active Prescriptions
    public String displayActivePrescriptions(String sortBy, String sortOrder) {
        ArrayBucketList<String, Prescription> activePrescriptions = getActivePrescriptions();
        if (activePrescriptions.isEmpty()) {
            return "No active prescriptions found.";
        }

        Prescription[] items = activePrescriptions.toArray(Prescription.class);

        Comparator<Prescription> comparator = getPrescriptionComparator(sortBy);
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        utility.QuickSort.sort(items, comparator);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("=".repeat(110)).append("\n");
        stringBuilder.append(ConsoleUtils.centerText("ACTIVE PRESCRIPTIONS", 110)).append("\n");
        stringBuilder.append("=".repeat(110)).append("\n\n");
        stringBuilder.append(String.format("Sorted by: %s (%s)\n\n", getSortFieldDisplayName(sortBy),
                (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) ? "DESC" : "ASC"));
        stringBuilder.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | %14s\n",
                "ID", "Patient", "Doctor", "Date", "Status", "Total"));
        stringBuilder.append("-".repeat(110)).append("\n");

        for (Prescription prescription : items) {
            if (prescription == null)
                continue;
            String id = prescription.getPrescriptionId() == null ? "-" : prescription.getPrescriptionId();
            String patientName = prescription.getPatient() == null ? "-" : prescription.getPatient().getFullName();
            String doctorName = prescription.getDoctor() == null ? "-" : prescription.getDoctor().getFullName();
            String date = prescription.getPrescriptionDate() == null ? "-"
                    : prescription.getPrescriptionDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String status = prescription.getStatus() == null ? "-" : prescription.getStatus().toString();
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";
            stringBuilder.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | RM %,10.2f\n", id, patientName,
                    doctorName, date, status, prescription.getTotalCost()));
        }

        stringBuilder.append("=".repeat(110)).append("\n");
        return stringBuilder.toString();
    }

    // Reporting Methods
    public String generateMedicineStockReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("PHARMACY MANAGEMENT SYSTEM - MEDICINE STOCK ANALYSIS REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("SUMMARY STATISTICS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("Total Medicines in System: %d\n", getTotalMedicines()));
        report.append(String.format("Low Stock Medicines: %d\n", getLowStockMedicines().getSize()));
        report.append(String.format("Expired Medicines: %d\n", getExpiredMedicines().getSize()));
        report.append(String.format("Active Medicines: %d\n", getTotalMedicines() - getExpiredMedicines().getSize()));

        int[] expiryYears = new int[10];
        int[] stockByYear = new int[10];
        double[] valueByYear = new double[10];
        int yearCount = 0;

        Iterator<Medicine> medicineIterator = medicineIndexById.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getExpiryDate() != null) {
                int year = medicine.getExpiryDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int yearIdx = 0; yearIdx < yearCount; yearIdx++) {
                    if (expiryYears[yearIdx] == year) {
                        yearIndex = yearIdx;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    expiryYears[yearCount] = year;
                    stockByYear[yearCount] = medicine.getQuantityInStock();
                    valueByYear[yearCount] = medicine.getQuantityInStock() * medicine.getUnitPrice();
                    yearCount++;
                } else {
                    // Update existing year data
                    stockByYear[yearIndex] += medicine.getQuantityInStock();
                    valueByYear[yearIndex] += medicine.getQuantityInStock() * medicine.getUnitPrice();
                }
            }
        }

        report.append("\nSTOCK DISTRIBUTION BY EXPIRY YEAR:\n");
        // Sort years in descending order using synchronized bubble sort
        if (yearCount > 1) {
            for (int outerIndex = 0; outerIndex < yearCount - 1; outerIndex++) {
                for (int innerIndex = outerIndex + 1; innerIndex < yearCount; innerIndex++) {
                    if (expiryYears[outerIndex] < expiryYears[innerIndex]) {
                        // Swap years
                        int tempYear = expiryYears[outerIndex];
                        expiryYears[outerIndex] = expiryYears[innerIndex];
                        expiryYears[innerIndex] = tempYear;

                        // Swap stock quantities
                        int tempStock = stockByYear[outerIndex];
                        stockByYear[outerIndex] = stockByYear[innerIndex];
                        stockByYear[innerIndex] = tempStock;

                        // Swap values
                        double tempValue = valueByYear[outerIndex];
                        valueByYear[outerIndex] = valueByYear[innerIndex];
                        valueByYear[innerIndex] = tempValue;
                    }
                }
            }
        }

        for (int yearIndex = 0; yearIndex < yearCount; yearIndex++) {
            report.append(String.format("Year %d: %,6d units (RM %,12.2f value)\n",
                    expiryYears[yearIndex], stockByYear[yearIndex], valueByYear[yearIndex]));
        }

        // Category analysis using arrays
        String[] medicineStatuses = new String[10]; // Assuming max 10 different statuses
        int[] statusCounts = new int[10];
        int statusCount = 0;

        medicineIterator = medicineIndexById.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            String status = medicine.getStatus() != null ? medicine.getStatus().toString() : "UNKNOWN";

            // Find if status already exists
            int statusIndex = -1;
            for (int statusCounter = 0; statusCounter < statusCount; statusCounter++) {
                if (medicineStatuses[statusCounter].equals(status)) {
                    statusIndex = statusCounter;
                    break;
                }
            }

            // If status doesn't exist, add new entry
            if (statusIndex == -1) {
                medicineStatuses[statusCount] = status;
                statusCounts[statusCount] = 1;
                statusCount++;
            } else {
                // Update existing status count
                statusCounts[statusIndex]++;
            }
        }

        report.append("\nCATEGORY ANALYSIS:\n");
        for (int statusCounter = 0; statusCounter < statusCount; statusCounter++) {
            report.append(String.format("%-15s: %d medicines\n", medicineStatuses[statusCounter],
                    statusCounts[statusCounter]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed medicine table with sorting
        report.append(ConsoleUtils.centerText("DETAILED MEDICINE INVENTORY", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-20s | %-16s | %6s | %6s | %-10s | %12s | %-10s\n",
                "ID", "Name", "Generic", "Stock", "Min", "Status", "Price", "Expiry"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        Medicine[] medicineArray = medicineIndexById.toArray(Medicine.class);

        // Sort the medicine array
        sortMedicineArray(medicineArray, sortBy, sortOrder);

        // Generate sorted table
        for (Medicine medicine : medicineArray) {
            String id = medicine.getMedicineId() == null ? "-" : medicine.getMedicineId();
            String name = medicine.getMedicineName() == null ? "-" : medicine.getMedicineName();
            String generic = medicine.getGenericName() == null ? "-" : medicine.getGenericName();
            String status = medicine.getStatus() == null ? "-" : medicine.getStatus().toString();
            String expiry = medicine.getExpiryDate() == null
                    ? "-"
                    : medicine.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));

            // Truncate fields to fit
            if (name.length() > 20)
                name = name.substring(0, 19) + "…";
            if (generic.length() > 16)
                generic = generic.substring(0, 15) + "…";

            report.append(String.format("%-8s | %-20s | %-16s | %,6d | %6d | %-10s | RM %,8.2f | %-10s\n",
                    id, name, generic, medicine.getQuantityInStock(),
                    medicine.getMinimumStockLevel(), status, medicine.getUnitPrice(), expiry));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF MEDICINE STOCK REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }

    public String generatePrescriptionReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("PHARMACY MANAGEMENT SYSTEM - PRESCRIPTION ANALYSIS REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("PRESCRIPTION SUMMARY", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("Total Prescriptions: %d\n", getTotalPrescriptions()));
        report.append(String.format("Active Prescriptions: %d\n", getActivePrescriptions().getSize()));
        report.append(String.format("Dispensed Prescriptions: %d\n", dispensedPrescriptions.getSize()));
        report.append(String.format("Completion Rate: %.1f%%\n",
                (double) dispensedPrescriptions.getSize() / getTotalPrescriptions() * 100));

        // Prescriptions by year analysis using arrays
        int[] prescriptionYears = new int[20];
        int[] prescriptionsByYear = new int[20];
        double[] revenueByYear = new double[20];
        int prescriptionYearCount = 0;

        Iterator<Prescription> prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate() != null) {
                int year = prescription.getPrescriptionDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int yearCounter = 0; yearCounter < prescriptionYearCount; yearCounter++) {
                    if (prescriptionYears[yearCounter] == year) {
                        yearIndex = yearCounter;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    prescriptionYears[prescriptionYearCount] = year;
                    prescriptionsByYear[prescriptionYearCount] = 1;
                    revenueByYear[prescriptionYearCount] = prescription.getTotalCost();
                    prescriptionYearCount++;
                } else {
                    // Update existing year data
                    prescriptionsByYear[yearIndex]++;
                    revenueByYear[yearIndex] += prescription.getTotalCost();
                }
            }
        }

        report.append("\nPRESCRIPTIONS BY YEAR:\n");
        // Sort prescription years in descending order using QuickSort
        if (prescriptionYearCount > 1) {
            for (int outerIndex = 0; outerIndex < prescriptionYearCount - 1; outerIndex++) {
                for (int innerIndex = outerIndex + 1; innerIndex < prescriptionYearCount; innerIndex++) {
                    if (prescriptionYears[outerIndex] < prescriptionYears[innerIndex]) {
                        // Swap years
                        int tempYear = prescriptionYears[outerIndex];
                        prescriptionYears[outerIndex] = prescriptionYears[innerIndex];
                        prescriptionYears[innerIndex] = tempYear;

                        // Swap counts
                        int tempCount = prescriptionsByYear[outerIndex];
                        prescriptionsByYear[outerIndex] = prescriptionsByYear[innerIndex];
                        prescriptionsByYear[innerIndex] = tempCount;

                        // Swap revenues
                        double tempRevenue = revenueByYear[outerIndex];
                        revenueByYear[outerIndex] = revenueByYear[innerIndex];
                        revenueByYear[innerIndex] = tempRevenue;
                    }
                }
            }
        }

        for (int yearCounter = 0; yearCounter < prescriptionYearCount; yearCounter++) {
            report.append(String.format("Year %d: %,6d prescriptions (RM %,12.2f revenue)\n",
                    prescriptionYears[yearCounter], prescriptionsByYear[yearCounter], revenueByYear[yearCounter]));
        }

        // Status distribution using arrays
        String[] prescriptionStatuses = new String[10]; // Assuming max 10 different statuses
        int[] prescriptionStatusCounts = new int[10];
        int prescriptionStatusCount = 0;

        prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            String status = prescription.getStatus() != null ? prescription.getStatus().toString() : "UNKNOWN";

            // Find if status already exists
            int statusIndex = -1;
            for (int statusCounter = 0; statusCounter < prescriptionStatusCount; statusCounter++) {
                if (prescriptionStatuses[statusCounter].equals(status)) {
                    statusIndex = statusCounter;
                    break;
                }
            }

            // If status doesn't exist, add new entry
            if (statusIndex == -1) {
                prescriptionStatuses[prescriptionStatusCount] = status;
                prescriptionStatusCounts[prescriptionStatusCount] = 1;
                prescriptionStatusCount++;
            } else {
                // Update existing status count
                prescriptionStatusCounts[statusIndex]++;
            }
        }

        report.append("\nPRESCRIPTION STATUS DISTRIBUTION:\n");
        for (int statusCounter = 0; statusCounter < prescriptionStatusCount; statusCounter++) {
            report.append(String.format("%-15s: %d prescriptions\n", prescriptionStatuses[statusCounter],
                    prescriptionStatusCounts[statusCounter]));
        }

        // Monthly trend for current year using arrays
        int currentYear = LocalDate.now().getYear();
        int[] monthlyTrend = new int[13]; // Index 0 unused, 1-12 for months

        prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate() != null &&
                    prescription.getPrescriptionDate().getYear() == currentYear) {
                int month = prescription.getPrescriptionDate().getMonthValue();
                monthlyTrend[month]++;
            }
        }

        report.append(String.format("\nMONTHLY TREND FOR %d:\n", currentYear));
        String[] months = { "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        for (int monthCounter = 1; monthCounter <= 12; monthCounter++) {
            if (monthlyTrend[monthCounter] > 0) {
                report.append(
                        String.format("%-3s: %d prescriptions\n", months[monthCounter], monthlyTrend[monthCounter]));
            }
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed prescription table with sorting
        report.append(ConsoleUtils.centerText("DETAILED PRESCRIPTION RECORDS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | %14s\n",
                "ID", "Patient", "Doctor", "Date", "Status", "Total"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        Prescription[] prescriptionArray = prescriptionIndexById.toArray(Prescription.class);

        // Sort the prescription array
        sortPrescriptionArray(prescriptionArray, sortBy, sortOrder);

        // Generate sorted table
        for (Prescription prescription : prescriptionArray) {
            String id = prescription.getPrescriptionId() == null ? "-" : prescription.getPrescriptionId();
            String patientName = prescription.getPatient() == null ? "-" : prescription.getPatient().getFullName();
            String doctorName = prescription.getDoctor() == null ? "-" : prescription.getDoctor().getFullName();
            String date = prescription.getPrescriptionDate() == null
                    ? "-"
                    : prescription.getPrescriptionDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String status = prescription.getStatus() == null ? "-" : prescription.getStatus().toString();

            // Truncate long names
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";

            report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | RM %,10.2f\n",
                    id, patientName, doctorName, date, status, prescription.getTotalCost()));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF PRESCRIPTION REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }

    /**
     * Generates a medicine usage report analyzing prescription patterns, most
     * prescribed medicines, and usage trends
     * 
     * @param sortBy    field to sort by
     * @param sortOrder sort order (asc/desc)
     * @return formatted report string
     */
    public String generateMedicineUsageReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(125)).append("\n");
        report.append(ConsoleUtils.centerText("PHARMACY MANAGEMENT SYSTEM - MEDICINE USAGE REPORT", 125))
                .append("\n");
        report.append("=".repeat(125)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(125)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(125)).append("\n");
        report.append(ConsoleUtils.centerText("USAGE METRICS SUMMARY", 125)).append("\n");
        report.append("-".repeat(125)).append("\n");
        report.append(String.format("Total Prescriptions: %d\n", getTotalPrescriptions()));
        report.append(String.format("Dispensed Prescriptions: %d\n", dispensedPrescriptions.getSize()));
        report.append(String.format("Total Medicines in Inventory: %d\n", getTotalMedicines()));
        report.append(String.format("Average Medicines per Prescription: %.1f\n",
                calculateAverageMedicinesPerPrescription()));
        report.append(String.format("Total Revenue from Dispensing: RM %.2f\n", calculateTotalDispensingRevenue()));

        // Most prescribed medicines analysis using arrays
        String[] medicineNames = new String[100];
        String[] medicineIds = new String[100];
        int[] prescriptionCounts = new int[100];
        int[] totalQuantities = new int[100];
        double[] totalRevenue = new double[100];
        int medicineCount = 0;

        Iterator<Prescription> prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getPrescribedMedicines() != null) {
                Iterator<Prescription.PrescribedMedicine> prescribedMedicineIterator = prescription
                        .getPrescribedMedicines().iterator();
                while (prescribedMedicineIterator.hasNext()) {
                    Prescription.PrescribedMedicine prescribedMedicine = prescribedMedicineIterator.next();
                    if (prescribedMedicine != null && prescribedMedicine.getMedicine() != null) {
                        String medicineName = prescribedMedicine.getMedicine().getMedicineName();
                        String medicineId = prescribedMedicine.getMedicine().getMedicineId();

                        // Find if medicine already exists
                        int medicineIndex = -1;
                        for (int medicineCounter = 0; medicineCounter < medicineCount; medicineCounter++) {
                            if (medicineIds[medicineCounter].equals(medicineId)) {
                                medicineIndex = medicineCounter;
                                break;
                            }
                        }

                        if (medicineIndex == -1) {
                            medicineNames[medicineCount] = medicineName;
                            medicineIds[medicineCount] = medicineId;
                            prescriptionCounts[medicineCount] = 1;
                            totalQuantities[medicineCount] = prescribedMedicine.getQuantity();
                            totalRevenue[medicineCount] = prescribedMedicine.getQuantity()
                                    * prescribedMedicine.getMedicine().getUnitPrice();
                            medicineCount++;
                        } else {
                            prescriptionCounts[medicineIndex]++;
                            totalQuantities[medicineIndex] += prescribedMedicine.getQuantity();
                            totalRevenue[medicineIndex] += prescribedMedicine.getQuantity()
                                    * prescribedMedicine.getMedicine().getUnitPrice();
                        }
                    }
                }
            }
        }

        report.append("\nMOST PRESCRIBED MEDICINES:\n");
        int[] topPrescribedIndices = getTopIndices(prescriptionCounts, Math.min(10, medicineCount));
        for (int rankCounter = 0; rankCounter < topPrescribedIndices.length; rankCounter++) {
            int index = topPrescribedIndices[rankCounter];
            report.append(String.format("%d. %-30s: %3d prescriptions, %6d units, RM %8.2f revenue\n",
                    rankCounter + 1, medicineNames[index], prescriptionCounts[index], totalQuantities[index],
                    totalRevenue[index]));
        }

        // Medicine category analysis
        report.append("\nMEDICINE CATEGORY ANALYSIS:\n");
        String[] categories = new String[20];
        int[] categoryCounts = new int[20];
        double[] categoryRevenue = new double[20];
        int categoryCount = 0;

        Iterator<Medicine> medicineIterator = medicineIndexById.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine != null && medicine.getGenericName() != null) {
                String category = getMedicineCategory(medicine.getGenericName());

                // Find if category already exists
                int categoryIndex = -1;
                for (int categoryCounter = 0; categoryCounter < categoryCount; categoryCounter++) {
                    if (categories[categoryCounter].equals(category)) {
                        categoryIndex = categoryCounter;
                        break;
                    }
                }

                if (categoryIndex == -1) {
                    categories[categoryCount] = category;
                    categoryCounts[categoryCount] = 1;
                    categoryRevenue[categoryCount] = medicine.getQuantityInStock() * medicine.getUnitPrice();
                    categoryCount++;
                } else {
                    categoryCounts[categoryIndex]++;
                    categoryRevenue[categoryIndex] += medicine.getQuantityInStock() * medicine.getUnitPrice();
                }
            }
        }

        for (int categoryCounter = 0; categoryCounter < categoryCount; categoryCounter++) {
            report.append(String.format("%-20s: %3d medicines, RM %10.2f inventory value\n",
                    categories[categoryCounter], categoryCounts[categoryCounter], categoryRevenue[categoryCounter]));
        }

        // Monthly prescription trends
        report.append("\nMONTHLY PRESCRIPTION TRENDS:\n");
        int currentYear = LocalDate.now().getYear();
        int[] monthlyPrescriptions = new int[13]; // Index 0 unused, 1-12 for months
        double[] monthlyRevenue = new double[13];

        prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getPrescriptionDate() != null &&
                    prescription.getPrescriptionDate().getYear() == currentYear) {
                int month = prescription.getPrescriptionDate().getMonthValue();
                monthlyPrescriptions[month]++;
                monthlyRevenue[month] += prescription.getTotalCost();
            }
        }

        String[] months = { "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        for (int monthCounter = 1; monthCounter <= 12; monthCounter++) {
            if (monthlyPrescriptions[monthCounter] > 0) {
                report.append(String.format("%-3s: %3d prescriptions, RM %8.2f revenue\n",
                        months[monthCounter], monthlyPrescriptions[monthCounter], monthlyRevenue[monthCounter]));
            }
        }

        // Doctor prescription patterns
        report.append("\nDOCTOR PRESCRIPTION PATTERNS:\n");
        String[] doctorIds = new String[50];
        String[] doctorNames = new String[50];
        int[] doctorPrescriptionCounts = new int[50];
        double[] doctorTotalRevenue = new double[50];
        int doctorCount = 0;

        prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getDoctor() != null) {
                String doctorId = prescription.getDoctor().getDoctorId();
                String doctorName = prescription.getDoctor().getFullName();

                // Find if doctor already exists
                int doctorIndex = -1;
                for (int doctorCounter = 0; doctorCounter < doctorCount; doctorCounter++) {
                    if (doctorIds[doctorCounter].equals(doctorId)) {
                        doctorIndex = doctorCounter;
                        break;
                    }
                }

                if (doctorIndex == -1) {
                    doctorIds[doctorCount] = doctorId;
                    doctorNames[doctorCount] = doctorName;
                    doctorPrescriptionCounts[doctorCount] = 1;
                    doctorTotalRevenue[doctorCount] = prescription.getTotalCost();
                    doctorCount++;
                } else {
                    doctorPrescriptionCounts[doctorIndex]++;
                    doctorTotalRevenue[doctorIndex] += prescription.getTotalCost();
                }
            }
        }

        // Top prescribing doctors
        report.append("\nTOP PRESCRIBING DOCTORS:\n");
        int[] topDoctorIndices = getTopIndices(doctorPrescriptionCounts, Math.min(5, doctorCount));
        for (int rankCounter = 0; rankCounter < topDoctorIndices.length; rankCounter++) {
            int index = topDoctorIndices[rankCounter];
            report.append(String.format("%d. %-30s: %3d prescriptions, RM %8.2f total revenue\n",
                    rankCounter + 1, doctorNames[index], doctorPrescriptionCounts[index], doctorTotalRevenue[index]));
        }

        // Stock turnover analysis
        report.append("\nSTOCK TURNOVER ANALYSIS:\n");
        Iterator<Medicine> stockIterator = medicineIndexById.iterator();
        while (stockIterator.hasNext()) {
            Medicine medicine = stockIterator.next();
            if (medicine != null) {
                double turnoverRate = calculateStockTurnoverRate(medicine);
                if (turnoverRate > 0) {
                    report.append(String.format("%-25s: %.1f%% turnover rate (stock: %d, min: %d)\n",
                            medicine.getMedicineName(), turnoverRate, medicine.getQuantityInStock(),
                            medicine.getMinimumStockLevel()));
                }
            }
        }

        report.append("-".repeat(125)).append("\n\n");

        // Detailed usage table with sorting
        report.append(ConsoleUtils.centerText("DETAILED MEDICINE USAGE", 125)).append("\n");
        report.append("-".repeat(125)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getUsageSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-25s | %-20s | %-15s | %-12s | %-13s | %-13s\n",
                "ID", "Medicine Name", "Generic Name", "Category", "Stock", "Prescriptions", "Revenue"));
        report.append("-".repeat(125)).append("\n");

        // Convert to array for sorting
        Medicine[] medicineArray = new Medicine[medicineIndexById.getSize()];
        int arrayCounter = 0;
        medicineIterator = medicineIndexById.iterator();
        while (medicineIterator.hasNext()) {
            medicineArray[arrayCounter++] = medicineIterator.next();
        }

        // Sort the medicine array
        sortMedicineUsageArray(medicineArray, sortBy, sortOrder);

        // Generate sorted table
        for (Medicine medicine : medicineArray) {
            if (medicine == null)
                continue;
            String id = medicine.getMedicineId() == null ? "-" : medicine.getMedicineId();
            String name = medicine.getMedicineName() == null ? "-" : medicine.getMedicineName();
            String generic = medicine.getGenericName() == null ? "-" : medicine.getGenericName();
            String category = getMedicineCategory(medicine.getGenericName());
            String stock = String.valueOf(medicine.getQuantityInStock());

            // Calculate prescription count and revenue for this medicine
            int prescriptionCount = getPrescriptionCountForMedicine(medicine.getMedicineId());
            double revenue = prescriptionCount * medicine.getUnitPrice();

            // Truncate long names
            if (name.length() > 25)
                name = name.substring(0, 24) + "…";
            if (generic.length() > 20)
                generic = generic.substring(0, 19) + "…";

            report.append(String.format("%-10s | %-25s | %-20s | %-15s | %-12s | %-13s | %-13s\n",
                    id, name, generic, category, stock, String.valueOf(prescriptionCount),
                    String.format("RM %.2f", revenue)));
        }

        report.append("-".repeat(125)).append("\n");
        report.append("*".repeat(125)).append("\n");
        report.append(ConsoleUtils.centerText("END OF MEDICINE USAGE REPORT", 125)).append("\n");
        report.append("=".repeat(125)).append("\n");

        return report.toString();
    }

    // Helper methods for sorting
    private String getSortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> "Medicine Name";
            case "generic" -> "Generic Name";
            case "stock" -> "Stock Quantity";
            case "price" -> "Unit Price";
            case "expiry" -> "Expiry Date";
            case "status" -> "Status";
            case "date" -> "Prescription Date";
            case "patient" -> "Patient Name";
            case "doctor" -> "Doctor Name";
            case "total" -> "Total Cost";
            case "cost" -> "Total Cost";
            case "id" -> "ID";
            default -> "Default";
        };
    }

    private void sortMedicineArray(Medicine[] medicineArray, String sortBy, String sortOrder) {
        if (medicineArray == null || medicineArray.length < 2)
            return;

        Comparator<Medicine> comparator = getMedicineComparator(sortBy);

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(medicineArray, comparator);
    }

    private void sortPrescriptionArray(Prescription[] prescriptionArray, String sortBy, String sortOrder) {
        if (prescriptionArray == null || prescriptionArray.length < 2)
            return;

        Comparator<Prescription> comparator = getPrescriptionComparator(sortBy);

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(prescriptionArray, comparator);
    }

    private Comparator<Medicine> getMedicineComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" ->
                Comparator.comparing(medicine -> medicine.getMedicineName() != null ? medicine.getMedicineName() : "");
            case "generic" ->
                Comparator.comparing(medicine -> medicine.getGenericName() != null ? medicine.getGenericName() : "");
            case "stock" -> Comparator.comparing(Medicine::getQuantityInStock);
            case "price" -> Comparator.comparing(Medicine::getUnitPrice);
            case "expiry" -> Comparator
                    .comparing(medicine -> medicine.getExpiryDate() != null ? medicine.getExpiryDate() : LocalDate.MAX);
            case "status" ->
                Comparator.comparing(medicine -> medicine.getStatus() != null ? medicine.getStatus().toString() : "");
            case "id" ->
                Comparator.comparing(medicine -> medicine.getMedicineId() != null ? medicine.getMedicineId() : "");
            default ->
                Comparator.comparing(medicine -> medicine.getMedicineName() != null ? medicine.getMedicineName() : "");
        };
    }

    private Comparator<Prescription> getPrescriptionComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "date" -> Comparator
                    .comparing(prescription -> prescription.getPrescriptionDate() != null
                            ? prescription.getPrescriptionDate()
                            : LocalDate.MAX);
            case "patient" -> Comparator.comparing(
                    prescription -> prescription.getPatient() != null ? prescription.getPatient().getFullName() : "");
            case "doctor" -> Comparator.comparing(
                    prescription -> prescription.getDoctor() != null ? prescription.getDoctor().getFullName() : "");
            case "total" -> Comparator.comparing(Prescription::getTotalCost);
            case "cost" -> Comparator.comparing(Prescription::getTotalCost);
            case "status" -> Comparator.comparing(
                    prescription -> prescription.getStatus() != null ? prescription.getStatus().toString() : "");
            case "payment" -> Comparator
                    .comparing(prescription -> prescription.getPaymentStatus() != null
                            ? prescription.getPaymentStatus().toString()
                            : "");
            case "id" -> Comparator.comparing(
                    prescription -> prescription.getPrescriptionId() != null ? prescription.getPrescriptionId() : "");
            default -> Comparator
                    .comparing(prescription -> prescription.getPrescriptionDate() != null
                            ? prescription.getPrescriptionDate()
                            : LocalDate.MAX);
        };
    }

    // Helper methods for usage report
    private double calculateAverageMedicinesPerPrescription() {
        int totalMedicines = 0;
        int prescriptionCount = 0;

        Iterator<Prescription> prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getPrescribedMedicines() != null) {
                totalMedicines += prescription.getPrescribedMedicines().getSize();
                prescriptionCount++;
            }
        }

        return prescriptionCount > 0 ? (double) totalMedicines / prescriptionCount : 0.0;
    }

    private double calculateTotalDispensingRevenue() {
        double totalRevenue = 0.0;

        Iterator<Prescription> prescriptionIterator = dispensedPrescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null) {
                totalRevenue += prescription.getTotalCost();
            }
        }

        return totalRevenue;
    }

    private String getMedicineCategory(String genericName) {
        if (genericName == null)
            return "Unknown";

        String generic = genericName.toLowerCase();
        if (generic.contains("antibiotic") || generic.contains("penicillin") || generic.contains("amoxicillin")) {
            return "Antibiotics";
        } else if (generic.contains("pain") || generic.contains("paracetamol") || generic.contains("ibuprofen")) {
            return "Pain Relief";
        } else if (generic.contains("vitamin") || generic.contains("supplement")) {
            return "Vitamins/Supplements";
        } else if (generic.contains("cough") || generic.contains("cold")) {
            return "Cold/Cough";
        } else if (generic.contains("fever") || generic.contains("temperature")) {
            return "Fever Management";
        } else if (generic.contains("allergy") || generic.contains("antihistamine")) {
            return "Allergy";
        } else {
            return "Other";
        }
    }

    private double calculateStockTurnoverRate(Medicine medicine) {
        // Simulate stock turnover rate based on prescription frequency
        int prescriptionCount = getPrescriptionCountForMedicine(medicine.getMedicineId());
        int stockLevel = medicine.getQuantityInStock();

        if (stockLevel == 0)
            return 0.0;

        // Calculate turnover as (prescriptions * avg quantity) / stock level * 100
        double avgQuantity = 2.0; // Assume average 2 units per prescription
        return Math.min(100.0, (prescriptionCount * avgQuantity / stockLevel) * 100);
    }

    private int getPrescriptionCountForMedicine(String medicineId) {
        int prescriptionCount = 0;

        Iterator<Prescription> prescriptionIterator = prescriptionIndexById.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getPrescribedMedicines() != null) {
                Iterator<Prescription.PrescribedMedicine> prescribedMedicineIterator = prescription
                        .getPrescribedMedicines().iterator();
                while (prescribedMedicineIterator.hasNext()) {
                    Prescription.PrescribedMedicine prescribedMedicine = prescribedMedicineIterator.next();
                    if (prescribedMedicine != null && prescribedMedicine.getMedicine() != null &&
                            prescribedMedicine.getMedicine().getMedicineId().equals(medicineId)) {
                        prescriptionCount++;
                    }
                }
            }
        }

        return prescriptionCount;
    }

    private int[] getTopIndices(int[] values, int count) {
        int[] indices = new int[Math.min(count, values.length)];
        int[] tempValues = values.clone();
        for (int rankCounter = 0; rankCounter < indices.length; rankCounter++) {
            int maxIndex = 0;
            for (int searchCounter = 1; searchCounter < tempValues.length; searchCounter++) {
                if (tempValues[searchCounter] > tempValues[maxIndex]) {
                    maxIndex = searchCounter;
                }
            }
            indices[rankCounter] = maxIndex;
            tempValues[maxIndex] = -1; // Mark as used
        }
        return indices;
    }

    private String getUsageSortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> "Medicine Name";
            case "generic" -> "Generic Name";
            case "category" -> "Category";
            case "stock" -> "Stock Level";
            case "prescriptions" -> "Prescription Count";
            case "revenue" -> "Revenue";
            case "id" -> "ID";
            default -> "Default";
        };
    }

    private void sortMedicineUsageArray(Medicine[] medicineArray, String sortBy, String sortOrder) {
        if (medicineArray == null || medicineArray.length < 2)
            return;

        Comparator<Medicine> comparator = getMedicineUsageComparator(sortBy);

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(medicineArray, comparator);
    }

    private Comparator<Medicine> getMedicineUsageComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" ->
                Comparator.comparing(medicine -> medicine.getMedicineName() != null ? medicine.getMedicineName() : "");
            case "generic" ->
                Comparator.comparing(medicine -> medicine.getGenericName() != null ? medicine.getGenericName() : "");
            case "category" -> Comparator.comparing(medicine -> getMedicineCategory(medicine.getGenericName()));
            case "stock" -> Comparator.comparing(Medicine::getQuantityInStock);
            case "prescriptions" ->
                Comparator.comparing(medicine -> getPrescriptionCountForMedicine(medicine.getMedicineId()));
            case "revenue" -> Comparator.comparing(
                    medicine -> getPrescriptionCountForMedicine(medicine.getMedicineId()) * medicine.getUnitPrice());
            case "id" ->
                Comparator.comparing(medicine -> medicine.getMedicineId() != null ? medicine.getMedicineId() : "");
            default ->
                Comparator.comparing(medicine -> medicine.getMedicineName() != null ? medicine.getMedicineName() : "");
        };
    }
}