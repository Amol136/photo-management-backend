package com.pms.photo_management_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_id_records")
public class CustomerIdRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "manager_id", nullable = false)
    private Long managerId;

    @Column(name = "sub_admin_id", nullable = false)
    private Long subAdminId;

    @Column(name = "front_object_key")
    private String frontObjectKey;

    @Column(name = "back_object_key")
    private String backObjectKey;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CustomerIdRecord() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Long getManagerId() {
        return managerId;
    }

    public Long getSubAdminId() {
        return subAdminId;
    }

    public String getFrontObjectKey() {
        return frontObjectKey;
    }

    public String getBackObjectKey() {
        return backObjectKey;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public void setSubAdminId(Long subAdminId) {
        this.subAdminId = subAdminId;
    }

    public void setFrontObjectKey(String frontObjectKey) {
        this.frontObjectKey = frontObjectKey;
    }

    public void setBackObjectKey(String backObjectKey) {
        this.backObjectKey = backObjectKey;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}