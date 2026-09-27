package com.pms.photo_management_backend.service;

import com.pms.photo_management_backend.entity.RegisterPhoto;
import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.repository.RegisterPhotoRepository;
import com.pms.photo_management_backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegisterPhotoService {

    private final RegisterPhotoRepository registerPhotoRepository;
    private final UserRepository userRepository;
    private final R2StorageService r2StorageService;

    public RegisterPhotoService(
            RegisterPhotoRepository registerPhotoRepository,
            UserRepository userRepository,
            R2StorageService r2StorageService
    ) {
        this.registerPhotoRepository = registerPhotoRepository;
        this.userRepository = userRepository;
        this.r2StorageService = r2StorageService;
    }

    // ==========================================
    // UPLOAD REGISTER PHOTO
    // ==========================================

    public RegisterPhoto createRecord(
            Long managerDatabaseId,
            LocalDate date,
            MultipartFile photo
    ) {

        User manager =
                userRepository
                        .findById(managerDatabaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Manager सापडला नाही."
                                )
                        );

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException(
                    "हा User Manager नाही."
            );
        }

        if (!Boolean.TRUE.equals(manager.getActive())) {
            throw new RuntimeException(
                    "Manager account inactive आहे."
            );
        }

        if (manager.getCreatedBySubAdminId() == null) {
            throw new RuntimeException(
                    "Manager ला Sub Admin जोडलेला नाही."
            );
        }

        if (date == null) {
            throw new RuntimeException(
                    "Register Photo Date आवश्यक आहे."
            );
        }

        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException(
                    "Register Photo आवश्यक आहे."
            );
        }

        if (
                photo.getContentType() == null ||
                        !photo.getContentType().startsWith("image/")
        ) {
            throw new RuntimeException(
                    "कृपया फक्त image file upload करा."
            );
        }

        String objectKey = null;

        try {

            String folder =
                    "register-photos/sub-admin-" +
                            manager.getCreatedBySubAdminId() +
                            "/manager-" +
                            manager.getId();

            // R2 upload
            objectKey =
                    r2StorageService.uploadFile(
                            photo,
                            folder
                    );

            RegisterPhoto record =
                    new RegisterPhoto();

            record.setDate(date);

            // Database Manager ID
            record.setManagerId(
                    manager.getId()
            );

            // Parent Sub Admin Database ID
            record.setSubAdminId(
                    manager.getCreatedBySubAdminId()
            );

            // Private R2 object key
            record.setObjectKey(
                    objectKey
            );

            record.setStatus(
                    "UPLOADED"
            );

            // PostgreSQL save
            return registerPhotoRepository.save(
                    record
            );

        } catch (Exception exception) {

            // DB save fail झाला तर R2 orphan photo delete करा
            if (objectKey != null) {
                try {
                    r2StorageService.deleteFile(
                            objectKey
                    );
                } catch (Exception ignored) {
                }
            }

            throw new RuntimeException(
                    "Register Photo save झाला नाही: " +
                            exception.getMessage(),
                    exception
            );
        }
    }

    // ==========================================
    // GET MANAGER REGISTER PHOTOS
    // ==========================================

    public List<RegisterPhoto> getManagerRecords(
            Long managerDatabaseId
    ) {
        return registerPhotoRepository
                .findByManagerIdOrderByCreatedAtDesc(
                        managerDatabaseId
                );
    }
    // ==========================================
// GET SUB ADMIN REGISTER PHOTOS
// ==========================================

    public List<RegisterPhoto> getSubAdminRecords(
            Long subAdminDatabaseId
    ) {

        User subAdmin =
                userRepository
                        .findById(subAdminDatabaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Sub Admin सापडला नाही."
                                )
                        );

        if (subAdmin.getRole() != Role.SUB_ADMIN) {
            throw new RuntimeException(
                    "हा User Sub Admin नाही."
            );
        }

        if (!Boolean.TRUE.equals(subAdmin.getActive())) {
            throw new RuntimeException(
                    "Sub Admin account inactive आहे."
            );
        }

        return registerPhotoRepository
                .findBySubAdminIdOrderByCreatedAtDesc(
                        subAdminDatabaseId
                );
    }

    // ==========================================
    // VIEW PHOTO SIGNED URL
    // ==========================================

    public String getPhotoSignedUrl(
            Long recordId,
            Long managerDatabaseId
    ) {

        RegisterPhoto record =
                getOwnedRecord(
                        recordId,
                        managerDatabaseId
                );

        if (
                record.getObjectKey() == null ||
                        record.getObjectKey().isBlank()
        ) {
            throw new RuntimeException(
                    "Register Photo उपलब्ध नाही."
            );
        }

        return r2StorageService.generateSignedUrl(
                record.getObjectKey()
        );
    }

    // ==========================================
    // DOWNLOAD PHOTO SIGNED URL
    // ==========================================

    public String getPhotoDownloadUrl(
            Long recordId,
            Long managerDatabaseId
    ) {

        RegisterPhoto record =
                getOwnedRecord(
                        recordId,
                        managerDatabaseId
                );

        if (
                record.getObjectKey() == null ||
                        record.getObjectKey().isBlank()
        ) {
            throw new RuntimeException(
                    "Register Photo उपलब्ध नाही."
            );
        }

        String fileName =
                "register-photo-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService
                .generateDownloadSignedUrl(
                        record.getObjectKey(),
                        fileName
                );
    }

    // ==========================================
    // CHECK RECORD OWNERSHIP
    // ==========================================

    private RegisterPhoto getOwnedRecord(
            Long recordId,
            Long managerDatabaseId
    ) {

        RegisterPhoto record =
                registerPhotoRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Register Photo record सापडला नाही."
                                )
                        );

        if (
                !record.getManagerId()
                        .equals(managerDatabaseId)
        ) {
            throw new RuntimeException(
                    "या Register Photo वर तुम्हाला access नाही."
            );
        }

        return record;
    }
    // ==========================================
// SUB ADMIN - VIEW PHOTO SIGNED URL
// ==========================================

    public String getSubAdminPhotoSignedUrl(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        RegisterPhoto record =
                getSubAdminOwnedRecord(
                        recordId,
                        subAdminDatabaseId
                );

        return r2StorageService.generateSignedUrl(
                record.getObjectKey()
        );
    }


// ==========================================
// SUB ADMIN - DOWNLOAD PHOTO SIGNED URL
// ==========================================

    public String getSubAdminPhotoDownloadUrl(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        RegisterPhoto record =
                getSubAdminOwnedRecord(
                        recordId,
                        subAdminDatabaseId
                );

        String fileName =
                "register-photo-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService
                .generateDownloadSignedUrl(
                        record.getObjectKey(),
                        fileName
                );
    }


// ==========================================
// SUB ADMIN OWNERSHIP CHECK
// ==========================================

    private RegisterPhoto getSubAdminOwnedRecord(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        RegisterPhoto record =
                registerPhotoRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Register Photo सापडला नाही."
                                )
                        );

        if (!record.getSubAdminId()
                .equals(subAdminDatabaseId)) {

            throw new RuntimeException(
                    "या Register Photo वर तुम्हाला access नाही."
            );
        }

        return record;
    }
    // ==========================================
// MAIN ADMIN - GET ALL REGISTER PHOTOS
// ==========================================

    public List<RegisterPhoto> getAllRecordsForMainAdmin(
            Long mainAdminDatabaseId
    ) {
        User mainAdmin = userRepository
                .findById(mainAdminDatabaseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Main Admin सापडला नाही."
                        )
                );

        if (mainAdmin.getRole() != Role.MAIN_ADMIN) {
            throw new RuntimeException(
                    "हा User Main Admin नाही."
            );
        }

        if (!Boolean.TRUE.equals(mainAdmin.getActive())) {
            throw new RuntimeException(
                    "Main Admin account inactive आहे."
            );
        }

        return registerPhotoRepository.findAll();
    }
    // ==========================================
// MAIN ADMIN - REGISTER PHOTO VIEW
// ==========================================

    public String getMainAdminPhotoSignedUrl(
            Long recordId,
            Long mainAdminDatabaseId
    ) {
        RegisterPhoto record =
                getMainAdminAccessibleRecord(
                        recordId,
                        mainAdminDatabaseId
                );

        return r2StorageService.generateSignedUrl(
                record.getObjectKey()
        );
    }


// ==========================================
// MAIN ADMIN - REGISTER PHOTO DOWNLOAD
// ==========================================

    public String getMainAdminPhotoDownloadUrl(
            Long recordId,
            Long mainAdminDatabaseId
    ) {
        RegisterPhoto record =
                getMainAdminAccessibleRecord(
                        recordId,
                        mainAdminDatabaseId
                );

        String fileName =
                "register-photo-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService.generateDownloadSignedUrl(
                record.getObjectKey(),
                fileName
        );
    }


// ==========================================
// MAIN ADMIN - CHECK ACCESS
// ==========================================

    private RegisterPhoto getMainAdminAccessibleRecord(
            Long recordId,
            Long mainAdminDatabaseId
    ) {
        User mainAdmin =
                userRepository
                        .findById(mainAdminDatabaseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Main Admin सापडला नाही."
                                )
                        );

        if (mainAdmin.getRole() != Role.MAIN_ADMIN) {
            throw new RuntimeException(
                    "हा User Main Admin नाही."
            );
        }

        if (!Boolean.TRUE.equals(mainAdmin.getActive())) {
            throw new RuntimeException(
                    "Main Admin account inactive आहे."
            );
        }

        return registerPhotoRepository
                .findById(recordId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Register Photo सापडला नाही."
                        )
                );
    }
}