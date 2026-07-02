package com.slotbooking.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.modules.tournament.dto.CreateTournamentRequest;
import com.slotbooking.modules.tournament.dto.TournamentResponse;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.tournament.service.TournamentService;
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
public class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;

    @Mock
    private com.slotbooking.modules.notification.service.NotificationService notificationService;

    @Mock
    private com.slotbooking.modules.booking.repository.BookingRepository bookingRepository;

    @Mock
    private com.slotbooking.modules.user.repository.UserRepository userRepository;

    @InjectMocks
    private TournamentService tournamentService;

    private Tournament upcomingTournament;

    @BeforeEach
    void setUp() {
        upcomingTournament = Tournament.builder()
                .id(1L)
                .title("Championship 2026")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(2))
                .tournamentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .totalSlots(20)
                .availableSlots(20)
                .entryFee(new BigDecimal("500.00"))
                .prizeAmount(new BigDecimal("5000.00"))
                .winnerCount(3)
                .status(TournamentStatus.UPCOMING)
                .build();
    }

    @Test
    void createTournament_success() {
        CreateTournamentRequest request = CreateTournamentRequest.builder()
                .title("New Cup")
                .registrationStartDate(LocalDateTime.now().plusHours(1))
                .registrationEndDate(LocalDateTime.now().plusDays(2))
                .tournamentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .totalSlots(50)
                .entryFee(new BigDecimal("100.00"))
                .prizeAmount(new BigDecimal("1000.00"))
                .winnerCount(1)
                .build();

        when(tournamentRepository.save(any(Tournament.class))).thenReturn(upcomingTournament);

        TournamentResponse response = tournamentService.createTournament(request, 1L);
        assertNotNull(response);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void openRegistration_success() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(upcomingTournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(upcomingTournament);

        TournamentResponse response = tournamentService.openRegistration(1L);
        assertNotNull(response);
        assertEquals(TournamentStatus.REGISTRATION_OPEN, upcomingTournament.getStatus());
    }

    @Test
    void cancelTournament_success() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(upcomingTournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(upcomingTournament);

        TournamentResponse response = tournamentService.cancelTournament(1L);
        assertNotNull(response);
        assertEquals(TournamentStatus.CANCELLED, upcomingTournament.getStatus());
    }

    @Test
    void cancelTournament_alreadyCompleted_throwsException() {
        upcomingTournament.setStatus(TournamentStatus.COMPLETED);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(upcomingTournament));

        assertThrows(BusinessException.class, () -> tournamentService.cancelTournament(1L));
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }
}
