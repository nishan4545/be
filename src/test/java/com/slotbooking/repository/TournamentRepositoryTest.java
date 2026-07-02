package com.slotbooking.repository;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class TournamentRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    private Tournament tournament;

    @BeforeEach
    void setup() {
        tournament = Tournament.builder()
                .title("Masters Cup")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(1))
                .tournamentDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .totalSlots(16)
                .availableSlots(16)
                .entryFee(new BigDecimal("100.00"))
                .prizeAmount(new BigDecimal("1000.00"))
                .winnerCount(1)
                .status(TournamentStatus.REGISTRATION_OPEN)
                .build();
        tournament = tournamentRepository.save(tournament);
    }

    @Test
    void testFindByIdWithLock() {
        Optional<Tournament> locked = tournamentRepository.findByIdWithLock(tournament.getId());
        assertTrue(locked.isPresent());
        assertEquals("Masters Cup", locked.get().getTitle());
    }

    @Test
    void testFindByTournamentDate() {
        var list = tournamentRepository.findByTournamentDate(LocalDate.now().plusDays(2));
        assertFalse(list.isEmpty());
        assertEquals("Masters Cup", list.getFirst().getTitle());
    }
}
