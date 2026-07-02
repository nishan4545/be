package com.slotbooking.modules.booking.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.exception.ResourceNotFoundException;
import com.slotbooking.modules.booking.dto.BookingResponse;
import com.slotbooking.modules.booking.dto.BookingSummaryResponse;
import com.slotbooking.modules.booking.dto.CreateBookingRequest;
import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing player tournament bookings.
 * Employs pessimistic locking to prevent overbooking and enforces core business validations.
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;
    private final com.slotbooking.modules.notification.service.NotificationService notificationService;

    @Override
    @Transactional
    public BookingResponse joinTournament(User currentUser, CreateBookingRequest request) {
        // 1. Validate User details
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.PLAYER) {
            throw new BusinessException("Only players can join tournaments.");
        }
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new BusinessException("Booking is only allowed for approved players.");
        }
        if (!user.isMobileVerified()) {
            throw new BusinessException("Mobile number must be verified to book.");
        }

        // 2. Lock and retrieve Tournament row to prevent concurrent slot conflicts
        Tournament tournament = tournamentRepository.findByIdWithLock(request.getTournamentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + request.getTournamentId()));

        // 3. Validate Tournament State
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new BusinessException("Registration is not open for this tournament.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(tournament.getRegistrationStartDate()) || now.isAfter(tournament.getRegistrationEndDate())) {
            throw new BusinessException("Registration period for this tournament is not active.");
        }

        if (tournament.getAvailableSlots() <= 0) {
            throw new BusinessException("Tournament is full.");
        }

        // 4. Duplicate booking/payment retry validation
        List<Booking> activeBookings = bookingRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId());
        Booking pendingBooking = null;
        for (Booking b : activeBookings) {
            if (b.getStatus() == BookingStatus.CONFIRMED) {
                throw new BusinessException("You have already booked a slot in this tournament.");
            } else if (b.getStatus() == BookingStatus.PENDING_PAYMENT) {
                pendingBooking = b;
            }
        }
        if (pendingBooking != null) {
            return mapToBookingResponse(pendingBooking);
        }

        // 5. Create booking (initially set to PENDING_PAYMENT, awaiting payment verification)
        Booking booking = Booking.builder()
                .user(user)
                .tournament(tournament)
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        webSocketNotificationService.notifyAdminDashboard("BOOKING_CREATED", "New booking created for tournament: " + tournament.getTitle(), savedBooking.getId());

        // Notify user about booking creation
        notificationService.createNotification(
                user,
                "Booking Awaiting Payment",
                "Your slot booking in '" + tournament.getTitle() + "' is created. Please complete payment of " + tournament.getEntryFee() + " within 15 minutes to lock your slot.",
                com.slotbooking.modules.notification.enums.NotificationType.BOOKING_CREATED,
                com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                "BOOKING",
                savedBooking.getId()
        );

        notificationService.createNotification(
                user,
                "Booking Awaiting Payment",
                "Your slot booking in '" + tournament.getTitle() + "' is created. Please complete payment within 15 minutes to lock your slot.",
                com.slotbooking.modules.notification.enums.NotificationType.BOOKING_CREATED,
                com.slotbooking.modules.notification.enums.DeliveryChannel.SMS.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                "BOOKING",
                savedBooking.getId()
        );

        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(User currentUser, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // 1. Confirm ownership/access
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. You cannot cancel another player's booking.");
        }

        // 2. Validate cancellation rules
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException("Only CONFIRMED bookings can be cancelled.");
        }

        Tournament tournament = tournamentRepository.findByIdWithLock(booking.getTournament().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));

        LocalDateTime tournamentStart = LocalDateTime.of(tournament.getTournamentDate(), tournament.getStartTime());
        if (LocalDateTime.now().isAfter(tournamentStart)) {
            throw new BusinessException("Cannot cancel booking. The tournament has already started.");
        }

        // 3. Update status and restore slots
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        tournament.setAvailableSlots(tournament.getAvailableSlots() + 1);
        tournamentRepository.save(tournament);

        webSocketNotificationService.notifyBookingCancelled(booking);
        webSocketNotificationService.notifySlotUpdated(tournament);

        // Notify user about cancellation
        notificationService.createNotification(
                booking.getUser(),
                "Booking Cancelled",
                "Your booking in the tournament '" + tournament.getTitle() + "' has been successfully cancelled.",
                com.slotbooking.modules.notification.enums.NotificationType.BOOKING_CANCELLED,
                com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                "BOOKING",
                booking.getId()
        );

        if (booking.getUser().getEmail() != null && !booking.getUser().getEmail().isBlank()) {
            notificationService.createNotification(
                    booking.getUser(),
                    "Booking Cancelled - " + tournament.getTitle(),
                    "Hello, your slot booking for the tournament '" + tournament.getTitle() + "' has been cancelled successfully.",
                    com.slotbooking.modules.notification.enums.NotificationType.BOOKING_CANCELLED,
                    com.slotbooking.modules.notification.enums.DeliveryChannel.EMAIL.name(),
                    com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                    "BOOKING",
                    booking.getId()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getMyBookings(User currentUser) {
        return bookingRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingDetails(User currentUser, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("Access denied. You cannot view another player's booking details.");
        }

        return mapToBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getTournamentBookings(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found with id: " + tournamentId);
        }
        return bookingRepository.findByTournamentId(tournamentId).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getPlayerBookingHistory(Long playerId) {
        if (!userRepository.existsById(playerId)) {
            throw new ResourceNotFoundException("Player not found with id: " + playerId);
        }
        return bookingRepository.findByUserId(playerId).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    // Mapper helper methods

    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .playerFullName(booking.getUser().getFullName())
                .playerMobileNumber(booking.getUser().getMobileNumber())
                .tournamentId(booking.getTournament().getId())
                .tournamentTitle(booking.getTournament().getTitle())
                .tournamentDate(booking.getTournament().getTournamentDate())
                .status(booking.getStatus())
                .seatNumber(booking.getSeatNumber())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private BookingSummaryResponse mapToSummaryResponse(Booking booking) {
        return BookingSummaryResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .playerFullName(booking.getUser().getFullName())
                .playerMobileNumber(booking.getUser().getMobileNumber())
                .tournamentId(booking.getTournament().getId())
                .tournamentTitle(booking.getTournament().getTitle())
                .tournamentDate(booking.getTournament().getTournamentDate())
                .status(booking.getStatus())
                .seatNumber(booking.getSeatNumber())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
