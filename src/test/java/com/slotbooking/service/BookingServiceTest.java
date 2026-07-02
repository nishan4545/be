package com.slotbooking.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.modules.booking.dto.BookingResponse;
import com.slotbooking.modules.booking.dto.CreateBookingRequest;
import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.booking.service.BookingServiceImpl;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;

    @Mock
    private com.slotbooking.modules.notification.service.NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User playerUser;
    private Tournament openTournament;
    private Booking pendingBooking;

    @BeforeEach
    void setUp() {
        playerUser = User.builder()
                .id(1L)
                .mobileNumber("9876543210")
                .status(UserStatus.APPROVED)
                .role(Role.PLAYER)
                .mobileVerified(true)
                .build();

        openTournament = Tournament.builder()
                .id(1L)
                .title("Open Tournament")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(1))
                .tournamentDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(16, 0))
                .totalSlots(10)
                .availableSlots(10)
                .entryFee(new BigDecimal("200.00"))
                .status(TournamentStatus.REGISTRATION_OPEN)
                .build();

        pendingBooking = Booking.builder()
                .id(100L)
                .user(playerUser)
                .tournament(openTournament)
                .status(BookingStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void joinTournament_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(playerUser));
        when(tournamentRepository.findByIdWithLock(1L)).thenReturn(Optional.of(openTournament));
        when(bookingRepository.findByUserIdAndTournamentId(eq(1L), eq(1L))).thenReturn(java.util.List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);

        BookingResponse response = bookingService.joinTournament(playerUser, new CreateBookingRequest(1L));
        assertNotNull(response);
        assertEquals(BookingStatus.PENDING_PAYMENT, response.getStatus());
        assertEquals(10, openTournament.getAvailableSlots());
    }

    @Test
    void joinTournament_unapprovedUser_throwsException() {
        playerUser.setStatus(UserStatus.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(playerUser));

        assertThrows(BusinessException.class, () -> bookingService.joinTournament(playerUser, new CreateBookingRequest(1L)));
    }

    @Test
    void joinTournament_registrationClosed_throwsException() {
        openTournament.setStatus(TournamentStatus.REGISTRATION_CLOSED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(playerUser));
        when(tournamentRepository.findByIdWithLock(1L)).thenReturn(Optional.of(openTournament));

        assertThrows(BusinessException.class, () -> bookingService.joinTournament(playerUser, new CreateBookingRequest(1L)));
    }

    @Test
    void cancelBooking_success() {
        pendingBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(pendingBooking));
        when(tournamentRepository.findByIdWithLock(1L)).thenReturn(Optional.of(openTournament));
        when(bookingRepository.save(any(Booking.class))).thenReturn(pendingBooking);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(openTournament);

        bookingService.cancelBooking(playerUser, 100L);
        assertEquals(BookingStatus.CANCELLED, pendingBooking.getStatus());
    }
}
