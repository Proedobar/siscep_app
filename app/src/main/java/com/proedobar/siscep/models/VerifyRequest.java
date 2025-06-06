package com.proedobar.siscep.models;

public class VerifyRequest {
    private String code;
    private int user_id;

    public VerifyRequest(String code, int user_id) {
        this.code = code;
        this.user_id = user_id;
    }

    // Getters y setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getUserId() { return user_id; }
    public void setUserId(int user_id) { this.user_id = user_id; }
} 