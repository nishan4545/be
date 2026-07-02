package com.slotbooking.modules.auth.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.exception.ValidationException;
import com.slotbooking.modules.auth.dto.AuthResponse;
import com.slotbooking.modules.auth.dto.LoginRequest;
import com.slotbooking.modules.auth.dto.RegisterRequest;
import com.slotbooking.modules.auth.entity.OTPVerification;
import com.slotbooking.modules.auth.repository.OTPVerificationRepository;
import com.slotbooking.modules.auth.validator.MobileValidator;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.UserRepository;
import com.slotbooking.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final OTPVerificationRepository otpVerificationRepository;
    private final TokenService jwtService;
    private final MobileValidator mobileValidator;
    private final PasswordEncoder passwordEncoder;
    private final com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;
    private final com.slotbooking.modules.notification.service.NotificationService notificationService;

    @Transactional
    public void sendOtp(String mobileNumber) {
        if (!mobileValidator.isValid(mobileNumber)) {
            throw new ValidationException("Invalid mobile number");
        }
        String otp = otpService.generateOtp(mobileNumber);
        otpService.sendOtp(mobileNumber, otp);
    }

    @Transactional
    public void verifyOtp(String mobileNumber, String otp) {
        boolean isValid = otpService.verifyOtp(mobileNumber, otp);
        if (!isValid) {
            throw new BusinessException("Invalid or expired OTP");
        }
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.findByMobileNumber(request.getMobileNumber()).isPresent()) {
            throw new BusinessException("Mobile number is already registered");
        }

        OTPVerification otpVerification = otpVerificationRepository
                .findTopByMobileNumberAndVerifiedTrueOrderByIdDesc(request.getMobileNumber())
                .orElseThrow(() -> new BusinessException("Mobile number must be OTP verified first"));

        User newUser = User.builder()
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.PLAYER)
                .status(UserStatus.PENDING)
                .mobileVerified(true)
                .build();
                
        User savedUser = userRepository.save(newUser);
        webSocketNotificationService.notifyAdminDashboard("PLAYER_REGISTERED", "New player registered: " + savedUser.getFullName(), savedUser.getId());
        
        notificationService.createNotification(
                savedUser,
                "Welcome to Slot Booking App",
                "Welcome " + savedUser.getFullName() + "! Your account registration is successful. Status: PENDING admin approval.",
                com.slotbooking.modules.notification.enums.NotificationType.PLAYER_REGISTERED,
                com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                "USER",
                null
        );

        if (savedUser.getEmail() != null && !savedUser.getEmail().isBlank()) {
            notificationService.createNotification(
                    savedUser,
                    "Registration Successful - Awaiting Approval",
                    "Welcome " + savedUser.getFullName() + "! Your registration is successful. Account is PENDING admin approval.",
                    com.slotbooking.modules.notification.enums.NotificationType.PLAYER_REGISTERED,
                    com.slotbooking.modules.notification.enums.DeliveryChannel.EMAIL.name(),
                    com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                    "USER",
                    null
            );
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new BusinessException("Invalid mobile number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Invalid mobile number or password");
        }

        if (!user.isMobileVerified()) {
            throw new BusinessException("Mobile number is not verified");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessException("Account pending admin approval");
        } else if (user.getStatus() == UserStatus.BLOCKED || user.getStatus() == UserStatus.REJECTED) {
            throw new BusinessException("Account is not active");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .status(user.getStatus().name())
                .build();
    }
}
