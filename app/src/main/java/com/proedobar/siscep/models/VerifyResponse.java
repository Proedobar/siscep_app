package com.proedobar.siscep.models;

public class VerifyResponse {
    private String status;
    private String message;
    private VerifyData data;

    public static class VerifyData {
        private int user_id;
        private String email;
        private boolean is_verified;

        // Getters y setters
        public int getUserId() { return user_id; }
        public void setUserId(int user_id) { this.user_id = user_id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isVerified() { return is_verified; }
        public void setVerified(boolean is_verified) { this.is_verified = is_verified; }
    }

    // Getters y setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public VerifyData getData() { return data; }
    public void setData(VerifyData data) { this.data = data; }
} 