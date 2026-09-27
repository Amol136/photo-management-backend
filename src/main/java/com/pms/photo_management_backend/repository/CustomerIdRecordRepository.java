package com.pms.photo_management_backend.repository;

import com.pms.photo_management_backend.entity.CustomerIdRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerIdRecordRepository
        extends JpaRepository<CustomerIdRecord, Long> {

    List<CustomerIdRecord>
    findByManagerIdOrderByCreatedAtDesc(Long managerId);

    List<CustomerIdRecord>
    findBySubAdminIdOrderByCreatedAtDesc(Long subAdminId);
}