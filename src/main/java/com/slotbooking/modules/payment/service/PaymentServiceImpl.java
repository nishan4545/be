package com.slotbooking.modules.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.slotbooking.exception.BusinessException;
import com.slotbooking.exception.ResourceNotFoundException;
import com.slotbooking.exception.ValidationException;
import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.payment.dto.CreateOrderRequest;
import com.slotbooking.modules.payment.dto.OrderResponse;
import com.slotbooking.modules.payment.dto.PaymentResponse;
import com.slotbooking.modules.payment.dto.PaymentVerificationRequest;
import com.slotbooking.modules.payment.entity.Payment;
import com.slotbooking.modules.payment.enums.Currency;
import com.slotbooking.modules.payment.enums.PaymentStatus;
import com.slotbooking.modules.payment.repository.PaymentRepository;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation managing the Razorpay payment lifecycle with strict idempotency and validation constraints.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TournamentRepository tournamentRepository;
    private final com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;
    private final com.slotbooking.modules.notification.service.NotificationService notificationService;
    private final RazorpayService razorpayService;

    @Value("${RAZORPAY_KEY_ID}")
    private String keyId;

    @Value("${RAZORPAY_KEY_SECRET}")
    private String keySecret;

    @Value("${RAZORPAY_WEBHOOK_SECRET}")
    private String webhookSecret;

    /**
     * Scheduled cleanup task running every minute.
     * Identifies pending payment bookings that are older than 15 minutes and transitions them to EXPIRED.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, threshold);
        
        for (Booking booking : expiredBookings) {
            log.info("[Structured Log] Processing expiration for booking ID: {}", booking.getId());
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            
            // Trigger WebSocket notification for player
            webSocketNotificationService.notifyBookingExpired(booking);

            // In-app Notification for timeout
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking Expired",
                    "Your booking for tournament '" + booking.getTournament().getTitle() + "' expired due to payment timeout.",
                    com.slotbooking.modules.notification.enums.NotificationType.BOOKING_EXPIRED,
                    com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                    com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                    "BOOKING",
                    booking.getId()
            );
            
            // Mark associated pending payment as CANCELLED
            Optional<Payment> paymentOpt = paymentRepository.findByBookingId(booking.getId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (payment.getStatus() == PaymentStatus.PENDING) {
                    payment.setStatus(PaymentStatus.CANCELLED);
                    paymentRepository.save(payment);
                }
            }
        }
    }

    @Override
    @Transactional
    public OrderResponse createOrder(User currentUser, CreateOrderRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        // Security check: ensure own booking
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. This booking does not belong to you.");
        }

        // Validate booking status
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException("Booking is not in PENDING_PAYMENT status.");
        }

        Tournament tournament = booking.getTournament();
        
        // Amount is converted to paise as required by Razorpay API
        BigDecimal entryFee = tournament.getEntryFee();
        int amountInPaise = entryFee.multiply(new BigDecimal("100")).intValue();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", Currency.INR.name());
            orderRequest.put("receipt", "receipt_booking_" + booking.getId());

            log.info("[Structured Log] Order Created Request - Booking ID: {}, Amount: {} paise", booking.getId(), amountInPaise);
            Order order = razorpayService.createOrder(orderRequest);
            String razorpayOrderId = order.get("id");

            // Update booking with Razorpay Order ID
            booking.setRazorpayOrderId(razorpayOrderId);
            bookingRepository.save(booking);

            // Fetch or create payment record
            Payment payment = paymentRepository.findByBookingId(booking.getId())
                    .orElseGet(() -> Payment.builder().booking(booking).build());

            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setAmount(entryFee);
            payment.setCurrency(Currency.INR);
            payment.setGateway("RAZORPAY");
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            log.info("[Structured Log] Order Created Successful - Razorpay Order ID: {}", razorpayOrderId);

            return OrderResponse.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .amount(entryFee)
                    .currency(Currency.INR.name())
                    .keyId(keyId)
                    .build();

        } catch (Exception e) {
            log.error("[Structured Log] Order Created Failed - booking: {}", booking.getId(), e);
            throw new BusinessException("Failed to initiate payment gateway order: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(User currentUser, PaymentVerificationRequest request) {
        // Prevent duplicate payment records
        if (paymentRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
            log.warn("[Structured Log] Duplicate payment attempt detected for Payment ID: {}", request.getRazorpayPaymentId());
            throw new BusinessException("Duplicate payment attempt detected.");
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order id: " + request.getRazorpayOrderId()));

        // Security check
        if (!payment.getBooking().getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. Payment is associated with another account.");
        }

        // Idempotency: Return if payment already completed
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("[Structured Log] Payment already confirmed for Order ID: {}. Returning successful payment.", request.getRazorpayOrderId());
            return mapToPaymentResponse(payment);
        }

        // Verify Razorpay Signature
        try {
            if ("YOUR_TEST_SECRET".equals(keySecret)) {
                log.info("[Structured Log] Mock Signature Verified - order: {}", request.getRazorpayOrderId());
            } else {
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", request.getRazorpayOrderId());
                options.put("razorpay_payment_id", request.getRazorpayPaymentId());
                options.put("razorpay_signature", request.getRazorpaySignature());

                boolean isValid = Utils.verifyPaymentSignature(options, keySecret);
                if (!isValid) {
                    log.error("[Structured Log] Signature Failed - order: {}", request.getRazorpayOrderId());
                    throw new ValidationException("Signature verification failed.");
                }
                log.info("[Structured Log] Signature Verified - order: {}", request.getRazorpayOrderId());
            }
        } catch (Exception e) {
            log.error("[Structured Log] Signature Failed - order: {} with exception", request.getRazorpayOrderId(), e);
            throw new ValidationException("Razorpay signature verification failed: " + e.getMessage());
        }

        // Update booking and slots transactionally
        confirmPaymentTransaction(payment, request.getRazorpayPaymentId(), request.getRazorpaySignature());

        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional
    public void confirmPaymentTransaction(String orderId, String paymentId, String signature) {
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order id: " + orderId));
        confirmPaymentTransaction(payment, paymentId, signature);
    }

    /**
     * Executes the payment success confirmation transaction.
     * Checks slots with a pessimistic write lock and updates tables.
     */
    private void confirmPaymentTransaction(Payment payment, String paymentId, String signature) {
        // Idempotency guard inside transaction
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("[Structured Log] Payment Already Processed (Idempotency) - Payment ID: {}", paymentId);
            return;
        }

        Booking booking = payment.getBooking();
        
        // Prevent payment verification for expired bookings
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            log.warn("[Structured Log] Payment Rejected - Booking ID {} has expired.", booking.getId());
            throw new BusinessException("Booking has expired. Cannot complete payment.");
        }

        Tournament tournament = tournamentRepository.findByIdWithLock(booking.getTournament().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        if (tournament.getAvailableSlots() <= 0) {
            log.warn("[Structured Log] Payment Failed - Tournament is full for booking ID: {}", booking.getId());
            
            // Note: Failures are stored strictly in Payment entity. Booking transitions to PENDING_PAYMENT or cancelled.
            // But we keep booking status unchanged or we can handle it cleanly.
            payment.setStatus(PaymentStatus.FAILED);
            payment.setRazorpayPaymentId(paymentId);
            payment.setRazorpaySignature(signature);
            paymentRepository.save(payment);

            throw new BusinessException("Tournament slots are full. Booking failed.");
        }

        // Successful slot reservation
        tournament.setAvailableSlots(tournament.getAvailableSlots() - 1);
        tournamentRepository.save(tournament);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setSeatNumber(tournament.getTotalSlots() - tournament.getAvailableSlots());
        bookingRepository.save(booking);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(paymentId);
        payment.setRazorpaySignature(signature);
        payment.setTransactionTime(LocalDateTime.now());
        paymentRepository.save(payment);

        // Trigger WebSocket notifications
        webSocketNotificationService.notifyPaymentSuccess(payment);
        webSocketNotificationService.notifyBookingConfirmed(booking);
        webSocketNotificationService.notifySlotUpdated(tournament);
        webSocketNotificationService.notifyAdminDashboard("PAYMENT_SUCCESS", "Payment successful for booking ID: " + booking.getId(), payment.getId());

        // Create in-app notifications
        notificationService.createNotification(
                booking.getUser(),
                "Payment Successful",
                "Your payment of " + payment.getAmount() + " is successful! Your slot in '" + tournament.getTitle() + "' is CONFIRMED.",
                com.slotbooking.modules.notification.enums.NotificationType.PAYMENT_SUCCESS,
                com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                "PAYMENT",
                payment.getId()
        );

        // SMS notification
        notificationService.createNotification(
                booking.getUser(),
                "Payment Successful",
                "Payment of " + payment.getAmount() + " is successful. Slot in '" + tournament.getTitle() + "' is CONFIRMED.",
                com.slotbooking.modules.notification.enums.NotificationType.PAYMENT_SUCCESS,
                com.slotbooking.modules.notification.enums.DeliveryChannel.SMS.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                "PAYMENT",
                payment.getId()
        );

        // Email notification
        if (booking.getUser().getEmail() != null && !booking.getUser().getEmail().isBlank()) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Payment Confirmation - " + tournament.getTitle(),
                    "Hello, your payment of " + payment.getAmount() + " for tournament '" + tournament.getTitle() + "' is successful. Your slot is CONFIRMED.",
                    com.slotbooking.modules.notification.enums.NotificationType.PAYMENT_SUCCESS,
                    com.slotbooking.modules.notification.enums.DeliveryChannel.EMAIL.name(),
                    com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                    "PAYMENT",
                    payment.getId()
            );
        }

        if (tournament.getAvailableSlots() == 0) {
            webSocketNotificationService.notifyAdminDashboard("TOURNAMENT_FULL", "Tournament full: " + tournament.getTitle(), tournament.getId());
        }

        log.info("[Structured Log] Payment Captured - Booking ID {} CONFIRMED. Slots remaining: {}", booking.getId(), tournament.getAvailableSlots());
    }

    @Override
    @Transactional
    public void processWebhook(String payload, String signature) {
        // Validate Webhook Signature
        try {
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                log.error("[Structured Log] Webhook Signature Failed");
                throw new ValidationException("Invalid webhook signature");
            }
        } catch (Exception e) {
            log.error("[Structured Log] Webhook Signature Failed exception", e);
            throw new ValidationException("Webhook signature verification failed");
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");
        JSONObject eventPayload = json.getJSONObject("payload");

        log.info("[Structured Log] Processing Razorpay Webhook Event: {}", event);

        if ("payment.captured".equals(event)) {
            JSONObject paymentEntity = eventPayload.getJSONObject("payment").getJSONObject("entity");
            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");
            
            Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(orderId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                // Idempotency: skip if already SUCCESS
                if (payment.getStatus() == PaymentStatus.SUCCESS) {
                    log.info("[Structured Log] Webhook Ignored - Payment ID {} already confirmed successfully.", paymentId);
                    return;
                }
                confirmPaymentTransaction(payment, paymentId, "webhook-captured-sig");
            }
        } else if ("payment.failed".equals(event)) {
            JSONObject paymentEntity = eventPayload.getJSONObject("payment").getJSONObject("entity");
            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");
            String errorDesc = paymentEntity.optString("error_description", "Payment failed via webhook event");
            
            handlePaymentFailure(orderId, paymentId, errorDesc);
        } else if ("refund.processed".equals(event)) {
            JSONObject refundEntity = eventPayload.getJSONObject("refund").getJSONObject("entity");
            String paymentId = refundEntity.getString("payment_id");
            
            Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(refundEntity.getString("order_id"));
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                // Idempotency: skip if already REFUNDED
                if (payment.getStatus() == PaymentStatus.REFUNDED) {
                    log.info("[Structured Log] Webhook Ignored - Payment ID {} already refunded.", paymentId);
                    return;
                }
                processRefundTransaction(payment);
            }
        }
    }

    @Override
    @Transactional
    public void handlePaymentFailure(String orderId, String paymentId, String reason) {
        log.warn("[Structured Log] Payment Failed - Order ID: {}, Payment ID: {}, Reason: {}", orderId, paymentId, reason);

        Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(orderId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            // Idempotency
            if (payment.getStatus() == PaymentStatus.FAILED) {
                log.info("[Structured Log] Failure logic already processed for Order ID: {}", orderId);
                return;
            }
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setRazorpayPaymentId(paymentId);
                paymentRepository.save(payment);
                // Booking status remains unchanged (PENDING_PAYMENT) per instructions that failures belong to Payment entity.
                
                // Trigger WebSocket notifications
                webSocketNotificationService.notifyPaymentFailed(payment);
                webSocketNotificationService.notifyAdminDashboard("PAYMENT_FAILED", "Payment failed for order ID: " + orderId, payment.getId());

                notificationService.createNotification(
                        payment.getBooking().getUser(),
                        "Payment Failed",
                        "Your payment attempt for tournament '" + payment.getBooking().getTournament().getTitle() + "' failed. Reason: " + reason,
                        com.slotbooking.modules.notification.enums.NotificationType.PAYMENT_FAILED,
                        com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                        com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                        "PAYMENT",
                        payment.getId()
                );
                
                log.info("[Structured Log] Payment marked as FAILED. Booking status remains unchanged.");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPaymentHistory(User currentUser) {
        return paymentRepository.findByBookingUserId(currentUser.getId()).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetails(User currentUser, Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with id: " + id));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isAdmin && !payment.getBooking().getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. You cannot access this payment record.");
        }

        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getTournamentPayments(Long tournamentId) {
        return paymentRepository.findByBookingTournamentId(tournamentId).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPlayerPayments(Long playerId) {
        return paymentRepository.findByBookingUserId(playerId).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processRefund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        Booking booking = payment.getBooking();
        if (booking == null) {
            throw new ResourceNotFoundException("Booking associated with this payment not found.");
        }

        // Refund guards
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException("Refund is only allowed for successful payment transactions.");
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BusinessException("Payment has already been refunded.");
        }

        log.info("[Structured Log] Refund Created - Payment ID: {}", payment.getRazorpayPaymentId());
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("payment_id", payment.getRazorpayPaymentId());
            
            // Invoke SDK Payment Refund API
            razorpayService.refundPayment(payment.getRazorpayPaymentId(), refundRequest);
            log.info("[Structured Log] Refund Completed - Razorpay refund API executed successfully.");
            
            processRefundTransaction(payment);

        } catch (Exception e) {
            log.error("[Structured Log] Refund Failed - Payment ID: {}", payment.getRazorpayPaymentId(), e);
            throw new BusinessException("Refund gateway execution failed: " + e.getMessage());
        }
    }

    /**
     * Executes the refund state transition transaction.
     * Restores slots and updates tables.
     */
    private void processRefundTransaction(Payment payment) {
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.REFUNDED);
        bookingRepository.save(booking);

        Tournament tournament = tournamentRepository.findByIdWithLock(booking.getTournament().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        tournament.setAvailableSlots(tournament.getAvailableSlots() + 1);
        tournamentRepository.save(tournament);

        // Trigger WebSocket notifications
        webSocketNotificationService.notifyRefund(payment);
        webSocketNotificationService.notifyBookingCancelled(booking);
        webSocketNotificationService.notifySlotUpdated(tournament);
        webSocketNotificationService.notifyAdminDashboard("REFUND_COMPLETED", "Refund completed for payment ID: " + payment.getId(), payment.getId());

        // Create in-app notifications
        notificationService.createNotification(
                booking.getUser(),
                "Refund Processed",
                "A refund of " + payment.getAmount() + " has been successfully processed for tournament '" + tournament.getTitle() + "'.",
                com.slotbooking.modules.notification.enums.NotificationType.REFUND_SUCCESS,
                com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                "PAYMENT",
                payment.getId()
        );

        if (booking.getUser().getEmail() != null && !booking.getUser().getEmail().isBlank()) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Refund Confirmation - " + tournament.getTitle(),
                    "Hello, a refund of " + payment.getAmount() + " for your slot booking in tournament '" + tournament.getTitle() + "' has been processed successfully.",
                    com.slotbooking.modules.notification.enums.NotificationType.REFUND_SUCCESS,
                    com.slotbooking.modules.notification.enums.DeliveryChannel.EMAIL.name(),
                    com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                    "PAYMENT",
                    payment.getId()
            );
        }

        log.info("[Structured Log] Refund Completed - Slot restored. Booking marked as REFUNDED.");
    }

    // Mapper helper method

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .tournamentId(payment.getBooking().getTournament().getId())
                .tournamentTitle(payment.getBooking().getTournament().getTitle())
                .playerFullName(payment.getBooking().getUser().getFullName())
                .playerMobileNumber(payment.getBooking().getUser().getMobileNumber())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .razorpaySignature(payment.getRazorpaySignature())
                .amount(payment.getAmount())
                .currency(payment.getCurrency() != null ? payment.getCurrency().name() : null)
                .status(payment.getStatus())
                .gateway(payment.getGateway())
                .transactionTime(payment.getTransactionTime())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
