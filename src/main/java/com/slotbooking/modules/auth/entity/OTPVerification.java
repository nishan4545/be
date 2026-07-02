package com.slotbooking.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTPVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String mobileNumber;
    
    @Column(nullable = false)
    private String otp;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    private boolean verified;
}
