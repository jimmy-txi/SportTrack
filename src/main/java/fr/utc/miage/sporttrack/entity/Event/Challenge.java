package fr.utc.miage.sporttrack.entity.event;

import fr.utc.miage.sporttrack.entity.activity.Sport;
import fr.utc.miage.sporttrack.entity.enumeration.Gender;
import fr.utc.miage.sporttrack.entity.enumeration.Metric;
import fr.utc.miage.sporttrack.entity.user.Athlete;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idC;
    private String nom;
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateDebut;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "Metric")
    private Metric metric;

    @ManyToOne
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Athlete organizer;

    @ManyToMany
    @JoinTable(
        name = "challenge_participants",
        joinColumns = @JoinColumn(name = "challenge_id"),
        inverseJoinColumns = @JoinColumn(name = "athlete_id")
    )private List<Athlete> participants;

    public Challenge() {}

    public Challenge(String nom, String description, LocalDate dateDebut, LocalDate dateFin, Metric metric) {
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.metric = metric;
    }

    public Athlete getOrganizer() {
        return organizer;
    }
 
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public List<Athlete> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Athlete> participants) {
        this.participants = participants;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Sport getSport() {
        return sport;
    }

    public void setOrganizer(Athlete organizer) {
        this.organizer = organizer;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }
}
