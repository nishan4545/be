package com.slotbooking.modules.user.repository;

import com.slotbooking.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobileNumber(String mobileNumber);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR " +
           "u.mobileNumber LIKE CONCAT('%', CAST(:search AS string), '%') OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:role IS NULL OR u.role = :role)")
    Page<User> findAllUsers(
            @Param("search") String search,
            @Param("status") UserStatus status,
            @Param("role") Role role,
            Pageable pageable
    );

    java.util.List<User> findByRole(Role role);
}
