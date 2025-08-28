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
        loadPhramacyData();
    }

    public void loadPhramacyData() {
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

    public ArrayBucketList<String, Prescription> findPrescriptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        ArrayBucketList<String, Prescription> dateRangePrescriptions = new ArrayBucketList<>();
        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate().isAfter(startDate)
                    && prescription.getPrescriptionDate().isBefore(endDate)) {
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
        int prescYearCount = 0;

        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate() != null) {
                int year = prescription.getPrescriptionDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int index = 0; index < prescYearCount; index++) {
                    if (prescriptionYears[index] == year) {
                        yearIndex = index;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    prescriptionYears[prescYearCount] = year;
                    prescriptionsByYear[prescYearCount] = 1;
                    revenueByYear[prescYearCount] = prescription.getTotalCost();
                    prescYearCount++;
                } else {
                    // Update existing year data
                    prescriptionsByYear[yearIndex]++;
                    revenueByYear[yearIndex] += prescription.getTotalCost();
                }
            }
        }

        report.append("\nPRESCRIPTIONS BY YEAR:\n");
        // Sort prescription years in descending order using QuickSort
        if (prescYearCount > 1) {
            for (int index = 0; index < prescYearCount - 1; index++) {
                for (int innerIndex = index + 1; innerIndex < prescYearCount; innerIndex++) {
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

        for (int index = 0; index < prescYearCount; index++) {
            report.append(String.format("Year %d: %,6d prescriptions (RM %,12.2f revenue)\n",
                    prescriptionYears[index], prescriptionsByYear[index], revenueByYear[index]));
        }

        // Status distribution using arrays
        String[] prescStatuses = new String[10]; // Assuming max 10 different statuses
        int[] prescStatusCounts = new int[10];
        int prescStatusCount = 0;

        prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            String status = prescription.getStatus() != null ? prescription.getStatus().toString() : "UNKNOWN";

            // Find if status already exists
            int statusIndex = -1;
            for (int index = 0; index < prescStatusCount; index++) {
                if (prescStatuses[index].equals(status)) {
                    statusIndex = index;
                    break;
                }
            }

            // If status doesn't exist, add new entry
            if (statusIndex == -1) {
                prescStatuses[prescStatusCount] = status;
                prescStatusCounts[prescStatusCount] = 1;
                prescStatusCount++;
            } else {
                // Update existing status count
                prescStatusCounts[statusIndex]++;
            }
        }

        report.append("\nPRESCRIPTION STATUS DISTRIBUTION:\n");
        for (int index = 0; index < prescStatusCount; index++) {
            report.append(String.format("%-15s: %d prescriptions\n", prescStatuses[index], prescStatusCounts[index]));
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
            case "id" -> Comparator.comparing(p -> p.getPrescriptionId() != null ? p.getPrescriptionId() : "");
            default -> Comparator
                    .comparing(p -> p.getPrescriptionDate() != null ? p.getPrescriptionDate() : LocalDate.MAX);
        };
    }
}