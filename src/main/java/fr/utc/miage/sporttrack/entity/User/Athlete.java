package fr.utc.miage.sporttrack.entity.User;

public class Athlete extends User {

    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    private String practiceLevel;
    private String bio;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getPracticeLevel() {
        return practiceLevel;
    }

    public void setPracticeLevel(String practiceLevel) {
        this.practiceLevel = practiceLevel;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }


}