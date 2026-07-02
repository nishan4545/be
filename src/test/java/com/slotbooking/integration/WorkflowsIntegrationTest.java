package com.slotbooking.integration;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.auth.entity.OTPVerification;
import com.slotbooking.modules.auth.repository.OTPVerificationRepository;
import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.payment.entity.Payment;
import com.slotbooking.modules.payment.enums.PaymentStatus;
import com.slotbooking.modules.payment.repository.PaymentRepository;
import com.slotbooking.modules.payment.service.PaymentService;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class WorkflowsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OTPVerificationRepository otpVerificationRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    private User adminUser;
    private User playerUser;
    private Tournament tournament;

    @BeforeEach
    void setupData() {
        adminUser = User.builder()
                .mobileNumber("9999999999")
                .password("adminpass")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .status(UserStatus.APPROVED)
                .mobileVerified(true)
                .build();
        userRepository.save(adminUser);

        playerUser = User.builder()
                .mobileNumber("9876543210")
                .password("playerpass")
                .fullName("Player User")
                .role(Role.PLAYER)
                .status(UserStatus.APPROVED)
                .mobileVerified(true)
                .build();
        userRepository.save(playerUser);

        tournament = Tournament.builder()
                .title("Integration Tourney")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(2))
                .tournamentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(18, 0))
                .totalSlots(10)
                .availableSlots(10)
                .entryFee(new BigDecimal("150.00"))
                .prizeAmount(new BigDecimal("1500.00"))
                .winnerCount(1)
                .status(TournamentStatus.REGISTRATION_OPEN)
                .build();
        tournamentRepository.save(tournament);
    }

    @Test
    void testWorkflow1_PlayerLifeCycle() {
        // 1. Mobile OTP generation and validation check
        OTPVerification verification = OTPVerification.builder()
                .mobileNumber("9000000001")
                .otp("123456")
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(5))
                .verified(true)
                .build();
        otpVerificationRepository.save(verification);

        // 2. Register Player user
        User newPlayer = User.builder()
                .mobileNumber("9000000001")
                .password("player_pass")
                .fullName("New Player")
                .role(Role.PLAYER)
                .status(UserStatus.PENDING)
                .mobileVerified(true)
                .build();
        User saved = userRepository.save(newPlayer);
        assertNotNull(saved.getId());

        // 3. Admin approval check
        saved.setStatus(UserStatus.APPROVED);
        User approved = userRepository.save(saved);
        assertEquals(UserStatus.APPROVED, approved.getStatus());
    }

    @Test
    void testWorkflow2_BookingAndPaymentConfirmation() {
        // 1. Player Books slot
        Booking booking = Booking.builder()
                .user(playerUser)
                .tournament(tournament)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();
        booking = bookingRepository.save(booking);
        
        tournament.setAvailableSlots(tournament.getAvailableSlots() - 1);
        tournamentRepository.save(tournament);

        // 2. Create Payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_xyz123")
                .build();
        payment = paymentRepository.save(payment);

        // 3. Mock Webhook success capture callback
        paymentService.confirmPaymentTransaction("order_xyz123", "pay_xyz123", "sig_xyz123");

        // 4. Verify confirmations
        Optional<Payment> confirmedPayment = paymentRepository.findById(payment.getId());
        assertTrue(confirmedPayment.isPresent());
        assertEquals(PaymentStatus.SUCCESS, confirmedPayment.get().getStatus());

        Optional<Booking> confirmedBooking = bookingRepository.findById(booking.getId());
        assertTrue(confirmedBooking.isPresent());
        assertEquals(BookingStatus.CONFIRMED, confirmedBooking.get().getStatus());
    }
}
