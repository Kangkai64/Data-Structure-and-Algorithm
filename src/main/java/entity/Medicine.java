package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Medicine {
    private String medicineId;
    private String medicineName;
    private String genericName;
    private String manufacturer;
    private String description;
    private String dosageForm;
    private String strength;
    private int quantityInStock;
    private int minimumStockLevel;
    private double unitPrice;
    private LocalDate expiryDate;
    private String storageLocation;
    private boolean requiresPrescription;
    private MedicineStatus status;

    public enum MedicineStatus {
        AVAILABLE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED, EXPIRED
    }

    public Medicine(String medicineId, String medicineName, String genericName,
            String manufacturer, String description, String dosageForm,
            String strength, int quantityInStock, int minimumStockLevel,
            double unitPrice, LocalDate expiryDate, String storageLocation,
            boolean requiresPrescription) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.manufacturer = manufacturer;
        this.description = description;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.quantityInStock = quantityInStock;
        this.minimumStockLevel = minimumStockLevel;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
        this.requiresPrescription = requiresPrescription;
        updateStatus();
    }

    // Constructor without medicineId for UI use
    public Medicine(String medicineName, String genericName,
            String manufacturer, String description, String dosageForm,
            String strength, int quantityInStock, int minimumStockLevel,
            double unitPrice, LocalDate expiryDate, String storageLocation,
            boolean requiresPrescription) {
        this.medicineId = null; // Will be set by control layer
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.manufacturer = manufacturer;
        this.description = description;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.quantityInStock = quantityInStock;
        this.minimumStockLevel = minimumStockLevel;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
        this.storageLocation = storageLocation;
        this.requiresPrescription = requiresPrescription;
        updateStatus();
    }

    // Getters and Setters
    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(int quantityInStock) {
        this.quantityInStock = quantityInStock;
        updateStatus();
    }

    public int getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public void setMinimumStockLevel(int minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
        updateStatus();
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public boolean getRequiresPrescription() {
        return requiresPrescription;
    }

    public String getRequiresPrescriptionString() {
        return requiresPrescription ? "Y" : "N";
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public MedicineStatus getStatus() {
        return status;
    }

    public void setStatus(MedicineStatus status) {
        this.status = status;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isLowStock() {
        return quantityInStock <= minimumStockLevel;
    }

    private void updateStatus() {
        if (quantityInStock == 0) {
            status = MedicineStatus.OUT_OF_STOCK;
        } else if (quantityInStock <= minimumStockLevel) {
            status = MedicineStatus.LOW_STOCK;
        } else {
            status = MedicineStatus.AVAILABLE;
        }
    }

    public void setStatus(String status) {
        if (status.equalsIgnoreCase("AVAILABLE")) {
            this.status = MedicineStatus.AVAILABLE;
        } else if (status.equalsIgnoreCase("LOW_STOCK")) {
            this.status = MedicineStatus.LOW_STOCK;
        } else if (status.equalsIgnoreCase("OUT_OF_STOCK")) {
            this.status = MedicineStatus.OUT_OF_STOCK;
        } else if (status.equalsIgnoreCase("DISCONTINUED")) {
            this.status = MedicineStatus.DISCONTINUED;
        }
    }

    @Override
    public String toString() {
        return "Medicine ID: " + medicineId + "\n" +
                "Medicine Name: " + medicineName + "\n" +
                "Generic Name: " + genericName + "\n" +
                "Manufacturer: " + manufacturer + "\n" +
                "Description: " + description + "\n" +
                "Dosage Form: " + dosageForm + "\n" +
                "Strength: " + strength + "\n" +
                "Quantity In Stock: " + quantityInStock + "\n" +
                "Minimum Stock Level: " + minimumStockLevel + "\n" +
                "Unit Price: RM " + String.format("%.2f", unitPrice) + "\n" +
                "Expiry Date: " + expiryDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\n" +
                "Storage Location: " + storageLocation + "\n" +
                "Requires Prescription: " + requiresPrescription + "\n" +
                "Status: " + status + "\n";
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(medicineId.replaceAll("[^0-9]", ""));
    }
}