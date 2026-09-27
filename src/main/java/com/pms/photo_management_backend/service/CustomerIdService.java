package com.pms.photo_management_backend.service;

import com.pms.photo_management_backend.entity.CustomerIdRecord;
import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.repository.CustomerIdRecordRepository;
import com.pms.photo_management_backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerIdService {

    private final CustomerIdRecordRepository customerIdRecordRepository;
    private final UserRepository userRepository;
    private final R2StorageService r2StorageService;

    public CustomerIdService(
            CustomerIdRecordRepository customerIdRecordRepository,
            UserRepository userRepository,
            R2StorageService r2StorageService
    ) {
        this.customerIdRecordRepository = customerIdRecordRepository;
        this.userRepository = userRepository;
        this.r2StorageService = r2StorageService;
    }

    // ==========================================
    // CREATE CUSTOMER ID RECORD WITH PHOTOS
    // ==========================================
    public CustomerIdRecord createRecord(
            Long managerDatabaseId,
            LocalDate date,
            MultipartFile frontPhoto,
            MultipartFile backPhoto
    ) {

        // 1. Manager शोधा
        User manager = userRepository.findById(managerDatabaseId)
                .orElseThrow(() ->
                        new RuntimeException("Manager सापडला नाही.")
                );

        // 2. User खरंच Manager आहे का?
        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException(
                    "हा User Manager नाही."
            );
        }

        // 3. Manager active आहे का?
        if (!Boolean.TRUE.equals(manager.getActive())) {
            throw new RuntimeException(
                    "Manager account inactive आहे."
            );
        }

        // 4. Manager ला Sub Admin जोडलेला आहे का?
        if (manager.getCreatedBySubAdminId() == null) {
            throw new RuntimeException(
                    "Manager ला Sub Admin जोडलेला नाही."
            );
        }

        // 5. Photos validate करा
        if (frontPhoto == null || frontPhoto.isEmpty()) {
            throw new RuntimeException(
                    "Customer ID Front Photo आवश्यक आहे."
            );
        }

        if (backPhoto == null || backPhoto.isEmpty()) {
            throw new RuntimeException(
                    "Customer ID Back Photo आवश्यक आहे."
            );
        }

        String frontObjectKey = null;
        String backObjectKey = null;

        try {

            /*
             * R2 folder structure:
             *
             * customer-ids/
             *   sub-admin-3/
             *     manager-4/
             *       front/
             *       back/
             */

            String baseFolder =
                    "customer-ids/sub-admin-" +
                            manager.getCreatedBySubAdminId() +
                            "/manager-" +
                            manager.getId();

            // 6. Front photo R2 वर upload
            frontObjectKey =
                    r2StorageService.uploadFile(
                            frontPhoto,
                            baseFolder + "/front"
                    );

            // 7. Back photo R2 वर upload
            backObjectKey =
                    r2StorageService.uploadFile(
                            backPhoto,
                            baseFolder + "/back"
                    );

            // 8. PostgreSQL record तयार करा
            CustomerIdRecord record =
                    new CustomerIdRecord();

            record.setDate(date);

            // Database Manager ID
            record.setManagerId(
                    manager.getId()
            );

            // Parent Sub Admin Database ID
            record.setSubAdminId(
                    manager.getCreatedBySubAdminId()
            );

            // R2 object keys
            record.setFrontObjectKey(
                    frontObjectKey
            );

            record.setBackObjectKey(
                    backObjectKey
            );

            record.setStatus("PENDING");

            // 9. PostgreSQL मध्ये save
            return customerIdRecordRepository.save(
                    record
            );

        } catch (Exception exception) {

            /*
             * जर Front upload झाला पण Back/DB save fail झाला,
             * तर orphan files R2 मध्ये राहू नयेत.
             */

            if (frontObjectKey != null) {
                try {
                    r2StorageService.deleteFile(
                            frontObjectKey
                    );
                } catch (Exception ignored) {
                }
            }

            if (backObjectKey != null) {
                try {
                    r2StorageService.deleteFile(
                            backObjectKey
                    );
                } catch (Exception ignored) {
                }
            }

            throw new RuntimeException(
                    "Customer ID save झाला नाही: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    // ==========================================
    // GET MANAGER CUSTOMER ID RECORDS
    // ==========================================
    public List<CustomerIdRecord> getManagerRecords(
            Long managerDatabaseId
    ) {

        return customerIdRecordRepository
                .findByManagerIdOrderByCreatedAtDesc(
                        managerDatabaseId
                );
    }
    // ==========================================
// GET FRONT PHOTO SIGNED URL
// ==========================================

    public String getFrontPhotoSignedUrl(
            Long recordId
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        if (
                record.getFrontObjectKey() == null ||
                        record.getFrontObjectKey().isBlank()
        ) {
            throw new RuntimeException(
                    "Front photo उपलब्ध नाही."
            );
        }

        return r2StorageService.generateSignedUrl(
                record.getFrontObjectKey()
        );
    }


// ==========================================
// GET BACK PHOTO SIGNED URL
// ==========================================

    public String getBackPhotoSignedUrl(
            Long recordId
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        if (
                record.getBackObjectKey() == null ||
                        record.getBackObjectKey().isBlank()
        ) {
            throw new RuntimeException(
                    "Back photo उपलब्ध नाही."
            );
        }

        return r2StorageService.generateSignedUrl(
                record.getBackObjectKey()
        );
    }
    // ==========================================
// FRONT PHOTO DOWNLOAD URL
// ==========================================

    public String getFrontPhotoDownloadUrl(
            Long recordId
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        if (
                record.getFrontObjectKey() == null ||
                        record.getFrontObjectKey().isBlank()
        ) {
            throw new RuntimeException(
                    "Front photo उपलब्ध नाही."
            );
        }

        String fileName =
                "customer-id-front-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService
                .generateDownloadSignedUrl(
                        record.getFrontObjectKey(),
                        fileName
                );
    }


// ==========================================
// BACK PHOTO DOWNLOAD URL
// ==========================================

    public String getBackPhotoDownloadUrl(
            Long recordId
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        if (
                record.getBackObjectKey() == null ||
                        record.getBackObjectKey().isBlank()
        ) {
            throw new RuntimeException(
                    "Back photo उपलब्ध नाही."
            );
        }

        String fileName =
                "customer-id-back-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService
                .generateDownloadSignedUrl(
                        record.getBackObjectKey(),
                        fileName
                );
    }
    // ==========================================
// GET SINGLE CUSTOMER ID RECORD
// ==========================================

    public CustomerIdRecord getRecordById(
            Long recordId,
            Long managerDatabaseId
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        // Manager can access only own record
        if (
                !record.getManagerId()
                        .equals(managerDatabaseId)
        ) {
            throw new RuntimeException(
                    "या Customer ID record वर तुम्हाला access नाही."
            );
        }

        return record;
    }
    // ==========================================
// VERIFY CUSTOMER ID
// ==========================================

    public CustomerIdRecord verifyRecord(
            Long recordId,
            Long managerDatabaseId
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        // Manager can verify only own record
        if (!record.getManagerId().equals(managerDatabaseId)) {
            throw new RuntimeException(
                    "या Customer ID record वर तुम्हाला access नाही."
            );
        }

        record.setStatus("VERIFIED");

        return customerIdRecordRepository.save(record);
    }
    // ==========================================
// REPLACE FRONT PHOTO
// ==========================================

    public CustomerIdRecord replaceFrontPhoto(
            Long recordId,
            Long managerDatabaseId,
            MultipartFile newFrontPhoto
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        // Manager can replace only own record
        if (!record.getManagerId().equals(managerDatabaseId)) {
            throw new RuntimeException(
                    "या Customer ID record वर तुम्हाला access नाही."
            );
        }

        if (newFrontPhoto == null || newFrontPhoto.isEmpty()) {
            throw new RuntimeException(
                    "नवीन Front Photo आवश्यक आहे."
            );
        }

        if (
                newFrontPhoto.getContentType() == null ||
                        !newFrontPhoto.getContentType().startsWith("image/")
        ) {
            throw new RuntimeException(
                    "कृपया फक्त image file upload करा."
            );
        }

        String oldObjectKey = record.getFrontObjectKey();
        String newObjectKey = null;

        try {

            String folder =
                    "customer-ids/sub-admin-" +
                            record.getSubAdminId() +
                            "/manager-" +
                            record.getManagerId() +
                            "/front";

            // Upload new photo first
            newObjectKey =
                    r2StorageService.uploadFile(
                            newFrontPhoto,
                            folder
                    );

            // Update PostgreSQL
            record.setFrontObjectKey(newObjectKey);

            CustomerIdRecord savedRecord =
                    customerIdRecordRepository.save(record);

            // Delete old photo only after DB update succeeds
            if (
                    oldObjectKey != null &&
                            !oldObjectKey.isBlank()
            ) {
                try {
                    r2StorageService.deleteFile(oldObjectKey);
                } catch (Exception ignored) {
                    // New photo + DB update already succeeded.
                }
            }

            return savedRecord;

        } catch (Exception exception) {

            // If something failed after new upload,
            // remove the newly uploaded orphan file.
            if (newObjectKey != null) {
                try {
                    r2StorageService.deleteFile(newObjectKey);
                } catch (Exception ignored) {
                }
            }

            throw new RuntimeException(
                    "Front Photo replace झाला नाही: " +
                            exception.getMessage(),
                    exception
            );
        }
    }


// ==========================================
// REPLACE BACK PHOTO
// ==========================================

    public CustomerIdRecord replaceBackPhoto(
            Long recordId,
            Long managerDatabaseId,
            MultipartFile newBackPhoto
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        // Manager can replace only own record
        if (!record.getManagerId().equals(managerDatabaseId)) {
            throw new RuntimeException(
                    "या Customer ID record वर तुम्हाला access नाही."
            );
        }

        if (newBackPhoto == null || newBackPhoto.isEmpty()) {
            throw new RuntimeException(
                    "नवीन Back Photo आवश्यक आहे."
            );
        }

        if (
                newBackPhoto.getContentType() == null ||
                        !newBackPhoto.getContentType().startsWith("image/")
        ) {
            throw new RuntimeException(
                    "कृपया फक्त image file upload करा."
            );
        }

        String oldObjectKey = record.getBackObjectKey();
        String newObjectKey = null;

        try {

            String folder =
                    "customer-ids/sub-admin-" +
                            record.getSubAdminId() +
                            "/manager-" +
                            record.getManagerId() +
                            "/back";

            // Upload new photo first
            newObjectKey =
                    r2StorageService.uploadFile(
                            newBackPhoto,
                            folder
                    );

            // Update PostgreSQL
            record.setBackObjectKey(newObjectKey);

            CustomerIdRecord savedRecord =
                    customerIdRecordRepository.save(record);

            // Delete old photo only after DB update succeeds
            if (
                    oldObjectKey != null &&
                            !oldObjectKey.isBlank()
            ) {
                try {
                    r2StorageService.deleteFile(oldObjectKey);
                } catch (Exception ignored) {
                    // New photo + DB update already succeeded.
                }
            }

            return savedRecord;

        } catch (Exception exception) {

            if (newObjectKey != null) {
                try {
                    r2StorageService.deleteFile(newObjectKey);
                } catch (Exception ignored) {
                }
            }

            throw new RuntimeException(
                    "Back Photo replace झाला नाही: " +
                            exception.getMessage(),
                    exception
            );
        }
    }
    // ==========================================
// GET SUB ADMIN CUSTOMER ID RECORDS
// ==========================================

    public List<CustomerIdRecord> getSubAdminRecords(
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

        return customerIdRecordRepository
                .findBySubAdminIdOrderByCreatedAtDesc(
                        subAdminDatabaseId
                );
    }
    // ==========================================
// SUB ADMIN - FRONT PHOTO VIEW URL
// ==========================================

    public String getSubAdminFrontPhotoSignedUrl(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        CustomerIdRecord record =
                getSubAdminOwnedRecord(
                        recordId,
                        subAdminDatabaseId
                );

        return r2StorageService.generateSignedUrl(
                record.getFrontObjectKey()
        );
    }


// ==========================================
// SUB ADMIN - BACK PHOTO VIEW URL
// ==========================================

    public String getSubAdminBackPhotoSignedUrl(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        CustomerIdRecord record =
                getSubAdminOwnedRecord(
                        recordId,
                        subAdminDatabaseId
                );

        return r2StorageService.generateSignedUrl(
                record.getBackObjectKey()
        );
    }


// ==========================================
// SUB ADMIN - FRONT PHOTO DOWNLOAD URL
// ==========================================

    public String getSubAdminFrontPhotoDownloadUrl(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        CustomerIdRecord record =
                getSubAdminOwnedRecord(
                        recordId,
                        subAdminDatabaseId
                );

        String fileName =
                "customer-id-front-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService
                .generateDownloadSignedUrl(
                        record.getFrontObjectKey(),
                        fileName
                );
    }


// ==========================================
// SUB ADMIN - BACK PHOTO DOWNLOAD URL
// ==========================================

    public String getSubAdminBackPhotoDownloadUrl(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        CustomerIdRecord record =
                getSubAdminOwnedRecord(
                        recordId,
                        subAdminDatabaseId
                );

        String fileName =
                "customer-id-back-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService
                .generateDownloadSignedUrl(
                        record.getBackObjectKey(),
                        fileName
                );
    }


// ==========================================
// SUB ADMIN OWNERSHIP CHECK
// ==========================================

    private CustomerIdRecord getSubAdminOwnedRecord(
            Long recordId,
            Long subAdminDatabaseId
    ) {

        CustomerIdRecord record =
                customerIdRecordRepository
                        .findById(recordId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer ID record सापडला नाही."
                                )
                        );

        if (!record.getSubAdminId()
                .equals(subAdminDatabaseId)) {

            throw new RuntimeException(
                    "या Customer ID record वर तुम्हाला access नाही."
            );
        }

        return record;
    }
    // ==========================================
// MAIN ADMIN - GET ALL CUSTOMER ID RECORDS
// ==========================================

    public List<CustomerIdRecord> getAllRecordsForMainAdmin(
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

        return customerIdRecordRepository.findAll();
    }
    // ==========================================
// MAIN ADMIN - FRONT PHOTO VIEW
// ==========================================

    public String getMainAdminFrontPhotoSignedUrl(
            Long recordId,
            Long mainAdminDatabaseId
    ) {
        CustomerIdRecord record =
                getMainAdminAccessibleRecord(
                        recordId,
                        mainAdminDatabaseId
                );

        return r2StorageService.generateSignedUrl(
                record.getFrontObjectKey()
        );
    }


// ==========================================
// MAIN ADMIN - BACK PHOTO VIEW
// ==========================================

    public String getMainAdminBackPhotoSignedUrl(
            Long recordId,
            Long mainAdminDatabaseId
    ) {
        CustomerIdRecord record =
                getMainAdminAccessibleRecord(
                        recordId,
                        mainAdminDatabaseId
                );

        return r2StorageService.generateSignedUrl(
                record.getBackObjectKey()
        );
    }


// ==========================================
// MAIN ADMIN - FRONT PHOTO DOWNLOAD
// ==========================================

    public String getMainAdminFrontPhotoDownloadUrl(
            Long recordId,
            Long mainAdminDatabaseId
    ) {
        CustomerIdRecord record =
                getMainAdminAccessibleRecord(
                        recordId,
                        mainAdminDatabaseId
                );

        String fileName =
                "customer-id-front-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService.generateDownloadSignedUrl(
                record.getFrontObjectKey(),
                fileName
        );
    }


// ==========================================
// MAIN ADMIN - BACK PHOTO DOWNLOAD
// ==========================================

    public String getMainAdminBackPhotoDownloadUrl(
            Long recordId,
            Long mainAdminDatabaseId
    ) {
        CustomerIdRecord record =
                getMainAdminAccessibleRecord(
                        recordId,
                        mainAdminDatabaseId
                );

        String fileName =
                "customer-id-back-" +
                        record.getDate() +
                        ".jpg";

        return r2StorageService.generateDownloadSignedUrl(
                record.getBackObjectKey(),
                fileName
        );
    }


// ==========================================
// MAIN ADMIN - CHECK ACCESS
// ==========================================

    private CustomerIdRecord getMainAdminAccessibleRecord(
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

        return customerIdRecordRepository
                .findById(recordId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer ID record सापडला नाही."
                        )
                );
    }
}