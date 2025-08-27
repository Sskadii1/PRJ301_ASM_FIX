package perfumeshop.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * User entity representing a system user
 * @author PerfumeShop Team
 */
public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+84|84|0)[3|5|7|8|9][0-9]{8}$"
    );

    private String userName;
    private String fullName;
    private String password;
    private String address;
    private String phone;
    private String image;
    private String email;
    private int roleID;
    private LocalDate birthdate;

    // Constructors
    public User() {
    }

    public User(String userName, String fullName, String password, String address,
                String phone, String email, String image, LocalDate birthdate, int roleID) {
        this.userName = userName;
        this.fullName = fullName;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.image = image;
        this.birthdate = birthdate;
        this.roleID = roleID;
    }

    // Legacy constructor for backward compatibility
    public User(String userName, String fullName, String password, String address,
                String phone, String email, String image, String birthdate, int roleID) {
        this.userName = userName;
        this.fullName = fullName;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.image = image;
        this.birthdate = parseBirthdate(birthdate);
        this.roleID = roleID;
    }

    // Helper method to parse birthdate
    private LocalDate parseBirthdate(String birthdateStr) {
        if (birthdateStr == null || birthdateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(birthdateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            // Try alternative formats
            try {
                return LocalDate.parse(birthdateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } catch (DateTimeParseException ex) {
                return null; // Invalid date format
            }
        }
    }

    // Getters and Setters with validation
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (userName.length() < 3 || userName.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        this.userName = userName.trim();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (fullName.length() > 100) {
            throw new IllegalArgumentException("Full name cannot exceed 100 characters");
        }
        this.fullName = fullName.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address != null && address.length() > 255) {
            throw new IllegalArgumentException("Address cannot exceed 255 characters");
        }
        this.address = address != null ? address.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new IllegalArgumentException("Invalid phone number format. Expected Vietnamese phone number.");
            }
        }
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        if (roleID < 0) {
            throw new IllegalArgumentException("Role ID cannot be negative");
        }
        this.roleID = roleID;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        if (birthdate != null && birthdate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birthdate cannot be in the future");
        }
        this.birthdate = birthdate;
    }

    // Legacy method for backward compatibility
    public String getBirthdateAsString() {
        return birthdate != null ? birthdate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = parseBirthdate(birthdate);
    }

    // Business methods
    public boolean isAdmin() {
        return roleID == 1;
    }

    public boolean isCustomer() {
        return roleID == 0;
    }

    public String getRoleName() {
        switch (roleID) {
            case 0: return "Customer";
            case 1: return "Administrator";
            default: return "Unknown";
        }
    }

    public int getAge() {
        if (birthdate == null) return -1;
        return LocalDate.now().getYear() - birthdate.getYear();
    }

    public boolean hasValidContactInfo() {
        return (email != null && !email.isEmpty()) || (phone != null && !phone.isEmpty());
    }

    // Object methods
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }

    @Override
    public String toString() {
        return String.format("User{username='%s', fullName='%s', email='%s', phone='%s', role='%s'}",
                           userName, fullName, email, phone, getRoleName());
    }
}
