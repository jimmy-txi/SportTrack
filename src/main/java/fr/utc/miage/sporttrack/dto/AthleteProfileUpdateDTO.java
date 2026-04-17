package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;
import fr.utc.miage.sporttrack.util.TextNormalizer;

/**
 * Data Transfer Object (DTO) used for athlete profile update form binding
 * within the SportTrack application.
 *
 * <p>This DTO replaces direct use of the Athlete JPA entity as a
 * {@code @ModelAttribute} to prevent mass assignment vulnerabilities
 * (SonarQube java:S4684). It exposes only the profile fields that an athlete
 * is permitted to modify through the profile update form.</p>
 *
 * @author SportTrack Team
 */
public class AthleteProfileUpdateDTO {

    /** The desired username for the athlete's account. */
    private String username;

    /** The first name (given name) of the athlete. */
    private String firstName;

    /** The last name (family name) of the athlete. */
    private String lastName;

    /** The gender of the athlete. */
    private Gender gender;

    /** The age of the athlete, in years. */
    private Integer age;

    /** The height of the athlete, in centimetres. */
    private Double height;

    /** The weight of the athlete, in kilograms. */
    private Double weight;

    /** The self-reported practice level indicating the athlete's general sport proficiency. */
    private PracticeLevel practiceLevel;

    /** A short personal biography or description provided by the athlete. */
    private String bio;

    // --- Getters ---

    /**
     * Returns the username.
     *
     * @return the athlete's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the first name of the athlete.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name of the athlete.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the gender of the athlete.
     *
     * @return the {@link Gender}
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Returns the age of the athlete.
     *
     * @return the age in years
     */
    public Integer getAge() {
        return age;
    }

    /**
     * Returns the height of the athlete.
     *
     * @return the height in centimetres
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Returns the weight of the athlete.
     *
     * @return the weight in kilograms
     */
    public Double getWeight() {
        return weight;
    }

    /**
     * Returns the practice level of the athlete.
     *
     * @return the {@link PracticeLevel}
     */
    public PracticeLevel getPracticeLevel() {
        return practiceLevel;
    }

    /**
     * Returns the personal biography of the athlete.
     *
     * @return the bio text
     */
    public String getBio() {
        return bio;
    }

    // --- Setters ---

    /**
     * Sets the username.
     *
     * @param username the username to assign
     */
    public void setUsername(String username) {
        this.username = TextNormalizer.trimNullable(username);
    }

    /**
     * Sets the first name of the athlete.
     *
     * @param firstName the first name to assign
     */
    public void setFirstName(String firstName) {
        this.firstName = TextNormalizer.trimNullable(firstName);
    }

    /**
     * Sets the last name of the athlete.
     *
     * @param lastName the last name to assign
     */
    public void setLastName(String lastName) {
        this.lastName = TextNormalizer.trimNullable(lastName);
    }

    /**
     * Sets the gender of the athlete.
     *
     * @param gender the {@link Gender} to assign
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Sets the age of the athlete.
     *
     * @param age the age in years to assign
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * Sets the height of the athlete.
     *
     * @param height the height in centimetres to assign
     */
    public void setHeight(Double height) {
        this.height = height;
    }

    /**
     * Sets the weight of the athlete.
     *
     * @param weight the weight in kilograms to assign
     */
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * Sets the practice level of the athlete.
     *
     * @param practiceLevel the {@link PracticeLevel} to assign
     */
    public void setPracticeLevel(PracticeLevel practiceLevel) {
        this.practiceLevel = practiceLevel;
    }

    /**
     * Sets the personal biography of the athlete.
     *
     * @param bio the biography text to assign
     */
    public void setBio(String bio) {
        this.bio = TextNormalizer.trimNullable(bio);
    }
}