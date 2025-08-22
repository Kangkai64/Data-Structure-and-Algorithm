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
    private MedicineDao medicineDao;
    private PrescriptionDao prescriptionDao;
    private PatientDao patientDao;
    private DoctorDao doctorDao;
    private ConsultationDao consultationDao;

    public PharmacyManagementControl() {
        this.medicines = new ArrayBucketList<String, Medicine>();
        this.prescriptions = new ArrayBucketList<String, Prescription>();
        this.dispensedPrescriptions = new ArrayBucketList<String, Prescription>();
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
                return removed;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error removing medicine from prescription: " + exception.getMessage());
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
        ArrayBucketList<String, Medicine> manufacturerMedicines = new ArrayBucketList<String, Medicine>();
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
        ArrayBucketList<String, Medicine> statusMedicines = new ArrayBucketList<String, Medicine>();
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
        ArrayBucketList<String, Medicine> lowStockMedicines = new ArrayBucketList<String, Medicine>();
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
        ArrayBucketList<String, Medicine> expiredMedicines = new ArrayBucketList<String, Medicine>();
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
        ArrayBucketList<String, Prescription> patientPrescriptions = new ArrayBucketList<String, Prescription>();
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
        ArrayBucketList<String, Prescription> doctorPrescriptions = new ArrayBucketList<String, Prescription>();
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
        ArrayBucketList<String, Prescription> statusPrescriptions = new ArrayBucketList<String, Prescription>();
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
        ArrayBucketList<String, Prescription> dateRangePrescriptions = new ArrayBucketList<String, Prescription>();
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
        ArrayBucketList<String, Prescription> activePrescriptions = new ArrayBucketList<String, Prescription>();
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
    public String generateMedicineStockReport() {
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
                for (int i = 0; i < yearCount; i++) {
                    if (years[i] == year) {
                        yearIndex = i;
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
        for (int i = 0; i < yearCount; i++) {
            report.append(String.format("Year %d: %,6d units (RM %,12.2f value)\n",
                    years[i], stockByYear[i], valueByYear[i]));
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
            for (int i = 0; i < statusCount; i++) {
                if (statuses[i].equals(status)) {
                    statusIndex = i;
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
        for (int i = 0; i < statusCount; i++) {
            report.append(String.format("%-15s: %d medicines\n", statuses[i], statusCounts[i]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed medicine table
        report.append(ConsoleUtils.centerText("DETAILED MEDICINE INVENTORY", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("%-10s | %-20s | %-16s | %6s | %6s | %-10s | %12s | %-10s\n",
                "ID", "Name", "Generic", "Stock", "Min", "Status", "Price", "Expiry"));
        report.append("-".repeat(120)).append("\n");

        medicineIterator = medicines.iterator();
        while (medicineIterator.hasNext()) {
            Medicine medicine = medicineIterator.next();
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

    public String generatePrescriptionReport() {
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
        int[] prescriptionYears = new int[50]; // Assuming max 50 different years
        int[] prescriptionsByYear = new int[50];
        double[] revenueByYear = new double[50];
        int prescYearCount = 0;

        Iterator<Prescription> prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
            if (prescription.getPrescriptionDate() != null) {
                int year = prescription.getPrescriptionDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int i = 0; i < prescYearCount; i++) {
                    if (prescriptionYears[i] == year) {
                        yearIndex = i;
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
        for (int i = 0; i < prescYearCount; i++) {
            report.append(String.format("Year %d: %,6d prescriptions (RM %,12.2f revenue)\n",
                    prescriptionYears[i], prescriptionsByYear[i], revenueByYear[i]));
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
            for (int i = 0; i < prescStatusCount; i++) {
                if (prescStatuses[i].equals(status)) {
                    statusIndex = i;
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
        for (int i = 0; i < prescStatusCount; i++) {
            report.append(String.format("%-15s: %d prescriptions\n", prescStatuses[i], prescStatusCounts[i]));
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
        for (int i = 1; i <= 12; i++) {
            if (monthlyTrend[i] > 0) {
                report.append(String.format("%-3s: %d prescriptions\n", months[i], monthlyTrend[i]));
            }
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed prescription table
        report.append(ConsoleUtils.centerText("DETAILED PRESCRIPTION RECORDS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | %14s\n",
                "ID", "Patient", "Doctor", "Date", "Status", "Total"));
        report.append("-".repeat(120)).append("\n");

        prescriptionIterator = prescriptions.iterator();
        while (prescriptionIterator.hasNext()) {
            Prescription prescription = prescriptionIterator.next();
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

    public String generatePharmacyAnalysisReport() {
        StringBuilder report = new StringBuilder();

        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("COMPREHENSIVE PHARMACY ANALYSIS REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n\n");

        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Key Performance Indicators
        report.append(ConsoleUtils.centerText("KEY PERFORMANCE INDICATORS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        double totalInventoryValue = 0;
        double totalRevenue = 0;
        int lowStockCount = getLowStockMedicines().getSize();
        int expiredCount = getExpiredMedicines().getSize();

        Iterator<Medicine> medIterator = medicines.iterator();
        while (medIterator.hasNext()) {
            Medicine med = medIterator.next();
            totalInventoryValue += med.getQuantityInStock() * med.getUnitPrice();
        }

        Iterator<Prescription> presIterator = prescriptions.iterator();
        while (presIterator.hasNext()) {
            Prescription pres = presIterator.next();
            totalRevenue += pres.getTotalCost();
        }

        report.append(String.format("Total Inventory Value    : RM %,15.2f\n", totalInventoryValue));
        report.append(String.format("Total Revenue Generated  : RM %,15.2f\n", totalRevenue));
        report.append(String.format("Stock Efficiency         : %6.1f%% (Low stock medicines: %d)\n",
                (double) (getTotalMedicines() - lowStockCount) / getTotalMedicines() * 100, lowStockCount));
        report.append(String.format("Inventory Health         : %6.1f%% (Non-expired: %d)\n",
                (double) (getTotalMedicines() - expiredCount) / getTotalMedicines() * 100,
                getTotalMedicines() - expiredCount));

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF ANALYSIS REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }
}