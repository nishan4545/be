package com.slotbooking.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.modules.auth.dto.LoginRequest;
import com.slotbooking.modules.auth.dto.RegisterRequest;
import com.slotbooking.modules.auth.service.AuthService;
import com.slotbooking.modules.auth.service.OtpService;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.slotbooking.security.TokenService jwtService;

    @Mock
    private com.slotbooking.modules.auth.validator.MobileValidator mobileValidator;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.slotbooking.modules.auth.repository.OTPVerificationRepository otpVerificationRepository;

    @Mock
    private com.slotbooking.modules.notification.service.NotificationService notificationService;

    @Mock
    private com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private AuthService authService;

    private User playerUser;

    @BeforeEach
    void setUp() {
        playerUser = User.builder()
                .id(1L)
                .mobileNumber("9876543210")
                .password("encoded_pass")
                .fullName("Test Player")
                .role(Role.PLAYER)
                .status(UserStatus.APPROVED)
                .mobileVerified(true)
                .build();
    }

    @Test
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .mobileNumber("9876543210")
                .password("password123")
                .fullName("Test Player")
                .email("test@player.com")
                .build();

        com.slotbooking.modules.auth.entity.OTPVerification otp = new com.slotbooking.modules.auth.entity.OTPVerification();
        otp.setMobileNumber(request.getMobileNumber());
        otp.setVerified(true);

        when(userRepository.findByMobileNumber(request.getMobileNumber())).thenReturn(Optional.empty());
        when(otpVerificationRepository.findTopByMobileNumberAndVerifiedTrueOrderByIdDesc(request.getMobileNumber()))
                .thenReturn(Optional.of(otp));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(playerUser);

        assertDoesNotThrow(() -> authService.register(request));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_duplicateMobile_throwsException() {
        RegisterRequest request = RegisterRequest.builder()
                .mobileNumber("9876543210")
                .password("password123")
                .build();

        when(userRepository.findByMobileNumber(request.getMobileNumber())).thenReturn(Optional.of(playerUser));

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .mobileNumber("9876543210")
                .password("password123")
                .build();

        when(userRepository.findByMobileNumber(request.getMobileNumber())).thenReturn(Optional.of(playerUser));
        when(passwordEncoder.matches(request.getPassword(), playerUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("mocked_token");

        assertNotNull(authService.login(request));
    }

    @Test
    void login_invalidPassword_throwsException() {
        LoginRequest request = LoginRequest.builder()
                .mobileNumber("9876543210")
                .password("wrong")
                .build();

        when(userRepository.findByMobileNumber(request.getMobileNumber())).thenReturn(Optional.of(playerUser));
        when(passwordEncoder.matches(request.getPassword(), playerUser.getPassword())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
