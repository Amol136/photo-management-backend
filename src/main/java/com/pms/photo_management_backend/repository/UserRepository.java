package com.pms.photo_management_backend.repository;
import com.pms.photo_management_backend.entity.Role;
import com.pms.photo_management_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(String userId);

    boolean existsByEmail(String email);

    boolean existsByUserId(String userId);
    List<User> findByRoleOrderByCreatedAtDesc(Role role);
}