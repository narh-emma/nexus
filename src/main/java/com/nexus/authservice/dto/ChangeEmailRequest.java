package com.nexus.authservice.dto;

public class ChangeEmailRequest {
    private String newEmail;
    private String currentPassword; // require re-auth for a sensitive change like this

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
}
