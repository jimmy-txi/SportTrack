package fr.utc.miage.sporttrack.entity.user;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;

@Entity
@Table(name = "athletes")
public class Athlete extends User implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "practice_level")
    private PracticeLevel practiceLevel;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // --- Getters ---

    public Gender getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public Double getHeight() {
        return height;
    }

    public Double getWeight() {
        return weight;
    }

    public PracticeLevel getPracticeLevel() {
        return practiceLevel;
    }

    public String getBio() {
        return bio;
    }

    // --- Setters ---

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setPracticeLevel(PracticeLevel practiceLevel) {
        this.practiceLevel = practiceLevel;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
