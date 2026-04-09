package fr.utc.miage.sporttrack.entity.User;

import java.util.Objects;

public abstract class User {

    private Integer idU;

    private String username;
    private String password;

    private String lastName;
    private String firstName;

    private String email;

    public Integer getIdU() {
        return idU;
    }

    public void setIdU(Integer idU) {
        this.idU = idU;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("Username is required");
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (isBlank(password)) {
            throw new IllegalArgumentException("Password is required");
        }
        this.password = password;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
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
        return idU == user.idU && Objects.equals(email, user.email);
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
