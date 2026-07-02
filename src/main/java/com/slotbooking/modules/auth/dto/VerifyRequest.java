package com.slotbooking.modules.auth.dto;
import lombok.Data;
@Data
public class VerifyRequest { private String mobileNumber; private String otp; }
