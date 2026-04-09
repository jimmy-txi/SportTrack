package fr.utc.miage.sporttrack.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@MappedSuperclass
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idU;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Setter
    @Column(name = "last_name")
    private String lastName;

    @Setter
    @Column(name = "first_name")
    private String firstName;

    @Column(nullable = false, unique = true)
    private String email;

    public void setUsername(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username is required");
        }
        this.username = username;
    }

    public void setPassword(String password) {
        if (isBlank(password)) {
            throw new IllegalArgumentException("Password is required");
        }
        this.password = password;
    }

    public void setEmail(String email) {
        if (isBlank(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(idU, user.idU) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idU, email);
    }

    //for verify the String value not blank
    protected boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public void login(){
        //logic
    }

    public void logout(){
        //logic
    }
}
