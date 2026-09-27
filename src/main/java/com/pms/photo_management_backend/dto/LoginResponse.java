package com.pms.photo_management_backend.dto;

public class LoginResponse {

    private Long id;
    private String name;
    private String userId;
    private String email;
    private String role;
    private Long createdBySubAdminId;
    private String token;
    private String message;

    public LoginResponse(
            Long id,
            String name,
            String userId,
            String email,
            String role,
            Long createdBySubAdminId,
            String token,
            String message
    ) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.createdBySubAdminId = createdBySubAdminId;
        this.token = token;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getCreatedBySubAdminId() {
        return createdBySubAdminId;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }
}