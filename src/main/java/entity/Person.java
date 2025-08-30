package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static utility.PatternChecker.*;

/**
 * Represents a person in the Clinic Management System.
 * It is the parent class of Patient and Doctor.
 * It should not be constructed or instantiated directly.
 */
public abstract class Person {
    private String fullName;
    private String ICNumber; // Included Date of Birth and gender
    private String email;
    private String phoneNumber;
    private Address address;
    private LocalDate registrationDate;

    public Person(String fullName, String ICNumber, String email, String phoneNumber, Address address,
            LocalDate registrationDate) {
        this.fullName = fullName;
        this.ICNumber = ICNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.registrationDate = registrationDate;
    }

    public Person(Person person) {
        this.fullName = person.getFullName();
        this.ICNumber = person.getICNumber();
        this.email = person.getEmail();
        this.phoneNumber = person.getPhoneNumber();
        this.address = person.getAddress();
        this.registrationDate = person.getRegistrationDate();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        this.fullName = fullName.trim();
    }

    public String getICNumber() {
        return ICNumber;
    }

    public void setICNumber(String ICNumber) {
        if (ICNumber == null || ICNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("ICNumber cannot be null or empty");
        } else if (!IC_PATTERN.matcher(ICNumber.trim()).matches()) {
            throw new IllegalArgumentException("Invalid IC number format");
        }
        this.ICNumber = ICNumber.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber.trim();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        if (registrationDate == null) {
            throw new IllegalArgumentException("Registration date cannot be null");
        }
        this.registrationDate = registrationDate;
    }

    /**
     * Calculate age based on IC number (Malaysian IC format)
     * @return age in years
     */
    public int getAge() {
        if (ICNumber == null || ICNumber.length() < 6) {
            return 0;
        }
        
        try {
            // Extract date of birth from IC number (first 6 digits: YYMMDD)
            String dobString = ICNumber.substring(0, 6);
            int year = Integer.parseInt(dobString.substring(0, 2));
            int month = Integer.parseInt(dobString.substring(2, 4));
            int day = Integer.parseInt(dobString.substring(4, 6));
            
            // Determine century (assume 20xx for years 00-29, 19xx for years 30-99)
            int fullYear = year <= 29 ? 2000 + year : 1900 + year;
            
            LocalDate dateOfBirth = LocalDate.of(fullYear, month, day);
            LocalDate currentDate = LocalDate.now();
            
            int age = currentDate.getYear() - dateOfBirth.getYear();
            
            // Adjust age if birthday hasn't occurred this year
            if (currentDate.getMonthValue() < dateOfBirth.getMonthValue() || 
                (currentDate.getMonthValue() == dateOfBirth.getMonthValue() && 
                 currentDate.getDayOfMonth() < dateOfBirth.getDayOfMonth())) {
                age--;
            }
            
            return Math.max(0, age); // Ensure non-negative age
        } catch (Exception e) {
            return 0; // Return 0 if calculation fails
        }
    }

    @Override
    public String toString() {
        return "Name = " + fullName + "\n"
                + ", ICNumber = " + ICNumber + "\n"
                + ", Email = " + email + "\n"
                + ", PhoneNumber = " + phoneNumber + "\n"
                + ", RegistrationDate = " + registrationDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Person user = (Person) o;
        return ICNumber.equals(user.ICNumber);
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(ICNumber.replaceAll("[^0-9]", ""));
    }
}