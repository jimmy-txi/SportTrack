package fr.utc.miage.sporttrack.entity.user;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;

/**
 * JPA entity representing an athlete user within the SportTrack application.
 *
 * <p>An athlete extends {@link User} with additional profile attributes such as
 * gender, age, height, weight, practice level, and a personal biography.
 * Athletes can create activities, participate in challenges, earn badges, and
 * interact with other athletes through friendships and messages.</p>
 *
 * @author SportTrack Team
 */
@Entity
@Table(name = "athletes")
public class Athlete extends User implements Serializable {

    /** The gender of the athlete. */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    /** The age of the athlete, in years. */
    @Column(name = "age")
    private Integer age;

    /** The height of the athlete, in centimetres. */
    @Column(name = "height")
    private Double height;

    /** The weight of the athlete, in kilograms. */
    @Column(name = "weight")
    private Double weight;

    /** The self-reported practice level indicating the athlete's general sport proficiency. */
    @Enumerated(EnumType.STRING)
    @Column(name = "practice_level")
    private PracticeLevel practiceLevel;

    /** A short personal biography or description provided by the athlete. */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // --- Getters ---

    /**
     * Returns the gender of this athlete.
     *
     * @return the {@link Gender}, or {@code null} if not specified
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Returns the age of this athlete.
     *
     * @return the age in years, or {@code null} if not specified
     */
    public Integer getAge() {
        return age;
    }

    /**
     * Returns the height of this athlete.
     *
     * @return the height in centimetres, or {@code null} if not specified
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Returns the weight of this athlete.
     *
     * @return the weight in kilograms, or {@code null} if not specified
     */
    public Double getWeight() {
        return weight;
    }

    /**
     * Returns the practice level of this athlete.
     *
     * @return the {@link PracticeLevel}, or {@code null} if not specified
     */
    public PracticeLevel getPracticeLevel() {
        return practiceLevel;
    }

    /**
     * Returns the personal biography of this athlete.
     *
     * @return the bio text, or {@code null} if not provided
     */
    public String getBio() {
        return bio;
    }

    // --- Setters ---

    /**
     * Sets the gender of this athlete.
     *
     * @param gender the {@link Gender} to assign
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Sets the age of this athlete.
     *
     * @param age the age in years to assign
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * Sets the height of this athlete.
     *
     * @param height the height in centimetres to assign
     */
    public void setHeight(Double height) {
        this.height = height;
    }

    /**
     * Sets the weight of this athlete.
     *
     * @param weight the weight in kilograms to assign
     */
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    /**
     * Sets the practice level of this athlete.
     *
     * @param practiceLevel the {@link PracticeLevel} to assign
     */
    public void setPracticeLevel(PracticeLevel practiceLevel) {
        this.practiceLevel = practiceLevel;
    }

    /**
     * Sets the personal biography of this athlete.
     *
     * @param bio the biography text to assign
     */
    public void setBio(String bio) {
        this.bio = bio;
    }
}