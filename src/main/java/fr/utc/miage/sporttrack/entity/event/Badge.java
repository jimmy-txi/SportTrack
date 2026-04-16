package fr.utc.miage.sporttrack.entity.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String label;

    @Column(name = "description_col", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Metric metric;

    @Column(nullable = false)
    private double threshold;

    @Column(nullable = false)
    private String icon;

    @ManyToMany
    @JoinTable(
        name = "badge_athletes",
        joinColumns = @JoinColumn(name = "badge_id"),
        inverseJoinColumns = @JoinColumn(name = "athlete_id")
    )
    private List<Athlete> earnedBy;

    public Badge() {}

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public Sport getSport() {
        return sport;
    }

    public Metric getMetric() {
        return metric;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getIcon() {
        return icon;
    }

    public List<Athlete> getEarnedBy() {
        return earnedBy;
    }

    // --- Setters ---

    public void setId(int id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setEarnedBy(List<Athlete> earnedBy) {
        this.earnedBy = earnedBy;
    }
}