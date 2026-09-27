package com.pms.photo_management_backend.controller;

import com.pms.photo_management_backend.dto.CreateManagerRequest;
import com.pms.photo_management_backend.dto.CreateSubAdminRequest;
import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    // =====================================
    // HELPER - FORBIDDEN
    // =====================================

    private ResponseEntity<?> forbidden(
            String message
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(message);
    }

    // =====================================
    // CREATE SUB ADMIN
    // MAIN ADMIN ONLY
    // =====================================

    @PostMapping("/sub-admins")
    public ResponseEntity<?> createSubAdmin(

            @AuthenticationPrincipal
            User currentUser,

            @RequestBody
            CreateSubAdminRequest request
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {

                return forbidden(
                        "फक्त Main Admin Sub Admin तयार करू शकतो."
                );
            }

            User user =
                    userService.createSubAdmin(
                            request
                    );

            return ResponseEntity.ok(
                    createSubAdminResponse(
                            user,
                            "Sub Admin यशस्वीरीत्या तयार झाला."
                    )
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // GET ALL SUB ADMINS
    // MAIN ADMIN ONLY
    // =====================================

    @GetMapping("/sub-admins")
    public ResponseEntity<?> getAllSubAdmins(

            @AuthenticationPrincipal
            User currentUser
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {

                return forbidden(
                        "फक्त Main Admin Sub Admin list पाहू शकतो."
                );
            }

            List<Map<String, Object>> response =
                    userService
                            .getAllSubAdmins()
                            .stream()
                            .map(
                                    user ->
                                            createSubAdminResponse(
                                                    user,
                                                    null
                                            )
                            )
                            .toList();

            return ResponseEntity.ok(
                    response
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // UPDATE SUB ADMIN
    // MAIN ADMIN ONLY
    // =====================================

    @PutMapping("/sub-admins/{id}")
    public ResponseEntity<?> updateSubAdmin(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long id,

            @RequestBody
            CreateSubAdminRequest request
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {

                return forbidden(
                        "फक्त Main Admin Sub Admin update करू शकतो."
                );
            }

            User user =
                    userService.updateSubAdmin(
                            id,
                            request
                    );

            return ResponseEntity.ok(
                    createSubAdminResponse(
                            user,
                            "Sub Admin यशस्वीरीत्या update झाला."
                    )
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // DELETE SUB ADMIN
    // MAIN ADMIN ONLY
    // =====================================

    @DeleteMapping("/sub-admins/{id}")
    public ResponseEntity<?> deleteSubAdmin(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long id
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {

                return forbidden(
                        "फक्त Main Admin Sub Admin delete करू शकतो."
                );
            }

            userService.deleteSubAdmin(
                    id
            );

            Map<String, String> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Sub Admin यशस्वीरीत्या delete झाला."
            );

            return ResponseEntity.ok(
                    response
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // SUB ADMIN RESPONSE BUILDER
    // =====================================

    private Map<String, Object> createSubAdminResponse(

            User user,
            String message
    ) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "id",
                user.getId()
        );

        response.put(
                "name",
                user.getName()
        );

        response.put(
                "subAdminId",
                user.getUserId()
        );

        response.put(
                "mobile",
                user.getMobile()
        );

        response.put(
                "email",
                user.getEmail()
        );

        response.put(
                "role",
                user.getRole().name()
        );

        response.put(
                "status",
                Boolean.TRUE.equals(
                        user.getActive()
                )
                        ? "ACTIVE"
                        : "INACTIVE"
        );

        if (message != null) {

            response.put(
                    "message",
                    message
            );
        }

        return response;
    }

    // =====================================
    // CREATE MANAGER
    // SUB ADMIN ONLY
    // =====================================

    @PostMapping("/managers")
    public ResponseEntity<?> createManager(

            @AuthenticationPrincipal
            User currentUser,

            @RequestBody
            CreateManagerRequest request
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {

                return forbidden(
                        "फक्त Sub Admin Manager तयार करू शकतो."
                );
            }

            // IMPORTANT:
            // Frontend कडून आलेला createdBySubAdminId
            // विश्वासाने वापरायचा नाही.
            //
            // JWT authenticated Sub Admin ची DB ID
            // force करून set करतो.

            request.setCreatedBySubAdminId(
                    currentUser.getId()
            );

            User manager =
                    userService.createManager(
                            request
                    );

            Map<String, Object> response =
                    createManagerResponse(
                            manager
                    );

            response.put(
                    "message",
                    "Manager यशस्वीरीत्या तयार झाला."
            );

            return ResponseEntity.ok(
                    response
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // GET SUB ADMIN'S MANAGERS
    // SUB ADMIN ONLY
    // =====================================

    @GetMapping(
            "/sub-admins/{subAdminId}/managers"
    )
    public ResponseEntity<?> getManagersBySubAdmin(

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

                return forbidden(
                        "फक्त Sub Admin Manager list पाहू शकतो."
                );
            }

            // URL मधील subAdminId authorization
            // साठी वापरत नाही.

            Long authenticatedSubAdminId =
                    currentUser.getId();

            List<Map<String, Object>> response =
                    userService
                            .getManagersBySubAdmin(
                                    authenticatedSubAdminId
                            )
                            .stream()
                            .map(
                                    this::createManagerResponse
                            )
                            .toList();

            return ResponseEntity.ok(
                    response
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // UPDATE MANAGER
    // SUB ADMIN ONLY
    // =====================================

    @PutMapping(
            "/sub-admins/{subAdminId}/managers/{managerId}"
    )
    public ResponseEntity<?> updateManager(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long subAdminId,

            @PathVariable
            Long managerId,

            @RequestBody
            CreateManagerRequest request
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {

                return forbidden(
                        "फक्त Sub Admin Manager update करू शकतो."
                );
            }

            Long authenticatedSubAdminId =
                    currentUser.getId();

            // Parent बदलण्याचा प्रयत्न होऊ नये
            request.setCreatedBySubAdminId(
                    authenticatedSubAdminId
            );

            User manager =
                    userService.updateManager(
                            managerId,
                            authenticatedSubAdminId,
                            request
                    );

            return ResponseEntity.ok(
                    createManagerResponse(
                            manager
                    )
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // DELETE MANAGER
    // SUB ADMIN ONLY
    // =====================================

    @DeleteMapping(
            "/sub-admins/{subAdminId}/managers/{managerId}"
    )
    public ResponseEntity<?> deleteManager(

            @AuthenticationPrincipal
            User currentUser,

            @PathVariable
            Long subAdminId,

            @PathVariable
            Long managerId
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.SUB_ADMIN
            ) {

                return forbidden(
                        "फक्त Sub Admin Manager delete करू शकतो."
                );
            }

            Long authenticatedSubAdminId =
                    currentUser.getId();

            userService.deleteManager(
                    managerId,
                    authenticatedSubAdminId
            );

            Map<String, String> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "Manager यशस्वीरीत्या delete झाला."
            );

            return ResponseEntity.ok(
                    response
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }

    // =====================================
    // MANAGER RESPONSE BUILDER
    // =====================================

    private Map<String, Object> createManagerResponse(

            User manager
    ) {

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "id",
                manager.getId()
        );

        response.put(
                "managerName",
                manager.getName()
        );

        response.put(
                "managerId",
                manager.getUserId()
        );

        response.put(
                "mobile",
                manager.getMobile()
        );

        response.put(
                "email",
                manager.getEmail()
        );

        response.put(
                "role",
                manager.getRole().name()
        );

        response.put(
                "createdBySubAdminId",
                manager.getCreatedBySubAdminId()
        );

        response.put(
                "status",
                Boolean.TRUE.equals(
                        manager.getActive()
                )
                        ? "ACTIVE"
                        : "INACTIVE"
        );

        return response;
    }

    // ==========================================
    // GET ALL MANAGERS
    // MAIN ADMIN ONLY
    // ==========================================

    @GetMapping("/managers")
    public ResponseEntity<?> getAllManagers(

            @AuthenticationPrincipal
            User currentUser
    ) {

        try {

            if (
                    currentUser == null ||
                            currentUser.getRole() != Role.MAIN_ADMIN
            ) {

                return forbidden(
                        "फक्त Main Admin सर्व Managers पाहू शकतो."
                );
            }

            List<Map<String, Object>> response =
                    userService
                            .getAllManagers()
                            .stream()
                            .map(
                                    this::createManagerResponse
                            )
                            .toList();

            return ResponseEntity.ok(
                    response
            );

        } catch (RuntimeException exception) {

            return ResponseEntity
                    .badRequest()
                    .body(exception.getMessage());
        }
    }
}