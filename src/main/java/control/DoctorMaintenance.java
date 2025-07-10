package control;

/*
 * @author Lee Yong Kang
 */

import dao.DoctorDao;
import entity.Doctor;
import java.sql.SQLException;
import java.util.Objects;

public class DoctorMaintenance {
    private static final DoctorDao DOCTOR_DAO = new DoctorDao();

    public static Doctor getDoctorById(String doctorId) {
        if (doctorId == null || doctorId.trim().isEmpty()) {
            throw  new IllegalArgumentException("Doctor ID cannot be null or empty");
        }

        try {
            return DOCTOR_DAO.findById(doctorId.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve doctor: " + e.getMessage(), e);
        }
    }
}
