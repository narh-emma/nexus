package com.nexus.authservice.dto;

public class AuthResponse {
    private String message;
    private String email;
    private String fullName;
    
    public AuthResponse() {}
    
    public AuthResponse(String message, String email, String fullName) {
        this.message = message;
        this.email = email;
        this.fullName = fullName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}