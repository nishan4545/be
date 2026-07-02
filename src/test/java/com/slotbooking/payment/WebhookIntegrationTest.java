package com.slotbooking.payment;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class WebhookIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment pendingPayment;
    private Booking booking;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .mobileNumber("9876543288")
                .password("pass")
                .fullName("WebHookUser")
                .mobileVerified(true)
                .build();
        user = userRepository.save(user);

        Tournament tournament = Tournament.builder()
                .title("Webhook Cup")
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

        pendingPayment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_webhook123")
                .build();
        pendingPayment = paymentRepository.save(pendingPayment);
    }

    @Test
    void testProcessWebhookCaptured_success() throws Exception {
        String payload = "{\n" +
                "  \"event\": \"payment.captured\",\n" +
                "  \"payload\": {\n" +
                "    \"payment\": {\n" +
                "      \"entity\": {\n" +
                "        \"id\": \"pay_captured123\",\n" +
                "        \"order_id\": \"order_webhook123\",\n" +
                "        \"status\": \"captured\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        mockMvc.perform(post("/api/webhooks/razorpay")
                .header("x-razorpay-signature", "validsignature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        Payment updatedPayment = paymentRepository.findById(pendingPayment.getId()).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());
        assertEquals("pay_captured123", updatedPayment.getRazorpayPaymentId());

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, updatedBooking.getStatus());
    }
}
