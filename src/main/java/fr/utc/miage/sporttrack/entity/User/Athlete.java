package fr.utc.miage.sporttrack.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "athletes")
public class Athlete extends User {

    @Column(name = "gender")
    private String gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "practice_level")
    private String practiceLevel;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // --- Getters ---

    public String getGender() {
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

    public String getPracticeLevel() {
        return practiceLevel;
    }

    public String getBio() {
        return bio;
    }

    // --- Setters ---

    public void setGender(String gender) {
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

    public void setPracticeLevel(String practiceLevel) {
        this.practiceLevel = practiceLevel;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
