package com.proedobar.siscep.models;

public class RegisterRequest {
    private String ci;
    private String email;
    private String password;

    public RegisterRequest(String ci, String email, String password) {
        this.ci = ci;
        this.email = email;
        this.password = password;
    }

    // Getters y setters
    public String getCi() { return ci; }
    public void setCi(String ci) { this.ci = ci; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
} 