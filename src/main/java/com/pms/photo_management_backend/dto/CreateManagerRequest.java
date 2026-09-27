package com.pms.photo_management_backend.dto;

public class CreateManagerRequest {

    private String managerName;
    private String managerId;
    private String mobile;
    private String email;
    private String password;
    private Long createdBySubAdminId;

    public CreateManagerRequest() {
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
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

    public Long getCreatedBySubAdminId() {
        return createdBySubAdminId;
    }

    public void setCreatedBySubAdminId(Long createdBySubAdminId) {
        this.createdBySubAdminId = createdBySubAdminId;
    }
}