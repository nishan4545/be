package com.slotbooking.repository;

import com.slotbooking.BaseIntegrationTest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class BookingRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    private User player;
    private Tournament tournament;
    private Booking booking;

    @BeforeEach
    void setup() {
        player = User.builder()
                .mobileNumber("9876543233")
                .password("pass")
                .fullName("Alex")
                .role(Role.PLAYER)
                .status(UserStatus.APPROVED)
                .mobileVerified(true)
                .build();
        player = userRepository.save(player);

        tournament = Tournament.builder()
                .title("League Cup")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(1))
                .tournamentDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(16, 0))
                .totalSlots(8)
                .availableSlots(8)
                .entryFee(new BigDecimal("50.00"))
                .status(TournamentStatus.REGISTRATION_OPEN)
                .build();
        tournament = tournamentRepository.save(tournament);

        booking = Booking.builder()
                .user(player)
                .tournament(tournament)
                .status(BookingStatus.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);
    }

    @Test
    void testFindByUserIdAndTournamentId() {
        List<Booking> result = bookingRepository.findByUserIdAndTournamentId(player.getId(), tournament.getId());
        assertEquals(1, result.size());
        assertEquals(BookingStatus.CONFIRMED, result.get(0).getStatus());
    }

    @Test
    void testFindByTournamentId() {
        List<Booking> list = bookingRepository.findByTournamentId(tournament.getId());
        assertEquals(1, list.size());
    }
}
