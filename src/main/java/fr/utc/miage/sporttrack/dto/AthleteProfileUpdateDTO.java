package fr.utc.miage.sporttrack.dto;

import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.PracticeLevel;
import fr.utc.miage.sporttrack.util.TextNormalizer;

//for security >.< 
public class AthleteProfileUpdateDTO {

    private String username;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Integer age;
    private Double height;
    private Double weight;
    private PracticeLevel practiceLevel;
    private String bio;

    // --- Getters ---
    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

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
    public void setUsername(String username) {
        this.username = TextNormalizer.trimNullable(username);
    }

    public void setFirstName(String firstName) {
        this.firstName = TextNormalizer.trimNullable(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = TextNormalizer.trimNullable(lastName);
    }

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
        this.bio = TextNormalizer.trimNullable(bio);
    }
}
