package com.slotbooking.modules.notification.scheduler;

import com.slotbooking.modules.booking.entity.Booking;
import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.booking.repository.BookingRepository;
import com.slotbooking.modules.notification.entity.Notification;
import com.slotbooking.modules.notification.enums.NotificationStatus;
import com.slotbooking.modules.notification.repository.NotificationDeliveryLogRepository;
import com.slotbooking.modules.notification.repository.NotificationRepository;
import com.slotbooking.modules.notification.service.NotificationService;
import com.slotbooking.modules.notification.service.RetryService;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled background tasks manager for the Notification Module.
 * Manages log prunings, automatic timeouts, re-delivery retries, and hourly reminders.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final RetryService retryService;
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryLogRepository logRepository;
    private final TournamentRepository tournamentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    /**
     * Executes every minute.
     * Retries failed notifications.
     */
    @Scheduled(cron = "0 * * * * *")
    public void runRetryJob() {
        log.info("[Scheduler] Starting failed notification delivery retry job...");
        retryService.retryFailedNotifications();
    }

    /**
     * Executes every 5 minutes.
     * Cleans up expired notifications (stuck in PENDING for more than 1 hour).
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void runCleanupJob() {
        log.info("[Scheduler] Starting notification expiration cleanup job...");
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);
        List<Notification> expired = notificationRepository.findByStatusAndCreatedAtBefore(NotificationStatus.PENDING, threshold);
        
        for (Notification n : expired) {
            n.setStatus(NotificationStatus.EXPIRED);
            notificationRepository.save(n);
        }
        log.info("[Scheduler] Expired {} pending alerts.", expired.size());
    }

    /**
     * Executes every hour.
     * Dispatches tournament reminders for matches scheduled tomorrow.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void runTournamentReminderJob() {
        log.info("[Scheduler] Starting upcoming tournament reminder check...");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Tournament> upcoming = tournamentRepository.findByTournamentDate(tomorrow);
        
        for (Tournament tournament : upcoming) {
            log.info("[Scheduler] Dispatched reminders for tournament ID: {}", tournament.getId());
            List<Booking> bookings = bookingRepository.findByTournamentId(tournament.getId());
            
            for (Booking booking : bookings) {
                if (booking.getStatus() == BookingStatus.CONFIRMED) {
                    String timeStr = tournament.getStartTime().toString();
                    String msg = String.format("Reminder: Your slot in tournament '%s' is scheduled tomorrow at %s. Don't be late!", 
                            tournament.getTitle(), timeStr);
                    
                    // Dispatch notification
                    notificationService.createNotification(
                            booking.getUser(),
                            "Upcoming Match Reminder",
                            msg,
                            com.slotbooking.modules.notification.enums.NotificationType.TOURNAMENT_REMINDER,
                            com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                            com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                            "TOURNAMENT",
                            tournament.getId()
                    );
                }
            }
        }
    }

    /**
     * Executes daily at midnight.
     * Deletes delivery logs older than 30 days.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void runDailyLogPruneJob() {
        log.info("[Scheduler] Starting log pruning job...");
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        logRepository.deleteLogsOlderThan(threshold);
        log.info("[Scheduler] Pruning completed successfully.");
    }
}
