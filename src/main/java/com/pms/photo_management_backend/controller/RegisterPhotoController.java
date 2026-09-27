package com.pms.photo_management_backend.controller;

import com.pms.photo_management_backend.entity.RegisterPhoto;
import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.service.RegisterPhotoService;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/register-photos")
public class RegisterPhotoController {

    private final RegisterPhotoService registerPhotoService;

    public RegisterPhotoController(
            RegisterPhotoService registerPhotoService
    ) {
        this.registerPhotoService =
                registerPhotoService;
    }

    // ==========================================
    // HELPER - FORBIDDEN
    // ==========================================

    private ResponseEntity<?> forbidden() {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        "या API साठी तुम्हाला permission नाही."
                );
    }

    // ==========================================
    // MANAGER - UPLOAD REGISTER PHOTO
    // ==========================================

    @PostMapping(
            value = "/upload",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> uploadRegisterPhoto(

            @AuthenticationPrincipal
            User currentUser,

            // Frontend compatibility साठी ठेवले आहे.
            // Authorization साठी वापरत नाही.
            @RequestParam(
                    value = "managerId",
                    required = false
            )
            Long ignoredManagerId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date,

            @RequestParam("photo")
            MultipartFile photo
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

            RegisterPhoto record =
                    registerPhotoService
                            .createRecord(
                                    managerId,
                                    date,
                                    photo
                            );

            return ResponseEntity.ok(record);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // MANAGER - GET OWN REGISTER PHOTOS
    // ==========================================

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<?> getManagerRecords(

            @AuthenticationPrincipal
            User currentUser,

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

            Long authenticatedManagerId =
                    currentUser.getId();

            List<RegisterPhoto> records =
                    registerPhotoService
                            .getManagerRecords(
                                    authenticatedManagerId
                            );

            return ResponseEntity.ok(records);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // MANAGER - VIEW REGISTER PHOTO
    // ==========================================

    @GetMapping("/{recordId}/photo-url")
    public ResponseEntity<?> getPhotoUrl(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long recordId,

            // Frontend compatibility only
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

            String signedUrl =
                    registerPhotoService
                            .getPhotoSignedUrl(
                                    recordId,
                                    currentUser.getId()
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
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // MANAGER - DOWNLOAD REGISTER PHOTO
    // ==========================================

    @GetMapping("/{recordId}/download-url")
    public ResponseEntity<?> getDownloadUrl(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long recordId,

            // Frontend compatibility only
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

            String downloadUrl =
                    registerPhotoService
                            .getPhotoDownloadUrl(
                                    recordId,
                                    currentUser.getId()
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
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // SUB ADMIN - GET OWN REGISTER PHOTOS
    // ==========================================

    @GetMapping("/sub-admin/{subAdminId}")
    public ResponseEntity<?> getSubAdminRecords(

            @AuthenticationPrincipal
            User currentUser,

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

            List<RegisterPhoto> records =
                    registerPhotoService
                            .getSubAdminRecords(
                                    authenticatedSubAdminId
                            );

            return ResponseEntity.ok(records);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // SUB ADMIN - VIEW REGISTER PHOTO
    // ==========================================

    @GetMapping("/{recordId}/sub-admin-photo-url")
    public ResponseEntity<?> getSubAdminPhotoUrl(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long recordId,

            // Frontend compatibility only
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
                    registerPhotoService
                            .getSubAdminPhotoSignedUrl(
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
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // SUB ADMIN - DOWNLOAD REGISTER PHOTO
    // ==========================================

    @GetMapping("/{recordId}/sub-admin-download-url")
    public ResponseEntity<?> getSubAdminDownloadUrl(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long recordId,

            // Frontend compatibility only
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
                    registerPhotoService
                            .getSubAdminPhotoDownloadUrl(
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
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // MAIN ADMIN - GET ALL REGISTER PHOTOS
    // ==========================================

    @GetMapping("/main-admin/{mainAdminId}")
    public ResponseEntity<?> getAllRecordsForMainAdmin(

            @AuthenticationPrincipal
            User currentUser,

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

            List<RegisterPhoto> records =
                    registerPhotoService
                            .getAllRecordsForMainAdmin(
                                    currentUser.getId()
                            );

            return ResponseEntity.ok(records);

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // MAIN ADMIN - VIEW REGISTER PHOTO
    // ==========================================

    @GetMapping("/{recordId}/main-admin-photo-url")
    public ResponseEntity<?> getMainAdminPhotoUrl(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long recordId,

            // Frontend compatibility only
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
                    registerPhotoService
                            .getMainAdminPhotoSignedUrl(
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
                    .body(
                            exception.getMessage()
                    );
        }
    }

    // ==========================================
    // MAIN ADMIN - DOWNLOAD REGISTER PHOTO
    // ==========================================

    @GetMapping("/{recordId}/main-admin-download-url")
    public ResponseEntity<?> getMainAdminPhotoDownloadUrl(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long recordId,

            // Frontend compatibility only
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
                    registerPhotoService
                            .getMainAdminPhotoDownloadUrl(
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
                    .body(
                            exception.getMessage()
                    );
        }
    }
}