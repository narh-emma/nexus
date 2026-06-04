package com.nexus.authservice.dto;

public class LoginResponse {
    private String message;
    private String email;
    private String fullName;
    private String token;
    private long expiresIn;
    
    public LoginResponse() {}
    
    public LoginResponse(String message, String email, String fullName, String token, long expiresIn) {
        this.message = message;
        this.email = email;
        this.fullName = fullName;
        this.token = token;
        this.expiresIn = expiresIn;
    }
    
    public String getMessage() { return message; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getToken() { return token; }
    public long getExpiresIn() { return expiresIn; }
    
    
    public void setMessage(String message) { this.message = message; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setToken(String token) { this.token = token; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}