package com.slotbooking.config;

import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin user on application startup if one does not already exist.
 * Default credentials:
 *   Mobile: 9999999999
 *   Password: Admin@123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminMobile = "9999999999";

        if (userRepository.findByMobileNumber(adminMobile).isEmpty()) {
            User admin = User.builder()
                    .fullName("Super Admin")
                    .mobileNumber(adminMobile)
                    .email("admin@slotbooking.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.APPROVED)
                    .mobileVerified(true)
                    .build();

            userRepository.save(admin);
            log.info("============================================");
            log.info("  DEFAULT ADMIN USER CREATED");
            log.info("  Mobile: {}", adminMobile);
            log.info("  Password: Admin@123");
            log.info("============================================");
        } else {
            log.info("[AdminSeeder] Admin user already exists.");
        }
    }
}
