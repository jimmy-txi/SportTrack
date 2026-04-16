package fr.utc.miage.sporttrack.dto;

/**
 * DTO used for athlete registration form binding.
 * Replaces direct use of the Athlete JPA entity as a @ModelAttribute
 * to prevent mass assignment vulnerabilities (SonarQube java:S4684).
 */
public class AthleteRegisterFormDTO {

    private String email;
    private String password;
    private String username;

    // --- Getters ---

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    // --- Setters ---

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
