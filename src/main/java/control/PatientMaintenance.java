package control;

import adt.ArrayList;
import dao.PatientDao;
import entity.Patient;
import java.sql.SQLException;
import java.util.Objects;

public class PatientMaintenance {
    private static final PatientDao PATIENT_DAO = new PatientDao();
    /**
     * Retrieves a patient by their ID
     * @param patientId The ID of the patient to retrieve
     * @return The patient if found, null otherwise
     * @throws IllegalArgumentException if patientId is null or empty
     */
    public static Patient getPatientById(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }

        try {
            return PATIENT_DAO.findById(patientId.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve patient: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all patients
     *
     * @return array of all patients
     */
    public static ArrayList<Patient> getAllPatients() throws SQLException {
        ArrayList<Patient> patients = PATIENT_DAO.findAll();
        return patients;
    }

    /**
     * Updates an existing patient
     * @param patient The patient to update
     * @return true if update was successful, false otherwise
     * @throws IllegalArgumentException if patient is null or invalid
     */
    public static boolean updateUser(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }

        try {
            // If updating email, check that it's not already in use
            Patient existingUserWithEmail = PATIENT_DAO.findByEmail(patient.getEmail());
            if (existingUserWithEmail != null && !Objects.equals(existingUserWithEmail.getPatientId(), patient.getPatientId())) {
                throw new IllegalStateException("Email already in use by another patient");
            }

            return PatientDao.update(patient);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a patient
     * @param patientId The ID of the patient to delete
     * @return true if deletion was successful, false otherwise
     * @throws IllegalArgumentException if patientId is null or empty
     */
    public static boolean deletePatient(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }

        try {
            return PATIENT_DAO.delete(patientId.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete patient: " + e.getMessage(), e);
        }
    }
}