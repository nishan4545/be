package com.slotbooking.performance;

import com.slotbooking.BaseIntegrationTest;
import com.slotbooking.modules.booking.service.BookingService;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import com.slotbooking.modules.user.enums.UserStatus;
import com.slotbooking.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerformanceLoadTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private BookingService bookingService;

    @Test
    void testBookingPerformanceUnderConcurrency() throws Exception {
        // 1. Create a tournament with large capacity
        Tournament tourney = Tournament.builder()
                .title("Mega Open")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(2))
                .tournamentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(20, 0))
                .totalSlots(1000)
                .availableSlots(1000)
                .entryFee(new BigDecimal("10.00"))
                .status(TournamentStatus.REGISTRATION_OPEN)
                .build();
        tourney = tournamentRepository.save(tourney);
        final Long tourneyId = tourney.getId();

        // 2. Pre-generate 100 players
        List<User> players = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User player = User.builder()
                    .mobileNumber("9000010" + String.format("%03d", i))
                    .password("pass")
                    .fullName("Bench Player " + i)
                    .role(Role.PLAYER)
                    .status(UserStatus.APPROVED)
                    .mobileVerified(true)
                    .build();
            players.add(userRepository.save(player));
        }

        // 3. Parallel Executor
        ExecutorService executor = Executors.newFixedThreadPool(20);
        long startTime = System.currentTimeMillis();

        List<Callable<Long>> tasks = new ArrayList<>();
        for (User player : players) {
            final Long playerId = player.getId();
            tasks.add(() -> {
                long start = System.nanoTime();
                bookingService.joinTournament(player, new com.slotbooking.modules.booking.dto.CreateBookingRequest(tourneyId));
                return System.nanoTime() - start;
            });
        }

        List<Future<Long>> results = executor.invokeAll(tasks);
        long totalDuration = System.currentTimeMillis() - startTime;

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 4. Calculate stats
        long totalExecutionNanos = 0;
        for (Future<Long> res : results) {
            totalExecutionNanos += res.get();
        }
        long averageMs = (totalExecutionNanos / results.size()) / 1_000_000;
        
        System.out.println("====== PERFORMANCE LOAD TEST RESULTS ======");
        System.out.println("Total time for 100 bookings: " + totalDuration + " ms");
        System.out.println("Average latency per booking: " + averageMs + " ms");
        System.out.println("===========================================");

        assertTrue(averageMs < 200, "Average booking transaction time should be under 200ms");
    }
}
