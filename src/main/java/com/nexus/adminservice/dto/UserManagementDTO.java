package com.nexus.adminservice.dto;

public class UserManagementDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean active;

    // 1. Required No-Args Constructor
    public UserManagementDTO() {}

    // 2. Required 6-Args Constructor matching AdminService line 104
    public UserManagementDTO(String username, String email, String firstName, String lastName, String role, boolean active) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.active = active;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}