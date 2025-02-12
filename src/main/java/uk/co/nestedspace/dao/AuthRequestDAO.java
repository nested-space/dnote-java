package uk.co.nestedspace.dao;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class AuthRequestDAO {
    private String email;
    private String password;

    public AuthRequestDAO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}