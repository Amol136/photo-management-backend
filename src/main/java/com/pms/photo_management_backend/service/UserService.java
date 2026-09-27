package com.pms.photo_management_backend.service;

import com.pms.photo_management_backend.dto.CreateSubAdminRequest;
import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.pms.photo_management_backend.dto.CreateManagerRequest;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================
    // CREATE SUB ADMIN
    // =====================================

    public User createSubAdmin(CreateSubAdminRequest request) {

        String email =
                request.getEmail().trim().toLowerCase();

        String subAdminId =
                request.getSubAdminId().trim().toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "या Email वर Sub Admin आधीपासून अस्तित्वात आहे."
            );
        }

        if (userRepository.existsByUserId(subAdminId)) {
            throw new RuntimeException(
                    "हा Sub Admin ID आधीपासून अस्तित्वात आहे."
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setUserId(subAdminId);
        user.setMobile(request.getMobile().trim());
        user.setEmail(email);

        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(Role.SUB_ADMIN);
        user.setActive(true);
        user.setCreatedBySubAdminId(null);

        return userRepository.save(user);
    }

    // =====================================
    // GET ALL SUB ADMINS
    // =====================================

    public List<User> getAllSubAdmins() {

        return userRepository
                .findAll()
                .stream()
                .filter(
                        user ->
                                user.getRole() == Role.SUB_ADMIN
                )
                .toList();
    }

    // =====================================
    // UPDATE SUB ADMIN
    // =====================================

    public User updateSubAdmin(
            Long id,
            CreateSubAdminRequest request
    ) {

        User user = userRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Sub Admin सापडला नाही."
                        )
                );

        if (user.getRole() != Role.SUB_ADMIN) {
            throw new RuntimeException(
                    "हा user Sub Admin नाही."
            );
        }

        String newEmail =
                request.getEmail().trim().toLowerCase();

        String newUserId =
                request.getSubAdminId().trim().toUpperCase();

        userRepository
                .findByEmail(newEmail)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException(
                                "हा Email आधीपासून वापरात आहे."
                        );
                    }
                });

        userRepository
                .findByUserId(newUserId)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException(
                                "हा Sub Admin ID आधीपासून वापरात आहे."
                        );
                    }
                });

        user.setName(request.getName().trim());
        user.setUserId(newUserId);
        user.setMobile(request.getMobile().trim());
        user.setEmail(newEmail);

        // Edit करताना password blank असेल
        // तर जुना password तसाच राहील.
        if (
                request.getPassword() != null &&
                        !request.getPassword().isBlank()
        ) {
            user.setPasswordHash(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        return userRepository.save(user);
    }

    // =====================================
    // DELETE SUB ADMIN
    // =====================================

    public void deleteSubAdmin(Long id) {

        User user = userRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Sub Admin सापडला नाही."
                        )
                );

        if (user.getRole() != Role.SUB_ADMIN) {
            throw new RuntimeException(
                    "हा user Sub Admin नाही."
            );
        }

        userRepository.delete(user);
    }
    // =====================================
// CREATE MANAGER
// =====================================

    public User createManager(CreateManagerRequest request) {

        if (request.getCreatedBySubAdminId() == null) {
            throw new RuntimeException(
                    "Sub Admin माहिती मिळाली नाही."
            );
        }

        User subAdmin = userRepository
                .findById(request.getCreatedBySubAdminId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Sub Admin सापडला नाही."
                        )
                );

        if (subAdmin.getRole() != Role.SUB_ADMIN) {
            throw new RuntimeException(
                    "Manager फक्त Sub Admin अंतर्गत तयार करता येतो."
            );
        }

        if (!Boolean.TRUE.equals(subAdmin.getActive())) {
            throw new RuntimeException(
                    "Sub Admin account inactive आहे."
            );
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String managerId =
                request.getManagerId()
                        .trim()
                        .toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException(
                    "या Email वर account आधीपासून अस्तित्वात आहे."
            );
        }

        if (userRepository.existsByUserId(managerId)) {
            throw new RuntimeException(
                    "हा Manager ID आधीपासून अस्तित्वात आहे."
            );
        }

        User manager = new User();

        manager.setName(
                request.getManagerName().trim()
        );

        manager.setUserId(managerId);

        manager.setMobile(
                request.getMobile().trim()
        );

        manager.setEmail(email);

        manager.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        manager.setRole(Role.MANAGER);

        // Database primary key of Sub Admin
        manager.setCreatedBySubAdminId(
                subAdmin.getId()
        );

        manager.setActive(true);

        return userRepository.save(manager);
    }
    // =====================================
// GET MANAGERS BY SUB ADMIN
// =====================================

    public List<User> getManagersBySubAdmin(Long subAdminId) {

        User subAdmin = userRepository
                .findById(subAdminId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Sub Admin सापडला नाही."
                        )
                );

        if (subAdmin.getRole() != Role.SUB_ADMIN) {
            throw new RuntimeException(
                    "हा user Sub Admin नाही."
            );
        }

        return userRepository
                .findAll()
                .stream()
                .filter(user ->
                        user.getRole() == Role.MANAGER &&
                                subAdminId.equals(
                                        user.getCreatedBySubAdminId()
                                )
                )
                .toList();
    }


// =====================================
// UPDATE MANAGER
// =====================================

    public User updateManager(
            Long managerId,
            Long subAdminId,
            CreateManagerRequest request
    ) {

        User manager = userRepository
                .findById(managerId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Manager सापडला नाही."
                        )
                );

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException(
                    "हा user Manager नाही."
            );
        }

        // Important ownership check
        if (
                !subAdminId.equals(
                        manager.getCreatedBySubAdminId()
                )
        ) {
            throw new RuntimeException(
                    "या Manager वर तुम्हाला access नाही."
            );
        }

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String newManagerId =
                request.getManagerId()
                        .trim()
                        .toUpperCase();

        userRepository
                .findByEmail(email)
                .ifPresent(existing -> {

                    if (
                            !existing.getId()
                                    .equals(managerId)
                    ) {
                        throw new RuntimeException(
                                "हा Email आधीपासून वापरात आहे."
                        );
                    }
                });

        userRepository
                .findByUserId(newManagerId)
                .ifPresent(existing -> {

                    if (
                            !existing.getId()
                                    .equals(managerId)
                    ) {
                        throw new RuntimeException(
                                "हा Manager ID आधीपासून वापरात आहे."
                        );
                    }
                });

        manager.setName(
                request.getManagerName().trim()
        );

        manager.setUserId(newManagerId);

        manager.setMobile(
                request.getMobile().trim()
        );

        manager.setEmail(email);

        // Password blank असल्यास जुना password राहील
        if (
                request.getPassword() != null &&
                        !request.getPassword().isBlank()
        ) {
            manager.setPasswordHash(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        // Ownership बदलणार नाही
        manager.setCreatedBySubAdminId(
                subAdminId
        );

        return userRepository.save(manager);
    }


// =====================================
// DELETE MANAGER
// =====================================

    public void deleteManager(
            Long managerId,
            Long subAdminId
    ) {

        User manager = userRepository
                .findById(managerId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Manager सापडला नाही."
                        )
                );

        if (manager.getRole() != Role.MANAGER) {
            throw new RuntimeException(
                    "हा user Manager नाही."
            );
        }

        if (
                !subAdminId.equals(
                        manager.getCreatedBySubAdminId()
                )
        ) {
            throw new RuntimeException(
                    "या Manager वर तुम्हाला access नाही."
            );
        }

        userRepository.delete(manager);
    }
    // ==========================================
// MAIN ADMIN - GET ALL MANAGERS
// ==========================================

    public List<User> getAllManagers() {
        return userRepository.findByRoleOrderByCreatedAtDesc(
                Role.MANAGER
        );
    }
}