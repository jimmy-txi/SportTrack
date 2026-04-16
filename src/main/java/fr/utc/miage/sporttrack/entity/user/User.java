package fr.utc.miage.sporttrack.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for all user types within the SportTrack application.
 *
 * <p>This class is mapped as a JPA {@link MappedSuperclass} and provides
 * common fields shared by both {@link Admin} and {@link Athlete} subclasses,
 * including authentication credentials, personal information, and a dynamically
 * generated profile photo URL.</p>
 *
 * @author SportTrack Team
 */
@MappedSuperclass
public abstract class User implements Serializable {

    /** The unique database-generated identifier for this user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** The unique username used for authentication and display purposes. */
    @Column(nullable = false, unique = true)
    private String username;

    /** The hashed password used for authentication. */
    @Column(nullable = false)
    private String password;

    /** The last name (family name) of the user. */
    @Column(name = "last_name")
    private String lastName;

    /** The first name (given name) of the user. */
    @Column(name = "first_name")
    private String firstName;

    /** The unique email address of the user, used for communication and authentication. */
    @Column(nullable = false, unique = true)
    private String email;

    // --- Getters ---

    /**
     * Returns the unique identifier of this user.
     *
     * @return the user's database identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the username of this user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the hashed password of this user.
     *
     * @return the password hash
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the last name (family name) of this user.
     *
     * @return the last name, or {@code null} if not provided
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the first name (given name) of this user.
     *
     * @return the first name, or {@code null} if not provided
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the email address of this user.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Generates and returns the URL of the user's profile photo.
     * Uses the RoboHash service to produce a unique avatar based on the username.
     * Falls back to a default avatar if the username is blank.
     *
     * @return the profile photo URL
     */
    public String getProfilePhotoUrl() {
        if (isBlank(username)) {
            return "https://robohash.org/default?set=set4";
        }
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        return "https://robohash.org/" + encodedUsername+"?set=set4";
    }

    // --- Setters ---

    /**
     * Sets the last name (family name) of this user.
     *
     * @param lastName the last name to assign
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the first name (given name) of this user.
     *
     * @param firstName the first name to assign
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the username of this user.
     *
     * @param username the username to assign; must not be blank
     * @throws IllegalArgumentException if the provided username is blank
     */
    public void setUsername(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username is required");
        }
        this.username = username;
    }

    /**
     * Sets the password of this user.
     *
     * @param password the password to assign; must not be blank
     * @throws IllegalArgumentException if the provided password is blank
     */
    public void setPassword(String password) {
        if (isBlank(password)) {
            throw new IllegalArgumentException("Password is required");
        }
        this.password = password;
    }

    /**
     * Sets the email address of this user.
     *
     * @param email the email address to assign; must not be blank
     * @throws IllegalArgumentException if the provided email is blank
     */
    public void setEmail(String email) {
        if (isBlank(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        this.email = email;
    }

    /**
     * Compares this user to another object for equality based on the
     * database identifier and email address.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are considered equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user))
            return false;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    /**
     * Returns the hash code for this user, computed from the identifier and email.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    /**
     * Checks whether the given string value is blank (i.e., {@code null},
     * empty, or consisting solely of whitespace characters).
     *
     * @param value the string value to check
     * @return {@code true} if the value is blank, {@code false} otherwise
     */
    protected boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}