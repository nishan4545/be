package com.slotbooking.modules.auth.service;

import com.slotbooking.modules.auth.entity.OTPVerification;
import com.slotbooking.modules.auth.repository.OTPVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockOtpServiceImpl implements OtpService {

    private final OTPVerificationRepository otpVerificationRepository;

    @Override
    public String generateOtp(String mobileNumber) {
        String otp = "123456"; // Mock OTP
        
        OTPVerification verification = OTPVerification.builder()
                .mobileNumber(mobileNumber)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();
                
        otpVerificationRepository.save(verification);
        return otp;
    }

    @Override
    public void sendOtp(String mobileNumber, String otp) {
        log.info("MOCK: Sending OTP {} to mobile number {}", otp, mobileNumber);
    }

    @Override
    public boolean verifyOtp(String mobileNumber, String otp) {
        return otpVerificationRepository.findTopByMobileNumberAndVerifiedFalseOrderByIdDesc(mobileNumber)
                .map(verification -> {
                    if (verification.getOtp().equals(otp) && verification.getExpiresAt().isAfter(LocalDateTime.now())) {
                        verification.setVerified(true);
                        otpVerificationRepository.save(verification);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
