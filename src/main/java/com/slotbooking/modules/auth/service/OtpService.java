package com.slotbooking.modules.auth.service;

public interface OtpService {
    String generateOtp(String mobileNumber);
    void sendOtp(String mobileNumber, String otp);
    boolean verifyOtp(String mobileNumber, String otp);
}
