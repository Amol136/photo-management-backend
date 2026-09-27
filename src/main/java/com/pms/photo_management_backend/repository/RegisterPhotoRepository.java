package com.pms.photo_management_backend.repository;

import com.pms.photo_management_backend.entity.RegisterPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegisterPhotoRepository
        extends JpaRepository<RegisterPhoto, Long> {

    // Manager चे Register Photos
    List<RegisterPhoto>
    findByManagerIdOrderByCreatedAtDesc(
            Long managerId
    );

    // Sub Admin अंतर्गत सर्व Register Photos
    List<RegisterPhoto>
    findBySubAdminIdOrderByCreatedAtDesc(
            Long subAdminId
    );
}