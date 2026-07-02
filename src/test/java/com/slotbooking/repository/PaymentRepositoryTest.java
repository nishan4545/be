package com.slotbooking.repository;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.payment.entity.Payment;
import com.slotbooking.modules.payment.enums.PaymentStatus;
import com.slotbooking.modules.payment.repository.PaymentRepository;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class PaymentRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .mobileNumber("9876543244")
                .password("pass")
                .fullName("Bob")
                .mobileVerified(true)
                .build();
        user = userRepository.save(user);

        Tournament tournament = Tournament.builder()
                .title("Masters")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(1))
                .tournamentDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .totalSlots(8)
                .availableSlots(8)
                .entryFee(new BigDecimal("100.00"))
                .status(TournamentStatus.REGISTRATION_OPEN)
                .build();
        tournament = tournamentRepository.save(tournament);

        booking = Booking.builder()
                .user(user)
                .tournament(tournament)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();
        booking = bookingRepository.save(booking);

        payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_abc123")
                .build();
        payment = paymentRepository.save(payment);
    }

    @Test
    void testFindByRazorpayOrderId() {
        Optional<Payment> found = paymentRepository.findByRazorpayOrderId("order_abc123");
        assertTrue(found.isPresent());
        assertEquals(payment.getId(), found.get().getId());
    }

    @Test
    void testUniqueRazorpayOrderIdConstraint() {
        Payment duplicate = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_abc123") // Duplicate order ID
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            paymentRepository.saveAndFlush(duplicate);
        });
    }
}
