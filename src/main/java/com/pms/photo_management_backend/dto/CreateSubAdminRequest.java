package com.pms.photo_management_backend.dto;

public class CreateSubAdminRequest {

    private String name;
    private String subAdminId;
    private String mobile;
    private String email;
    private String password;

    public CreateSubAdminRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubAdminId() {
        return subAdminId;
    }

    public void setSubAdminId(String subAdminId) {
        this.subAdminId = subAdminId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}