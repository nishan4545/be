package com.slotbooking.integration;

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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrencyBookingTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private BookingService bookingService;

    @Test
    void testConcurrentBookingForLastRemainingSlot() throws InterruptedException {
        // 1. Create tournament with only 1 slot available
        Tournament tourney = Tournament.builder()
                .title("Concurrent Race Tournament")
                .registrationStartDate(LocalDateTime.now().minusDays(1))
                .registrationEndDate(LocalDateTime.now().plusDays(2))
                .tournamentDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(17, 0))
                .totalSlots(10)
                .availableSlots(1) // Only 1 slot left!
                .entryFee(new BigDecimal("100.00"))
                .status(TournamentStatus.REGISTRATION_OPEN)
                .build();
        tourney = tournamentRepository.save(tourney);
        final Long tourneyId = tourney.getId();

        // 2. Create 50 player users
        List<User> players = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            User player = User.builder()
                    .mobileNumber("9000000" + String.format("%03d", i))
                    .password("pass")
                    .fullName("Player " + i)
                    .role(Role.PLAYER)
                    .status(UserStatus.APPROVED)
                    .mobileVerified(true)
                    .build();
            players.add(userRepository.save(player));
        }

        // 3. Launch concurrent booking tasks
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failureCounter = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (User player : players) {
            final Long playerId = player.getId();
            tasks.add(() -> {
                latch.await();
                try {
                    bookingService.joinTournament(player, new com.slotbooking.modules.booking.dto.CreateBookingRequest(tourneyId));
                    successCounter.incrementAndGet();
                } catch (Exception e) {
                    failureCounter.incrementAndGet();
                }
                return null;
            });
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }

        latch.countDown();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 4. Verify results
        assertEquals(1, successCounter.get(), "Exactly one player must successfully book the last slot");
        assertEquals(49, failureCounter.get(), "Forty-nine players must fail booking due to lock constraints");

        Tournament updatedTourney = tournamentRepository.findById(tourneyId).orElseThrow();
        assertEquals(0, updatedTourney.getAvailableSlots());
    }
}
