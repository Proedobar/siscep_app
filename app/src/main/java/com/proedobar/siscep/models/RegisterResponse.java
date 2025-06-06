package com.proedobar.siscep.models;

public class RegisterResponse {
    private String status;
    private String message;
    private RegisterData data;
    private Boolean warning;

    public static class RegisterData {
        private int user_id;
        private String email;

        // Getters y setters
        public int getUserId() { return user_id; }
        public void setUserId(int user_id) { this.user_id = user_id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // Getters y setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public RegisterData getData() { return data; }
    public void setData(RegisterData data) { this.data = data; }
    public Boolean getWarning() { return warning; }
    public void setWarning(Boolean warning) { this.warning = warning; }
} 