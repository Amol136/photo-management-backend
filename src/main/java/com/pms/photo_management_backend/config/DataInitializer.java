package com.pms.photo_management_backend.config;

import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Bean
    CommandLineRunner createMainAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            // Admin credentials configure केले नसतील
            // तर initialization skip करा.
            if (
                    adminEmail == null ||
                            adminEmail.isBlank() ||
                            adminPassword == null ||
                            adminPassword.isBlank()
            ) {

                System.out.println(
                        "Main Admin initialization skipped."
                );

                return;
            }

            // Admin आधीपासून database मध्ये असल्यास
            // password किंवा account मध्ये कोणताही बदल करू नका.
            if (userRepository.existsByEmail(adminEmail)) {

                System.out.println(
                        "Main Admin already exists."
                );

                return;
            }

            // Admin database मध्ये नसेल तरच नवीन admin तयार करा.
            User admin = new User();

            admin.setName("Main Admin");
            admin.setUserId("ADMIN-001");
            admin.setEmail(adminEmail);
            admin.setMobile("9999999999");

            admin.setPasswordHash(
                    passwordEncoder.encode(adminPassword)
            );

            admin.setRole(Role.MAIN_ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

            System.out.println(
                    "Main Admin created successfully."
            );
        };
    }
}