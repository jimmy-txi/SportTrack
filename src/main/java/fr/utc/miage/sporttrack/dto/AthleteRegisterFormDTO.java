package fr.utc.miage.sporttrack.dto;

/**
 * Data Transfer Object (DTO) used for athlete registration form binding
 * within the SportTrack application.
 *
 * <p>This DTO replaces direct use of the Athlete JPA entity as a
 * {@code @ModelAttribute} to prevent mass assignment vulnerabilities
 * (SonarQube java:S4684). It carries only the fields required for
 * registering a new athlete account: email, password, and username.</p>
 *
 * @author SportTrack Team
 */
public class AthleteRegisterFormDTO {

    /** The email address provided by the user for registration and future authentication. */
    private String email;

    /** The plaintext password provided by the user, to be hashed before persistence. */
    private String password;

    /** The desired display name and login identifier for the new athlete account. */
    private String username;

    // --- Getters ---

    /**
     * Returns the email address provided during registration.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the plaintext password provided during registration.
     *
     * @return the password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the desired username for the new account.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    // --- Setters ---

    /**
     * Sets the email address for registration.
     *
     * @param email the email address to assign
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the password for registration.
     *
     * @param password the plaintext password to assign
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the desired username for the new account.
     *
     * @param username the username to assign
     */
    public void setUsername(String username) {
        this.username = username;
    }
}