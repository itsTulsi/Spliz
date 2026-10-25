package com.fairshare.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @JsonAlias({"usernameOrEmail", "username", "email"})
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getUsernameOrEmail() { return identifier; }
    public void setUsernameOrEmail(String usernameOrEmail) {
        if (this.identifier == null || this.identifier.isBlank()) {
            this.identifier = usernameOrEmail;
        }
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
