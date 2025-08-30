package control;

import adt.ArrayBucketList;
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

    private ArrayBucketList<String, Medicine> medicines;
    private ArrayBucketList<String, Prescription> prescriptions;
    private ArrayBucketList<String, Prescription> dispensedPrescriptions;
    private final MedicineDao medicineDao;
    private final PrescriptionDao prescriptionDao;
    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final ConsultationDao consultationDao;

    public PharmacyManagementControl() {
        this.medicines = new ArrayBucketList<>();
        this.prescriptions = new ArrayBucketList<>();
        this.dispensedPrescriptions = new ArrayBucketList<>();
        this.medicineDao = new MedicineDao();
        this.prescriptionDao = new PrescriptionDao();
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        this.consultationDao = new ConsultationDao();
    }

    public void loadPharmacyData() {
        try {
            medicines = medicineDao.findAll();
            prescriptions = prescriptionDao.findAll();
            Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
            while (prescriptionIterator.hasNext()) {
                Prescription prescription = prescriptionIterator.next();
                if (prescription.getStatus() == Prescription.PrescriptionStatus.DISPENSED) {
                    dispensedPrescriptions.add(prescription.getPrescriptionId(), prescription);
                }
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
            medicines.add(medicine.getMedicineId(), medicine);

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
            medicines.add(medicine.getMedicineId(), medicine);
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
                medicines.add(medicine.getMedicineId(), medicine);
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
            prescriptions.add(prescription.getPrescriptionId(), prescription);

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
                prescriptions.add(prescription.getPrescriptionId(), prescription);
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
                prescriptions.add(prescription.getPrescriptionId(), prescription);
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
            prescriptions.add(prescription.getPrescriptionId(), prescription);
            if (prescription.getStatus() == Prescription.PrescriptionStatus.DISPENSED) {
                dispensedPrescriptions.add(prescription.getPrescriptionId(), prescription);
            }

            if (oldStatus == Prescription.PrescriptionStatus.DISPENSED
                    && prescription.getStatus() != Prescription.PrescriptionStatus.DISPENSED) {
                dispensedPrescriptions.remove(prescription.getPrescriptionId());
            }

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
                    prescriptions.add(prescription.getPrescriptionId(), prescription);
                    medicines.add(medicine.getMedicineId(), medicine);
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
                prescriptions.add(prescription.getPrescriptionId(), prescription);
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
        return medicines.getValue(medicineId);
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

    public ArrayBucketList<String, Medicine> findMedicineByManufacturer(String manufacturer) {
        ArrayBucketList<String, Medicine> manufacturerMedicines = new ArrayBucketList<>();
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getManufacturer().equalsIgnoreCase(manufacturer)) {
                manufacturerMedicines.add(medicine.getMedicineId(), medicine);
            }
        }
        return manufacturerMedicines;
    }

    public ArrayBucketList<String, Medicine> findMedicineByStatus(int statusChoice) {
        ArrayBucketList<String, Medicine> statusMedicines = new ArrayBucketList<>();
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getStatus().equals(Medicine.MedicineStatus.values()[statusChoice - 1])) {
                statusMedicines.add(medicine.getMedicineId(), medicine);
            }
        }
        return statusMedicines;
    }

    public ArrayBucketList<String, Medicine> getLowStockMedicines() {
        ArrayBucketList<String, Medicine> lowStockMedicines = new ArrayBucketList<>();
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.isLowStock()) {
                lowStockMedicines.add(medicine.getMedicineId(), medicine);
            }
        }
        return lowStockMedicines;
    }

    public ArrayBucketList<String, Medicine> getExpiredMedicines() {
        ArrayBucketList<String, Medicine> expiredMedicines = new ArrayBucketList<>();
        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.isExpired()) {
                expiredMedicines.add(medicine.getMedicineId(), medicine);
            }
        }
        return expiredMedicines;
    }

    public Prescription findPrescriptionById(String prescriptionId) {
        return prescriptions.getValue(prescriptionId);
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByPatient(String patientId) {
        ArrayBucketList<String, Prescription> patientPrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPatient().getPatientId().equals(patientId)) {
                patientPrescriptions.add(prescription.getPrescriptionId(), prescription);
            }
        }

        return patientPrescriptions;
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByDoctor(String doctorId) {
        ArrayBucketList<String, Prescription> doctorPrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getDoctor().getDoctorId().equals(doctorId)) {
                doctorPrescriptions.add(prescription.getPrescriptionId(), prescription);
            }
        }
        return doctorPrescriptions;
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByStatus(int statusChoice) {
        ArrayBucketList<String, Prescription> statusPrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getStatus().equals(Prescription.PrescriptionStatus.values()[statusChoice - 1])) {
                statusPrescriptions.add(prescription.getPrescriptionId(), prescription);
            }
        }
        return statusPrescriptions;
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByPaymentStatus(
            Prescription.PaymentStatus paymentStatus) {
        ArrayBucketList<String, Prescription> results = new ArrayBucketList<>();
        if (paymentStatus == null) {
            return results;
        }
        Iterator<Prescription> iterator = prescriptions.iterator();
        while (iterator.hasNext()) {
            Prescription p = iterator.next();
            if (p.getPaymentStatus() == paymentStatus) {
                results.add(p.getPrescriptionId(), p);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Prescription> findPrescriptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        ArrayBucketList<String, Prescription> dateRangePrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate().isAfter(startDate.minusDays(1))
                    && prescription.getPrescriptionDate().isBefore(endDate.plusDays(1))) {
                dateRangePrescriptions.add(prescription.getPrescriptionId(), prescription);
            }
        }
        return dateRangePrescriptions;
    }

    public ArrayBucketList<String, Prescription> getActivePrescriptions() {
        ArrayBucketList<String, Prescription> activePrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getStatus() == Prescription.PrescriptionStatus.ACTIVE) {
                activePrescriptions.add(prescription.getPrescriptionId(), prescription);
            }
        }
        return activePrescriptions;
    }

    public ArrayBucketList<String, Medicine> getAllMedicines() {
        return medicines;
    }

    public ArrayBucketList<String, Prescription> getAllPrescriptions() {
        return prescriptions;
    }

    public int getTotalMedicines() {
        return medicines.getSize();
    }

    public int getTotalPrescriptions() {
        return prescriptions.getSize();
    }

    // Sorted display for search results (Medicines)
    public String displaySortedMedicineSearchResults(ArrayBucketList<String, Medicine> list, String searchCriteria,
            String sortBy, String sortOrder) {
        if (list == null || list.isEmpty()) {
            return "No medicines found.";
        }

        // Copy to array
        Medicine[] items = new Medicine[list.getSize()];
        int pos = 0;
        Iterator<Medicine> it = list.iterator();
        while (it.hasNext() && pos < items.length) {
            items[pos++] = it.next();
        }

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
            String gen = medicine.getGenericName() == null ? "-" : medicine.getGenericName();
            String status = medicine.getStatus() == null ? "-" : medicine.getStatus().toString();
            String expiry = medicine.getExpiryDate() == null ? "-"
                    : medicine.getExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            if (name.length() > 20)
                name = name.substring(0, 19) + "…";
            if (gen.length() > 16)
                gen = gen.substring(0, 15) + "…";
            stringBuilder.append(String.format("%-10s | %-20s | %-16s | %,6d | %-10s | RM %,10.2f | %-10s\n", id, name, gen,
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

        Prescription[] items = new Prescription[list.getSize()];
        int pos = 0;
        Iterator<Prescription> it = list.iterator();
        while (it.hasNext() && pos < items.length) {
            items[pos++] = it.next();
        }

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

        Prescription[] items = new Prescription[activePrescriptions.getSize()];
        int pos = 0;
        Iterator<Prescription> it = activePrescriptions.iterator();
        while (it.hasNext() && pos < items.length) {
            items[pos++] = it.next();
        }

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

        int[] years = new int[10];
        int[] stockByYear = new int[10];
        double[] valueByYear = new double[10];
        int yearCount = 0;

        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine.getExpiryDate() != null) {
                int year = medicine.getExpiryDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int index = 0; index < yearCount; index++) {
                    if (years[index] == year) {
                        yearIndex = index;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    years[yearCount] = year;
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
            for (int index = 0; index < yearCount - 1; index++) {
                for (int innerIndex = index + 1; innerIndex < yearCount; innerIndex++) {
                    if (years[index] < years[innerIndex]) {
                        // Swap years
                        int tempYear = years[index];
                        years[index] = years[innerIndex];
                        years[innerIndex] = tempYear;

                        // Swap stock quantities
                        int tempStock = stockByYear[index];
                        stockByYear[index] = stockByYear[innerIndex];
                        stockByYear[innerIndex] = tempStock;

                        // Swap values
                        double tempValue = valueByYear[index];
                        valueByYear[index] = valueByYear[innerIndex];
                        valueByYear[innerIndex] = tempValue;
                    }
                }
            }
        }

        for (int index = 0; index < yearCount; index++) {
            report.append(String.format("Year %d: %,6d units (RM %,12.2f value)\n",
                    years[index], stockByYear[index], valueByYear[index]));
        }

        // Category analysis using arrays
        String[] statuses = new String[10]; // Assuming max 10 different statuses
        int[] statusCounts = new int[10];
        int statusCount = 0;

        medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            String status = medicine.getStatus() != null ? medicine.getStatus().toString() : "UNKNOWN";

            // Find if status already exists
            int statusIndex = -1;
            for (int index = 0; index < statusCount; index++) {
                if (statuses[index].equals(status)) {
                    statusIndex = index;
                    break;
                }
            }

            // If status doesn't exist, add new entry
            if (statusIndex == -1) {
                statuses[statusCount] = status;
                statusCounts[statusCount] = 1;
                statusCount++;
            } else {
                // Update existing status count
                statusCounts[statusIndex]++;
            }
        }

        report.append("\nCATEGORY ANALYSIS:\n");
        for (int index = 0; index < statusCount; index++) {
            report.append(String.format("%-15s: %d medicines\n", statuses[index], statusCounts[index]));
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
        Medicine[] medicineArray = new Medicine[medicines.getSize()];
        int index = 0;
        medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            medicineArray[index++] = medicineIterator.next();
        }

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

        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate() != null) {
                int year = prescription.getPrescriptionDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int index = 0; index < prescriptionYearCount; index++) {
                    if (prescriptionYears[index] == year) {
                        yearIndex = index;
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
            for (int index = 0; index < prescriptionYearCount - 1; index++) {
                for (int innerIndex = index + 1; innerIndex < prescriptionYearCount; innerIndex++) {
                    if (prescriptionYears[index] < prescriptionYears[innerIndex]) {
                        // Swap years
                        int tempYear = prescriptionYears[index];
                        prescriptionYears[index] = prescriptionYears[innerIndex];
                        prescriptionYears[innerIndex] = tempYear;

                        // Swap counts
                        int tempCount = prescriptionsByYear[index];
                        prescriptionsByYear[index] = prescriptionsByYear[innerIndex];
                        prescriptionsByYear[innerIndex] = tempCount;

                        // Swap revenues
                        double tempRevenue = revenueByYear[index];
                        revenueByYear[index] = revenueByYear[innerIndex];
                        revenueByYear[innerIndex] = tempRevenue;
                    }
                }
            }
        }

        for (int index = 0; index < prescriptionYearCount; index++) {
            report.append(String.format("Year %d: %,6d prescriptions (RM %,12.2f revenue)\n",
                    prescriptionYears[index], prescriptionsByYear[index], revenueByYear[index]));
        }

        // Status distribution using arrays
        String[] prescriptionStatuses = new String[10]; // Assuming max 10 different statuses
        int[] prescriptionStatusCounts = new int[10];
        int prescriptionStatusCount = 0;

        prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            String status = prescription.getStatus() != null ? prescription.getStatus().toString() : "UNKNOWN";

            // Find if status already exists
            int statusIndex = -1;
            for (int index = 0; index < prescriptionStatusCount; index++) {
                if (prescriptionStatuses[index].equals(status)) {
                    statusIndex = index;
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
        for (int index = 0; index < prescriptionStatusCount; index++) {
            report.append(String.format("%-15s: %d prescriptions\n", prescriptionStatuses[index], prescriptionStatusCounts[index]));
        }

        // Monthly trend for current year using arrays
        int currentYear = LocalDate.now().getYear();
        int[] monthlyTrend = new int[13]; // Index 0 unused, 1-12 for months

        prescriptionIterator = prescriptions.iterator();
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
        for (int index = 1; index <= 12; index++) {
            if (monthlyTrend[index] > 0) {
                report.append(String.format("%-3s: %d prescriptions\n", months[index], monthlyTrend[index]));
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
        Prescription[] prescriptionArray = new Prescription[prescriptions.getSize()];
        int index = 0;
        prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            prescriptionArray[index++] = prescriptionIterator.next();
        }

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
     * Generates a medicine usage report analyzing prescription patterns, most prescribed medicines, and usage trends
     * @param sortBy field to sort by
     * @param sortOrder sort order (asc/desc)
     * @return formatted report string
     */
    public String generateMedicineUsageReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("PHARMACY MANAGEMENT SYSTEM - MEDICINE USAGE REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("USAGE METRICS SUMMARY", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("Total Prescriptions: %d\n", getTotalPrescriptions()));
        report.append(String.format("Dispensed Prescriptions: %d\n", dispensedPrescriptions.getSize()));
        report.append(String.format("Total Medicines in Inventory: %d\n", getTotalMedicines()));
        report.append(String.format("Average Medicines per Prescription: %.1f\n", calculateAverageMedicinesPerPrescription()));
        report.append(String.format("Total Revenue from Dispensing: RM %.2f\n", calculateTotalDispensingRevenue()));

        // Most prescribed medicines analysis using arrays
        String[] medicineNames = new String[100];
        String[] medicineIds = new String[100];
        int[] prescriptionCounts = new int[100];
        int[] totalQuantities = new int[100];
        double[] totalRevenue = new double[100];
        int medicineCount = 0;

        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getPrescribedMedicines() != null) {
                Iterator<Prescription.PrescribedMedicine> prescribedMedicineIterator = prescription.getPrescribedMedicines().iterator();
                while (prescribedMedicineIterator.hasNext()) {
                    Prescription.PrescribedMedicine prescribedMedicine = prescribedMedicineIterator.next();
                    if (prescribedMedicine != null && prescribedMedicine.getMedicine() != null) {
                        String medicineName = prescribedMedicine.getMedicine().getMedicineName();
                        String medicineId = prescribedMedicine.getMedicine().getMedicineId();
                        
                        // Find if medicine already exists
                        int medicineIndex = -1;
                        for (int i = 0; i < medicineCount; i++) {
                            if (medicineIds[i].equals(medicineId)) {
                                medicineIndex = i;
                                break;
                            }
                        }
                        
                        if (medicineIndex == -1) {
                            medicineNames[medicineCount] = medicineName;
                            medicineIds[medicineCount] = medicineId;
                            prescriptionCounts[medicineCount] = 1;
                            totalQuantities[medicineCount] = prescribedMedicine.getQuantity();
                            totalRevenue[medicineCount] = prescribedMedicine.getQuantity() * prescribedMedicine.getMedicine().getUnitPrice();
                            medicineCount++;
                        } else {
                            prescriptionCounts[medicineIndex]++;
                            totalQuantities[medicineIndex] += prescribedMedicine.getQuantity();
                            totalRevenue[medicineIndex] += prescribedMedicine.getQuantity() * prescribedMedicine.getMedicine().getUnitPrice();
                        }
                    }
                }
            }
        }

        report.append("\nMOST PRESCRIBED MEDICINES:\n");
        int[] topPrescribedIndices = getTopIndices(prescriptionCounts, Math.min(10, medicineCount));
        for (int i = 0; i < topPrescribedIndices.length; i++) {
            int index = topPrescribedIndices[i];
            report.append(String.format("%d. %-30s: %3d prescriptions, %6d units, RM %8.2f revenue\n",
                    i + 1, medicineNames[index], prescriptionCounts[index], totalQuantities[index], totalRevenue[index]));
        }

        // Medicine category analysis
        report.append("\nMEDICINE CATEGORY ANALYSIS:\n");
        String[] categories = new String[20];
        int[] categoryCounts = new int[20];
        double[] categoryRevenue = new double[20];
        int categoryCount = 0;

        Iterator<Medicine> medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
            if (medicine != null && medicine.getGenericName() != null) {
                String category = getMedicineCategory(medicine.getGenericName());
                
                // Find if category already exists
                int categoryIndex = -1;
                for (int i = 0; i < categoryCount; i++) {
                    if (categories[i].equals(category)) {
                        categoryIndex = i;
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

        for (int i = 0; i < categoryCount; i++) {
            report.append(String.format("%-20s: %3d medicines, RM %10.2f inventory value\n",
                    categories[i], categoryCounts[i], categoryRevenue[i]));
        }

        // Monthly prescription trends
        report.append("\nMONTHLY PRESCRIPTION TRENDS:\n");
        int currentYear = LocalDate.now().getYear();
        int[] monthlyPrescriptions = new int[13]; // Index 0 unused, 1-12 for months
        double[] monthlyRevenue = new double[13];

        prescriptionIterator = prescriptions.iterator();
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
        for (int i = 1; i <= 12; i++) {
            if (monthlyPrescriptions[i] > 0) {
                report.append(String.format("%-3s: %3d prescriptions, RM %8.2f revenue\n",
                        months[i], monthlyPrescriptions[i], monthlyRevenue[i]));
            }
        }

        // Doctor prescription patterns
        report.append("\nDOCTOR PRESCRIPTION PATTERNS:\n");
        String[] doctorIds = new String[50];
        String[] doctorNames = new String[50];
        int[] doctorPrescriptionCounts = new int[50];
        double[] doctorTotalRevenue = new double[50];
        int doctorCount = 0;

        prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getDoctor() != null) {
                String doctorId = prescription.getDoctor().getDoctorId();
                String doctorName = prescription.getDoctor().getFullName();
                
                // Find if doctor already exists
                int doctorIndex = -1;
                for (int i = 0; i < doctorCount; i++) {
                    if (doctorIds[i].equals(doctorId)) {
                        doctorIndex = i;
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
        for (int i = 0; i < topDoctorIndices.length; i++) {
            int index = topDoctorIndices[i];
            report.append(String.format("%d. %-30s: %3d prescriptions, RM %8.2f total revenue\n",
                    i + 1, doctorNames[index], doctorPrescriptionCounts[index], doctorTotalRevenue[index]));
        }

        // Stock turnover analysis
        report.append("\nSTOCK TURNOVER ANALYSIS:\n");
        Iterator<Medicine> stockIterator = medicines.iterator();
        while (stockIterator.hasNext()) {
            Medicine medicine = stockIterator.next();
            if (medicine != null) {
                double turnoverRate = calculateStockTurnoverRate(medicine);
                if (turnoverRate > 0) {
                    report.append(String.format("%-25s: %.1f%% turnover rate (stock: %d, min: %d)\n",
                            medicine.getMedicineName(), turnoverRate, medicine.getQuantityInStock(), medicine.getMinimumStockLevel()));
                }
            }
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed usage table with sorting
        report.append(ConsoleUtils.centerText("DETAILED MEDICINE USAGE", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getUsageSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-25s | %-20s | %-15s | %-12s | %-12s | %-12s\n",
                "ID", "Medicine Name", "Generic Name", "Category", "Stock", "Prescriptions", "Revenue"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        Medicine[] medicineArray = new Medicine[medicines.getSize()];
        int index = 0;
        medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            medicineArray[index++] = medicineIterator.next();
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

            report.append(String.format("%-10s | %-25s | %-20s | %-15s | %-12s | %-12s | %-12s\n",
                    id, name, generic, category, stock, String.valueOf(prescriptionCount), 
                    String.format("RM %.2f", revenue)));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF MEDICINE USAGE REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

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
            case "name" -> Comparator.comparing(m -> m.getMedicineName() != null ? m.getMedicineName() : "");
            case "generic" -> Comparator.comparing(m -> m.getGenericName() != null ? m.getGenericName() : "");
            case "stock" -> Comparator.comparing(Medicine::getQuantityInStock);
            case "price" -> Comparator.comparing(Medicine::getUnitPrice);
            case "expiry" -> Comparator.comparing(m -> m.getExpiryDate() != null ? m.getExpiryDate() : LocalDate.MAX);
            case "status" -> Comparator.comparing(m -> m.getStatus() != null ? m.getStatus().toString() : "");
            case "id" -> Comparator.comparing(m -> m.getMedicineId() != null ? m.getMedicineId() : "");
            default -> Comparator.comparing(m -> m.getMedicineName() != null ? m.getMedicineName() : "");
        };
    }

    private Comparator<Prescription> getPrescriptionComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "date" -> Comparator
                    .comparing(p -> p.getPrescriptionDate() != null ? p.getPrescriptionDate() : LocalDate.MAX);
            case "patient" -> Comparator.comparing(p -> p.getPatient() != null ? p.getPatient().getFullName() : "");
            case "doctor" -> Comparator.comparing(p -> p.getDoctor() != null ? p.getDoctor().getFullName() : "");
            case "total" -> Comparator.comparing(Prescription::getTotalCost);
            case "cost" -> Comparator.comparing(Prescription::getTotalCost);
            case "status" -> Comparator.comparing(p -> p.getStatus() != null ? p.getStatus().toString() : "");
            case "payment" -> Comparator
                    .comparing(p -> p.getPaymentStatus() != null ? p.getPaymentStatus().toString() : "");
            case "id" -> Comparator.comparing(p -> p.getPrescriptionId() != null ? p.getPrescriptionId() : "");
            default -> Comparator
                    .comparing(p -> p.getPrescriptionDate() != null ? p.getPrescriptionDate() : LocalDate.MAX);
        };
    }

    // Helper methods for usage report
    private double calculateAverageMedicinesPerPrescription() {
        int totalMedicines = 0;
        int prescriptionCount = 0;
        
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
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
        if (genericName == null) return "Unknown";
        
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
        
        if (stockLevel == 0) return 0.0;
        
        // Calculate turnover as (prescriptions * avg quantity) / stock level * 100
        double avgQuantity = 2.0; // Assume average 2 units per prescription
        return Math.min(100.0, (prescriptionCount * avgQuantity / stockLevel) * 100);
    }

    private int getPrescriptionCountForMedicine(String medicineId) {
        int count = 0;
        
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription != null && prescription.getPrescribedMedicines() != null) {
                Iterator<Prescription.PrescribedMedicine> prescribedMedicineIterator = prescription.getPrescribedMedicines().iterator();
                while (prescribedMedicineIterator.hasNext()) {
                    Prescription.PrescribedMedicine prescribedMedicine = prescribedMedicineIterator.next();
                    if (prescribedMedicine != null && prescribedMedicine.getMedicine() != null &&
                            prescribedMedicine.getMedicine().getMedicineId().equals(medicineId)) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }

    private int[] getTopIndices(int[] values, int count) {
        int[] indices = new int[Math.min(count, values.length)];
        int[] tempValues = values.clone();
        for (int i = 0; i < indices.length; i++) {
            int maxIndex = 0;
            for (int j = 1; j < tempValues.length; j++) {
                if (tempValues[j] > tempValues[maxIndex]) {
                    maxIndex = j;
                }
            }
            indices[i] = maxIndex;
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
            case "name" -> Comparator.comparing(m -> m.getMedicineName() != null ? m.getMedicineName() : "");
            case "generic" -> Comparator.comparing(m -> m.getGenericName() != null ? m.getGenericName() : "");
            case "category" -> Comparator.comparing(m -> getMedicineCategory(m.getGenericName()));
            case "stock" -> Comparator.comparing(Medicine::getQuantityInStock);
            case "prescriptions" -> Comparator.comparing(m -> getPrescriptionCountForMedicine(m.getMedicineId()));
            case "revenue" -> Comparator.comparing(m -> getPrescriptionCountForMedicine(m.getMedicineId()) * m.getUnitPrice());
            case "id" -> Comparator.comparing(m -> m.getMedicineId() != null ? m.getMedicineId() : "");
            default -> Comparator.comparing(m -> m.getMedicineName() != null ? m.getMedicineName() : "");
        };
    }
}