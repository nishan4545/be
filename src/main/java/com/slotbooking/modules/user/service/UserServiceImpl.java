package com.slotbooking.modules.user.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.exception.ResourceNotFoundException;
import com.slotbooking.exception.ValidationException;
import com.slotbooking.modules.user.dto.*;
import com.slotbooking.modules.user.entity.BankAccount;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.BankAccountRepository;
import com.slotbooking.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service implementation for UserService.
 * Manages admin player operations and player profiles/bank details with security and audit controls.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;
    private final com.slotbooking.modules.notification.service.NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getPlayerList(
            String search,
            UserStatus status,
            Role role,
            Pageable pageable
    ) {
        String searchKeyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<User> users = userRepository.findAllUsers(searchKeyword, status, role, pageable);
        return users.map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsResponse getPlayerDetails(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        BankAccount bankAccount = bankAccountRepository.findByUserId(user.getId()).orElse(null);
        return mapToDetailsResponse(user, bankAccount);
    }

    @Override
    @Transactional
    public void approvePlayer(Long id, User admin) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessException("Only Pending users can be approved.");
        }

        user.setStatus(UserStatus.APPROVED);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(admin);
        
        String adminIdentifier = (admin.getFullName() != null && !admin.getFullName().isBlank()) 
                ? admin.getFullName() 
                : admin.getMobileNumber();
        user.setUpdatedBy(adminIdentifier);

        userRepository.save(user);
        webSocketNotificationService.notifyAdminDashboard("PLAYER_APPROVED", "Player approved: " + user.getFullName(), user.getId());

        notificationService.createNotification(
                user,
                "Account Approved",
                "Congratulations " + user.getFullName() + "! Your account has been approved by the administrator. You can now book tournament slots.",
                com.slotbooking.modules.notification.enums.NotificationType.PLAYER_APPROVED,
                com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                "USER",
                null
        );

        notificationService.createNotification(
                user,
                "Account Approved",
                "Congratulations " + user.getFullName() + "! Your account has been approved by the administrator. You can now book tournament slots.",
                com.slotbooking.modules.notification.enums.NotificationType.PLAYER_APPROVED,
                com.slotbooking.modules.notification.enums.DeliveryChannel.SMS.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                "USER",
                null
        );

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            notificationService.createNotification(
                    user,
                    "Account Approved - Slot Booking App",
                    "Congratulations " + user.getFullName() + "! Your account has been approved by the administrator. You can now log in and book tournament slots.",
                    com.slotbooking.modules.notification.enums.NotificationType.PLAYER_APPROVED,
                    com.slotbooking.modules.notification.enums.DeliveryChannel.EMAIL.name(),
                    com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                    "USER",
                    null
            );
        }
    }

    @Override
    @Transactional
    public void rejectPlayer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot reject an admin user.");
        }

        user.setStatus(UserStatus.REJECTED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void blockPlayer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("Cannot block an admin user.");
        }

        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        webSocketNotificationService.notifyAdminDashboard("PLAYER_BLOCKED", "Player blocked: " + user.getFullName(), user.getId());
    }

    @Override
    @Transactional
    public void unblockPlayer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setStatus(UserStatus.APPROVED);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsResponse getProfile(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BankAccount bankAccount = bankAccountRepository.findByUserId(user.getId()).orElse(null);
        return mapToDetailsResponse(user, bankAccount);
    }

    @Override
    @Transactional
    public UserDetailsResponse updateProfile(User currentUser, UserProfileUpdateRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name must not be blank");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email must not be blank");
        }
        if (!request.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new ValidationException("Invalid email format");
        }

        String modifier = (currentUser.getFullName() != null && !currentUser.getFullName().isBlank())
                ? currentUser.getFullName()
                : currentUser.getMobileNumber();

        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim());
        if (request.getProfileImage() != null) {
            user.setProfilePhoto(request.getProfileImage().trim());
        }
        user.setUpdatedBy(modifier);

        userRepository.save(user);
        BankAccount bankAccount = bankAccountRepository.findByUserId(user.getId()).orElse(null);
        return mapToDetailsResponse(user, bankAccount);
    }

    @Override
    @Transactional
    public BankAccountResponse addBankDetails(User currentUser, BankDetailsRequest request) {
        validateBankDetailsRequest(request);

        // Check duplicate account number
        Optional<BankAccount> existingAccount = bankAccountRepository.findByAccountNumber(request.getAccountNumber().trim());
        if (existingAccount.isPresent() && !existingAccount.get().getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("Bank account number is already registered by another user.");
        }

        BankAccount bankAccount = bankAccountRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> BankAccount.builder().user(currentUser).build());

        String modifier = (currentUser.getFullName() != null && !currentUser.getFullName().isBlank())
                ? currentUser.getFullName()
                : currentUser.getMobileNumber();

        bankAccount.setBankAccountHolderName(request.getAccountHolderName().trim());
        bankAccount.setBankName(request.getBankName().trim());
        bankAccount.setAccountNumber(request.getAccountNumber().trim());
        bankAccount.setIfscCode(request.getIfscCode().trim());
        bankAccount.setBranchName(request.getBranchName().trim());
        bankAccount.setUpdatedBy(modifier);

        BankAccount saved = bankAccountRepository.save(bankAccount);
        return mapToBankAccountResponse(saved);
    }

    @Override
    @Transactional
    public BankAccountResponse addUpiDetails(User currentUser, UpiDetailsRequest request) {
        validateUpiDetailsRequest(request);

        // Check duplicate UPI
        Optional<BankAccount> existingUpi = bankAccountRepository.findByUpiId(request.getUpiId().trim());
        if (existingUpi.isPresent() && !existingUpi.get().getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("UPI ID is already registered by another user.");
        }

        BankAccount bankAccount = bankAccountRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> BankAccount.builder().user(currentUser).build());

        String modifier = (currentUser.getFullName() != null && !currentUser.getFullName().isBlank())
                ? currentUser.getFullName()
                : currentUser.getMobileNumber();

        bankAccount.setUpiId(request.getUpiId().trim());
        bankAccount.setUpiAccountHolderName(request.getAccountHolderName().trim());
        bankAccount.setUpdatedBy(modifier);

        BankAccount saved = bankAccountRepository.save(bankAccount);
        return mapToBankAccountResponse(saved);
    }

    @Override
    @Transactional
    public BankAccountResponse updateBankDetails(User currentUser, Long id, BankDetailsRequest request) {
        validateBankDetailsRequest(request);

        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + id));

        // Security check: ensure own bank details
        if (!bankAccount.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. You cannot modify another player's bank information.");
        }

        // Check duplicate account number
        Optional<BankAccount> existingAccount = bankAccountRepository.findByAccountNumber(request.getAccountNumber().trim());
        if (existingAccount.isPresent() && !existingAccount.get().getId().equals(id)) {
            throw new ValidationException("Bank account number is already registered by another user.");
        }

        String modifier = (currentUser.getFullName() != null && !currentUser.getFullName().isBlank())
                ? currentUser.getFullName()
                : currentUser.getMobileNumber();

        bankAccount.setBankAccountHolderName(request.getAccountHolderName().trim());
        bankAccount.setBankName(request.getBankName().trim());
        bankAccount.setAccountNumber(request.getAccountNumber().trim());
        bankAccount.setIfscCode(request.getIfscCode().trim());
        bankAccount.setBranchName(request.getBranchName().trim());
        bankAccount.setUpdatedBy(modifier);

        BankAccount saved = bankAccountRepository.save(bankAccount);
        return mapToBankAccountResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBankDetails(User currentUser, Long id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + id));

        // Security check: ensure own bank details
        if (!bankAccount.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. You cannot modify another player's bank information.");
        }

        String modifier = (currentUser.getFullName() != null && !currentUser.getFullName().isBlank())
                ? currentUser.getFullName()
                : currentUser.getMobileNumber();

        // Clear bank details
        bankAccount.setBankAccountHolderName(null);
        bankAccount.setBankName(null);
        bankAccount.setAccountNumber(null);
        bankAccount.setIfscCode(null);
        bankAccount.setBranchName(null);
        bankAccount.setUpdatedBy(modifier);

        // If UPI info is also null/empty, we delete the entity entirely
        if (bankAccount.getUpiId() == null || bankAccount.getUpiId().trim().isEmpty()) {
            bankAccountRepository.delete(bankAccount);
        } else {
            bankAccountRepository.save(bankAccount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountResponse getBankDetails(User currentUser) {
        Optional<BankAccount> bankAccountOpt = bankAccountRepository.findByUserId(currentUser.getId());
        if (bankAccountOpt.isEmpty()) {
            return BankAccountResponse.builder().build(); // Return empty response DTO instead of throwing error for better UX
        }
        return mapToBankAccountResponse(bankAccountOpt.get());
    }

    // Helper methods for validation and DTO mapping

    private void validateBankDetailsRequest(BankDetailsRequest request) {
        if (request.getAccountHolderName() == null || request.getAccountHolderName().trim().isEmpty()) {
            throw new ValidationException("Account holder name must not be blank");
        }
        if (request.getBankName() == null || request.getBankName().trim().isEmpty()) {
            throw new ValidationException("Bank name must not be blank");
        }
        if (request.getAccountNumber() == null || request.getAccountNumber().trim().isEmpty()) {
            throw new ValidationException("Account number must not be blank");
        }
        if (request.getIfscCode() == null || request.getIfscCode().trim().isEmpty()) {
            throw new ValidationException("IFSC code must not be blank");
        }
        if (!request.getIfscCode().trim().matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            throw new ValidationException("Invalid IFSC format");
        }
        if (request.getBranchName() == null || request.getBranchName().trim().isEmpty()) {
            throw new ValidationException("Branch name must not be blank");
        }
    }

    private void validateUpiDetailsRequest(UpiDetailsRequest request) {
        if (request.getAccountHolderName() == null || request.getAccountHolderName().trim().isEmpty()) {
            throw new ValidationException("Account holder name must not be blank");
        }
        if (request.getUpiId() == null || request.getUpiId().trim().isEmpty()) {
            throw new ValidationException("UPI ID must not be blank");
        }
        if (!request.getUpiId().trim().matches("^[\\w.-]+@[\\w.-]+$")) {
            throw new ValidationException("Invalid UPI format");
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return null;
        }
        String trimmed = accountNumber.trim();
        if (trimmed.length() <= 4) {
            return "XXXX " + trimmed;
        }
        return "XXXX XXXX " + trimmed.substring(trimmed.length() - 4);
    }

    private UserSummaryResponse mapToSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserDetailsResponse mapToDetailsResponse(User user, BankAccount bankAccount) {
        String approvedByStr = null;
        if (user.getApprovedBy() != null) {
            approvedByStr = (user.getApprovedBy().getFullName() != null && !user.getApprovedBy().getFullName().isBlank())
                    ? user.getApprovedBy().getFullName()
                    : user.getApprovedBy().getMobileNumber();
        }

        UserDetailsResponse.UserDetailsResponseBuilder builder = UserDetailsResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .profilePhoto(user.getProfilePhoto())
                .createdAt(user.getCreatedAt())
                .approvedAt(user.getApprovedAt())
                .approvedBy(approvedByStr);

        if (bankAccount != null) {
            builder.bankAccountHolderName(bankAccount.getBankAccountHolderName())
                    .bankName(bankAccount.getBankName())
                    .accountNumber(maskAccountNumber(bankAccount.getAccountNumber()))
                    .ifscCode(bankAccount.getIfscCode())
                    .branchName(bankAccount.getBranchName())
                    .upiId(bankAccount.getUpiId())
                    .upiAccountHolderName(bankAccount.getUpiAccountHolderName());
        }

        return builder.build();
    }

    private BankAccountResponse mapToBankAccountResponse(BankAccount bankAccount) {
        return BankAccountResponse.builder()
                .id(bankAccount.getId())
                .bankAccountHolderName(bankAccount.getBankAccountHolderName())
                .bankName(bankAccount.getBankName())
                .accountNumber(maskAccountNumber(bankAccount.getAccountNumber()))
                .ifscCode(bankAccount.getIfscCode())
                .branchName(bankAccount.getBranchName())
                .upiId(bankAccount.getUpiId())
                .upiAccountHolderName(bankAccount.getUpiAccountHolderName())
                .build();
    }
}
