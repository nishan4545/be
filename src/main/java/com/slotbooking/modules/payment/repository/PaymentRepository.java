package com.slotbooking.modules.payment.repository;

import com.slotbooking.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import com.slotbooking.modules.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.user JOIN FETCH b.tournament WHERE p.razorpayOrderId = :orderId")
    Optional<Payment> findByRazorpayOrderId(@Param("orderId") String orderId);

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByBookingUserId(Long userId);

    List<Payment> findByBookingTournamentId(Long tournamentId);

    boolean existsByRazorpayOrderId(String orderId);

    boolean existsByRazorpayPaymentId(String paymentId);

    long countByStatusAndCreatedAtAfter(PaymentStatus status, LocalDateTime startOfDay);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt >= :startOfDay")
    BigDecimal sumRevenueSince(@Param("startOfDay") LocalDateTime startOfDay);
}
