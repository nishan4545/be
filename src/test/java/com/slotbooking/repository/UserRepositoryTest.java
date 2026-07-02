package com.slotbooking.repository;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        User player1 = User.builder()
                .mobileNumber("9876543211")
                .password("pass1")
                .fullName("John Doe")
                .role(Role.PLAYER)
                .status(UserStatus.APPROVED)
                .mobileVerified(true)
                .build();
        userRepository.save(player1);

        User player2 = User.builder()
                .mobileNumber("9876543212")
                .password("pass2")
                .fullName("Jane Doe")
                .role(Role.PLAYER)
                .status(UserStatus.PENDING)
                .mobileVerified(true)
                .build();
        userRepository.save(player2);
    }

    @Test
    void testFindByMobileNumber() {
        Optional<User> user = userRepository.findByMobileNumber("9876543211");
        assertTrue(user.isPresent());
        assertEquals("John Doe", user.get().getFullName());
    }

    @Test
    void testFindAllUsers_withSearchAndStatus() {
        Page<User> result = userRepository.findAllUsers("Doe", UserStatus.APPROVED, Role.PLAYER, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().getFirst().getFullName());
    }

    @Test
    void testFindAllUsers_searchFullNameIgnoreCase() {
        Page<User> result = userRepository.findAllUsers("john", null, null, PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().getFirst().getFullName());
    }
}
