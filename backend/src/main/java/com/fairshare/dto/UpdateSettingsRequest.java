package com.fairshare.dto;

public class UpdateSettingsRequest {

    private String username;
    private String displayName;
    private String email;
    private String upiId;
    private String currentPassword;
    private String newPassword;

    public UpdateSettingsRequest() {}

    public UpdateSettingsRequest(String username, String displayName, String email, String upiId, String currentPassword, String newPassword) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.upiId = upiId;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
