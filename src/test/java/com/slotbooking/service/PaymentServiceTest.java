package com.slotbooking.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.payment.dto.CreateOrderRequest;
import com.slotbooking.modules.payment.dto.OrderResponse;
import com.slotbooking.modules.payment.entity.Payment;
import com.slotbooking.modules.payment.enums.PaymentStatus;
import com.slotbooking.modules.payment.repository.PaymentRepository;
import com.slotbooking.modules.payment.service.PaymentServiceImpl;
import com.slotbooking.modules.payment.service.RazorpayService;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.user.entity.User;
import com.razorpay.Order;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private com.slotbooking.modules.tournament.repository.TournamentRepository tournamentRepository;

    @Mock
    private com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;

    @Mock
    private com.slotbooking.modules.notification.service.NotificationService notificationService;

    @Mock
    private RazorpayService razorpayService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User currentUser;
    private Booking pendingBooking;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).mobileNumber("9876543210").build();
        Tournament tournament = Tournament.builder()
                .id(1L)
                .title("Pro Tournament")
                .entryFee(new BigDecimal("300.00"))
                .availableSlots(5)
                .build();

        pendingBooking = Booking.builder()
                .id(100L)
                .user(currentUser)
                .tournament(tournament)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        pendingPayment = Payment.builder()
                .id(1L)
                .booking(pendingBooking)
                .amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_mock123")
                .build();
    }

    @Test
    void createOrder_success() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder().bookingId(100L).build();
        // Build a real Razorpay Order object from JSON (avoids mocking a final class on JDK 25)
        com.razorpay.Order razorpayOrder = new com.razorpay.Order(new org.json.JSONObject("{\"id\":\"order_mock123\"}"));

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(pendingBooking));
        when(razorpayService.createOrder(any(JSONObject.class))).thenReturn(razorpayOrder);
        when(paymentRepository.findByBookingId(100L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);

        OrderResponse response = paymentService.createOrder(currentUser, request);
        assertNotNull(response);
        assertEquals("order_mock123", response.getRazorpayOrderId());
    }

    @Test
    void confirmPaymentTransaction_duplicateCheck() {
        pendingPayment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByRazorpayOrderId("order_mock123")).thenReturn(Optional.of(pendingPayment));

        // Attempting to confirm a success payment transaction should run cleanly without duplicate updates.
        paymentService.confirmPaymentTransaction("order_mock123", "pay_mock123", "sig_mock123");
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
