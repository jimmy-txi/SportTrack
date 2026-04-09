package fr.utc.miage.sporttrack.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "athletes")
@Setter
@Getter
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

}