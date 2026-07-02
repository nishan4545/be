package com.slotbooking.modules.payment.entity;

import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.payment.enums.PaymentStatus;
import com.slotbooking.modules.payment.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(unique = true)
    private String razorpayPaymentId;

    @Column(unique = true, nullable = false)
    private String razorpayOrderId;
    private String razorpaySignature;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private Currency currency;
    private String gateway;
    private LocalDateTime transactionTime;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
