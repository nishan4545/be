package com.slotbooking.modules.tournament.scheduler;

import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.websocket.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentScheduler {

    private final TournamentRepository tournamentRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * Periodically check and auto-close registrations for tournaments
     * whose registration end date is in the past.
     * Runs every 30 seconds.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void autoCloseExpiredRegistrations() {
        LocalDateTime now = LocalDateTime.now();
        List<Tournament> expiredTournaments = tournamentRepository.findByStatusAndRegistrationEndDateBefore(
                TournamentStatus.REGISTRATION_OPEN, 
                now
        );

        for (Tournament tournament : expiredTournaments) {
            log.info("[Tournament Autoclose] Closing registration for tournament ID: {}, Title: '{}', Reg End Date: {}", 
                    tournament.getId(), tournament.getTitle(), tournament.getRegistrationEndDate());
            
            tournament.setStatus(TournamentStatus.REGISTRATION_CLOSED);
            Tournament saved = tournamentRepository.save(tournament);
            
            // Broadcast the tournament update via WebSockets to update player/admin UIs
            webSocketNotificationService.notifyTournamentUpdated(saved);
        }
    }
}
