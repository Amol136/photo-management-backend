package com.pms.photo_management_backend.controller;

import com.pms.photo_management_backend.entity.CustomerIdRecord;
import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.service.CustomerIdService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-ids")
public class CustomerIdController {

    private final CustomerIdService customerIdService;

    public CustomerIdController(
            CustomerIdService customerIdService
    ) {
        this.customerIdService = customerIdService;
    }

    // ==========================================
    // HELPER - CHECK ROLE
    // ==========================================

    private ResponseEntity<?> forbidden() {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("या API साठी तुम्हाला permission नाही.");
    }

    // ==========================================
    // MANAGER - UPLOAD CUSTOMER ID
    // ==========================================

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadCustomerId(

            @AuthenticationPrincipal User currentUser,

            // Frontend compatibility साठी ठेवले आहे.
            // Authorization साठी वापरत नाही.
            @RequestParam(
                    value = "managerId",
                    required = false
            )
            Long ignoredManagerId,

            @RequestParam("date")
            String date,

            @RequestParam("frontPhoto")
            MultipartFile frontPhoto,

            @RequestParam("backPhoto")
            MultipartFile backPhoto
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            Long managerId =
                    currentUser.getId();

            LocalDate recordDate =
                    LocalDate.parse(date);

            CustomerIdRecord record =
                    customerIdService.createRecord(
                            managerId,
                            recordDate,
                            frontPhoto,
                            backPhoto
                    );

            return ResponseEntity.ok(record);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - GET OWN RECORDS
    // ==========================================

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<?> getManagerRecords(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long managerId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            // URL मधला managerId विश्वासाने वापरत नाही.
            Long authenticatedManagerId =
                    currentUser.getId();

            List<CustomerIdRecord> records =
                    customerIdService
                            .getManagerRecords(
                                    authenticatedManagerId
                            );

            return ResponseEntity.ok(records);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - GET SINGLE RECORD
    // ==========================================

    @GetMapping("/{recordId}")
    public ResponseEntity<?> getCustomerIdRecord(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "managerId",
                    required = false
            )
            Long ignoredManagerId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            CustomerIdRecord record =
                    customerIdService.getRecordById(
                            recordId,
                            currentUser.getId()
                    );

            return ResponseEntity.ok(record);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - VERIFY CUSTOMER ID
    // ==========================================

    @PutMapping("/{recordId}/verify")
    public ResponseEntity<?> verifyCustomerId(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "managerId",
                    required = false
            )
            Long ignoredManagerId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            CustomerIdRecord record =
                    customerIdService.verifyRecord(
                            recordId,
                            currentUser.getId()
                    );

            return ResponseEntity.ok(record);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - REPLACE FRONT PHOTO
    // ==========================================

    @PutMapping(
            value = "/{recordId}/replace-front",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> replaceFrontPhoto(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "managerId",
                    required = false
            )
            Long ignoredManagerId,

            @RequestParam("frontPhoto")
            MultipartFile frontPhoto
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            CustomerIdRecord record =
                    customerIdService.replaceFrontPhoto(
                            recordId,
                            currentUser.getId(),
                            frontPhoto
                    );

            return ResponseEntity.ok(record);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - REPLACE BACK PHOTO
    // ==========================================

    @PutMapping(
            value = "/{recordId}/replace-back",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> replaceBackPhoto(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "managerId",
                    required = false
            )
            Long ignoredManagerId,

            @RequestParam("backPhoto")
            MultipartFile backPhoto
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            CustomerIdRecord record =
                    customerIdService.replaceBackPhoto(
                            recordId,
                            currentUser.getId(),
                            backPhoto
                    );

            return ResponseEntity.ok(record);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - FRONT PHOTO VIEW
    // ==========================================

    @GetMapping("/{recordId}/front-url")
    public ResponseEntity<?> getFrontPhotoUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            // आधी ownership verify
            customerIdService.getRecordById(
                    recordId,
                    currentUser.getId()
            );

            String signedUrl =
                    customerIdService
                            .getFrontPhotoSignedUrl(
                                    recordId
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            signedUrl
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - BACK PHOTO VIEW
    // ==========================================

    @GetMapping("/{recordId}/back-url")
    public ResponseEntity<?> getBackPhotoUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            customerIdService.getRecordById(
                    recordId,
                    currentUser.getId()
            );

            String signedUrl =
                    customerIdService
                            .getBackPhotoSignedUrl(
                                    recordId
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            signedUrl
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - FRONT DOWNLOAD
    // ==========================================

    @GetMapping("/{recordId}/front-download-url")
    public ResponseEntity<?> getFrontPhotoDownloadUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            customerIdService.getRecordById(
                    recordId,
                    currentUser.getId()
            );

            String downloadUrl =
                    customerIdService
                            .getFrontPhotoDownloadUrl(
                                    recordId
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            downloadUrl
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MANAGER - BACK DOWNLOAD
    // ==========================================

    @GetMapping("/{recordId}/back-download-url")
    public ResponseEntity<?> getBackPhotoDownloadUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MANAGER
            ) {
                return forbidden();
            }

            customerIdService.getRecordById(
                    recordId,
                    currentUser.getId()
            );

            String downloadUrl =
                    customerIdService
                            .getBackPhotoDownloadUrl(
                                    recordId
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            downloadUrl
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // SUB ADMIN - GET OWN RECORDS
    // ==========================================

    @GetMapping("/sub-admin/{subAdminId}")
    public ResponseEntity<?> getSubAdminRecords(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long subAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {
                return forbidden();
            }

            Long authenticatedSubAdminId =
                    currentUser.getId();

            List<CustomerIdRecord> records =
                    customerIdService
                            .getSubAdminRecords(
                                    authenticatedSubAdminId
                            );

            return ResponseEntity.ok(records);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // SUB ADMIN - FRONT VIEW
    // ==========================================

    @GetMapping("/{recordId}/sub-admin-front-url")
    public ResponseEntity<?> getSubAdminFrontPhotoUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "subAdminId",
                    required = false
            )
            Long ignoredSubAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getSubAdminFrontPhotoSignedUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // SUB ADMIN - BACK VIEW
    // ==========================================

    @GetMapping("/{recordId}/sub-admin-back-url")
    public ResponseEntity<?> getSubAdminBackPhotoUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "subAdminId",
                    required = false
            )
            Long ignoredSubAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getSubAdminBackPhotoSignedUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // SUB ADMIN - FRONT DOWNLOAD
    // ==========================================

    @GetMapping("/{recordId}/sub-admin-front-download-url")
    public ResponseEntity<?> getSubAdminFrontDownloadUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "subAdminId",
                    required = false
            )
            Long ignoredSubAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getSubAdminFrontPhotoDownloadUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // SUB ADMIN - BACK DOWNLOAD
    // ==========================================

    @GetMapping("/{recordId}/sub-admin-back-download-url")
    public ResponseEntity<?> getSubAdminBackDownloadUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "subAdminId",
                    required = false
            )
            Long ignoredSubAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getSubAdminBackPhotoDownloadUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MAIN ADMIN - GET ALL RECORDS
    // ==========================================

    @GetMapping("/main-admin/{mainAdminId}")
    public ResponseEntity<?> getAllRecordsForMainAdmin(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long mainAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {
                return forbidden();
            }

            List<CustomerIdRecord> records =
                    customerIdService
                            .getAllRecordsForMainAdmin(
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(records);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MAIN ADMIN - FRONT VIEW
    // ==========================================

    @GetMapping("/{recordId}/main-admin-front-url")
    public ResponseEntity<?> getMainAdminFrontPhotoUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "mainAdminId",
                    required = false
            )
            Long ignoredMainAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getMainAdminFrontPhotoSignedUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MAIN ADMIN - BACK VIEW
    // ==========================================

    @GetMapping("/{recordId}/main-admin-back-url")
    public ResponseEntity<?> getMainAdminBackPhotoUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "mainAdminId",
                    required = false
            )
            Long ignoredMainAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getMainAdminBackPhotoSignedUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MAIN ADMIN - FRONT DOWNLOAD
    // ==========================================

    @GetMapping("/{recordId}/main-admin-front-download-url")
    public ResponseEntity<?> getMainAdminFrontPhotoDownloadUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "mainAdminId",
                    required = false
            )
            Long ignoredMainAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getMainAdminFrontPhotoDownloadUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // ==========================================
    // MAIN ADMIN - BACK DOWNLOAD
    // ==========================================

    @GetMapping("/{recordId}/main-admin-back-download-url")
    public ResponseEntity<?> getMainAdminBackDownloadUrl(

            @AuthenticationPrincipal User currentUser,

            @PathVariable
            Long recordId,

            @RequestParam(
                    value = "mainAdminId",
                    required = false
            )
            Long ignoredMainAdminId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {
                return forbidden();
            }

            String url =
                    customerIdService
                            .getMainAdminBackPhotoDownloadUrl(
                                    recordId,
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "url",
                            url
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }
}