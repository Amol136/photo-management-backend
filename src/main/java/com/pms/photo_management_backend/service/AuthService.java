package com.pms.photo_management_backend.service;

import com.pms.photo_management_backend.dto.LoginRequest;
import com.pms.photo_management_backend.dto.LoginResponse;
import com.pms.photo_management_backend.entity.User;
import com.pms.photo_management_backend.repository.UserRepository;
import com.pms.photo_management_backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Invalid email or password"
                        )
                );

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException(
                    "Account is inactive"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        // JWT TOKEN GENERATE
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getUserId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedBySubAdminId(),
                token,
                "Login successful"
        );
    }
}