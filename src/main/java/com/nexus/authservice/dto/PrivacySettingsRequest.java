package com.nexus.authservice.dto;

public class PrivacySettingsRequest {
    
    private boolean shareMedicalData;
    private boolean receiveNotifications;
    private boolean dataConsent;
    private boolean shareLocation;

    // ===== CONSTRUCTORS =====
    public PrivacySettingsRequest() {}

    public PrivacySettingsRequest(boolean shareMedicalData, boolean receiveNotifications, 
                                   boolean dataConsent, boolean shareLocation) {
        this.shareMedicalData = shareMedicalData;
        this.receiveNotifications = receiveNotifications;
        this.dataConsent = dataConsent;
        this.shareLocation = shareLocation;
    }

    // ===== GETTERS =====
    public boolean isShareMedicalData() { return shareMedicalData; }
    public boolean isReceiveNotifications() { return receiveNotifications; }
    public boolean isDataConsent() { return dataConsent; }
    public boolean isShareLocation() { return shareLocation; }

    // ===== SETTERS =====
    public void setShareMedicalData(boolean shareMedicalData) { this.shareMedicalData = shareMedicalData; }
    public void setReceiveNotifications(boolean receiveNotifications) { this.receiveNotifications = receiveNotifications; }
    public void setDataConsent(boolean dataConsent) { this.dataConsent = dataConsent; }
    public void setShareLocation(boolean shareLocation) { this.shareLocation = shareLocation; }
}