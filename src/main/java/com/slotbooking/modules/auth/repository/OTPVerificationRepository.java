package com.slotbooking.modules.auth.repository;

import com.slotbooking.modules.auth.entity.OTPVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OTPVerificationRepository extends JpaRepository<OTPVerification, Long> {
    Optional<OTPVerification> findTopByMobileNumberAndVerifiedFalseOrderByIdDesc(String mobileNumber);
    Optional<OTPVerification> findTopByMobileNumberAndVerifiedTrueOrderByIdDesc(String mobileNumber);
}
