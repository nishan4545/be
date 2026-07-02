package com.slotbooking.modules.tournament.service;

import com.slotbooking.exception.BusinessException;
import com.slotbooking.exception.ResourceNotFoundException;
import com.slotbooking.exception.ValidationException;
import com.slotbooking.modules.tournament.dto.CreateTournamentRequest;
import com.slotbooking.modules.tournament.dto.TournamentResponse;
import com.slotbooking.modules.tournament.dto.UpdateTournamentRequest;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.tournament.enums.TournamentStatus;
import com.slotbooking.modules.tournament.repository.TournamentRepository;
import com.slotbooking.modules.user.repository.UserRepository;
import com.slotbooking.modules.user.entity.User;
import com.slotbooking.modules.user.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for tournament management.
 * Handles all CRUD operations and lifecycle transitions
 * with comprehensive business rule validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final com.slotbooking.modules.websocket.service.WebSocketNotificationService webSocketNotificationService;
    private final com.slotbooking.modules.notification.service.NotificationService notificationService;
    private final com.slotbooking.modules.booking.repository.BookingRepository bookingRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new tournament after validating all date and slot constraints.
     *
     * @param request  the creation request DTO
     * @param adminId  the ID of the admin creating the tournament
     * @return the created tournament response
     */
    @Transactional
    public TournamentResponse createTournament(CreateTournamentRequest request, Long adminId) {
        validateTournamentDates(
                request.getRegistrationStartDate(),
                request.getRegistrationEndDate(),
                request.getTournamentDate()
        );

        Tournament tournament = Tournament.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .bannerImage(request.getBannerImage())
                .registrationStartDate(request.getRegistrationStartDate())
                .registrationEndDate(request.getRegistrationEndDate())
                .tournamentDate(request.getTournamentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalSlots(request.getTotalSlots())
                .availableSlots(request.getTotalSlots())
                .entryFee(request.getEntryFee())
                .prizeAmount(request.getPrizeAmount())
                .winnerCount(request.getWinnerCount())
                .status(TournamentStatus.UPCOMING)
                .createdBy(adminId)
                .build();

        Tournament saved = tournamentRepository.save(tournament);
        webSocketNotificationService.notifyTournamentCreated(saved);
        webSocketNotificationService.notifyAdminDashboard("TOURNAMENT_CREATED", "New tournament created: " + saved.getTitle(), saved.getId());

        // Notify all players of new tournament
        try {
            List<User> players = userRepository.findByRole(Role.PLAYER);
            for (User player : players) {
                notificationService.createNotification(
                        player,
                        "New Tournament Scheduled",
                        "A new tournament '" + saved.getTitle() + "' has been scheduled for " + saved.getTournamentDate() + ". Register now!",
                        com.slotbooking.modules.notification.enums.NotificationType.TOURNAMENT_CREATED,
                        com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                        com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                        "TOURNAMENT",
                        saved.getId()
                );
            }
        } catch (Exception e) {
            log.error("Failed to broadcast tournament creation notification: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    /**
     * Updates an existing tournament with the provided fields.
     * Only non-null fields in the request will overwrite existing values.
     *
     * @param id      the tournament ID
     * @param request the update request DTO
     * @return the updated tournament response
     */
    @Transactional
    public TournamentResponse updateTournament(Long id, UpdateTournamentRequest request) {
        Tournament tournament = findTournamentOrThrow(id);

        if (tournament.getStatus() == TournamentStatus.COMPLETED
                || tournament.getStatus() == TournamentStatus.CANCELLED) {
            throw new BusinessException("Cannot update a tournament that is completed or cancelled");
        }

        if (request.getTitle() != null) {
            tournament.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            tournament.setDescription(request.getDescription());
        }
        if (request.getBannerImage() != null) {
            tournament.setBannerImage(request.getBannerImage());
        }
        if (request.getRegistrationStartDate() != null) {
            tournament.setRegistrationStartDate(request.getRegistrationStartDate());
        }
        if (request.getRegistrationEndDate() != null) {
            tournament.setRegistrationEndDate(request.getRegistrationEndDate());
        }
        if (request.getTournamentDate() != null) {
            tournament.setTournamentDate(request.getTournamentDate());
        }
        if (request.getStartTime() != null) {
            tournament.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            tournament.setEndTime(request.getEndTime());
        }
        if (request.getTotalSlots() != null) {
            int bookedSlots = tournament.getTotalSlots() - tournament.getAvailableSlots();
            if (request.getTotalSlots() < bookedSlots) {
                throw new ValidationException("Total slots cannot be less than already booked slots (" + bookedSlots + ")");
            }
            tournament.setAvailableSlots(request.getTotalSlots() - bookedSlots);
            tournament.setTotalSlots(request.getTotalSlots());
        }
        if (request.getEntryFee() != null) {
            tournament.setEntryFee(request.getEntryFee());
        }
        if (request.getPrizeAmount() != null) {
            tournament.setPrizeAmount(request.getPrizeAmount());
        }
        if (request.getWinnerCount() != null) {
            tournament.setWinnerCount(request.getWinnerCount());
        }

        validateTournamentDates(
                tournament.getRegistrationStartDate(),
                tournament.getRegistrationEndDate(),
                tournament.getTournamentDate()
        );

        Tournament saved = tournamentRepository.save(tournament);
        webSocketNotificationService.notifyTournamentUpdated(saved);
        return mapToResponse(saved);
    }

    /**
     * Deletes a tournament. Only UPCOMING or CANCELLED tournaments can be deleted.
     *
     * @param id the tournament ID
     */
    @Transactional
    public void deleteTournament(Long id) {
        Tournament tournament = findTournamentOrThrow(id);

        if (tournament.getStatus() != TournamentStatus.UPCOMING
                && tournament.getStatus() != TournamentStatus.CANCELLED) {
            throw new BusinessException("Only UPCOMING or CANCELLED tournaments can be deleted");
        }

        tournamentRepository.delete(tournament);
        webSocketNotificationService.notifyTournamentCancelled(tournament);
    }

    /**
     * Retrieves a single tournament by ID.
     *
     * @param id the tournament ID
     * @return the tournament response
     */
    @Transactional(readOnly = true)
    public TournamentResponse getTournament(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        return mapToResponse(tournament);
    }

    /**
     * Retrieves all tournaments ordered by creation date (newest first).
     *
     * @return list of tournament responses
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getAllTournaments() {
        return tournamentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Opens registration for a tournament.
     * Only UPCOMING tournaments can transition to REGISTRATION_OPEN.
     *
     * @param id the tournament ID
     * @return the updated tournament response
     */
    @Transactional
    public TournamentResponse openRegistration(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        validateStatusTransition(tournament.getStatus(), TournamentStatus.REGISTRATION_OPEN);
        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        Tournament saved = tournamentRepository.save(tournament);
        webSocketNotificationService.notifyTournamentUpdated(saved);
        return mapToResponse(saved);
    }

    /**
     * Closes registration for a tournament.
     * Only REGISTRATION_OPEN tournaments can transition to REGISTRATION_CLOSED.
     *
     * @param id the tournament ID
     * @return the updated tournament response
     */
    @Transactional
    public TournamentResponse closeRegistration(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        validateStatusTransition(tournament.getStatus(), TournamentStatus.REGISTRATION_CLOSED);
        tournament.setStatus(TournamentStatus.REGISTRATION_CLOSED);
        Tournament saved = tournamentRepository.save(tournament);
        webSocketNotificationService.notifyTournamentUpdated(saved);
        return mapToResponse(saved);
    }

    /**
     * Cancels a tournament. Can be done from any non-terminal state.
     *
     * @param id the tournament ID
     * @return the updated tournament response
     */
    @Transactional
    public TournamentResponse cancelTournament(Long id) {
        Tournament tournament = findTournamentOrThrow(id);

        if (tournament.getStatus() == TournamentStatus.COMPLETED
                || tournament.getStatus() == TournamentStatus.CANCELLED) {
            throw new BusinessException("Tournament is already " + tournament.getStatus().name().toLowerCase());
        }

        tournament.setStatus(TournamentStatus.CANCELLED);
        Tournament saved = tournamentRepository.save(tournament);
        webSocketNotificationService.notifyTournamentCancelled(saved);

        // Notify affected players
        try {
            List<com.slotbooking.modules.booking.entity.Booking> bookings = bookingRepository.findByTournamentId(saved.getId());
            for (com.slotbooking.modules.booking.entity.Booking booking : bookings) {
                if (booking.getStatus() == com.slotbooking.modules.booking.enums.BookingStatus.CONFIRMED ||
                        booking.getStatus() == com.slotbooking.modules.booking.enums.BookingStatus.PENDING_PAYMENT) {
                    
                    notificationService.createNotification(
                            booking.getUser(),
                            "Tournament Cancelled",
                            "Important: The tournament '" + saved.getTitle() + "' has been cancelled. Your booking has been cancelled and any paid entry fees will be refunded.",
                            com.slotbooking.modules.notification.enums.NotificationType.TOURNAMENT_CANCELLED,
                            com.slotbooking.modules.notification.enums.DeliveryChannel.IN_APP.name(),
                            com.slotbooking.modules.notification.enums.NotificationPriority.HIGH,
                            "TOURNAMENT",
                            saved.getId()
                    );

                    if (booking.getUser().getEmail() != null && !booking.getUser().getEmail().isBlank()) {
                        notificationService.createNotification(
                                booking.getUser(),
                                "Tournament Cancelled Alert - " + saved.getTitle(),
                                "Hello, this is to inform you that the tournament '" + saved.getTitle() + "' scheduled on " + saved.getTournamentDate() + " has been cancelled. Your booking has been cancelled and entry fees will be refunded.",
                                com.slotbooking.modules.notification.enums.NotificationType.TOURNAMENT_CANCELLED,
                                com.slotbooking.modules.notification.enums.DeliveryChannel.EMAIL.name(),
                                com.slotbooking.modules.notification.enums.NotificationPriority.NORMAL,
                                "TOURNAMENT",
                                saved.getId()
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast tournament cancellation notification: {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    /**
     * Marks a tournament as completed.
     * Only ONGOING or REGISTRATION_CLOSED tournaments can be completed.
     *
     * @param id the tournament ID
     * @return the updated tournament response
     */
    @Transactional
    public TournamentResponse completeTournament(Long id) {
        Tournament tournament = findTournamentOrThrow(id);
        validateStatusTransition(tournament.getStatus(), TournamentStatus.COMPLETED);
        tournament.setStatus(TournamentStatus.COMPLETED);
        Tournament saved = tournamentRepository.save(tournament);
        webSocketNotificationService.notifyTournamentUpdated(saved);
        return mapToResponse(saved);
    }

    // ──────────────────────────── Private helpers ────────────────────────────

    /**
     * Finds a tournament by ID or throws ResourceNotFoundException.
     */
    private Tournament findTournamentOrThrow(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + id));
    }

    /**
     * Validates business rules for tournament date constraints:
     * <ul>
     *   <li>Registration start must be before registration end</li>
     *   <li>Tournament date must be on or after registration end date</li>
     * </ul>
     */
    private void validateTournamentDates(
            java.time.LocalDateTime registrationStart,
            java.time.LocalDateTime registrationEnd,
            java.time.LocalDate tournamentDate) {

        if (registrationStart.isAfter(registrationEnd) || registrationStart.isEqual(registrationEnd)) {
            throw new ValidationException("Registration start date must be before registration end date");
        }

        if (tournamentDate.isBefore(registrationEnd.toLocalDate())) {
            throw new ValidationException("Tournament date must be on or after registration end date");
        }
    }

    /**
     * Validates that a status transition is allowed according to the tournament lifecycle.
     */
    private void validateStatusTransition(TournamentStatus current, TournamentStatus target) {
        boolean valid = switch (target) {
            case REGISTRATION_OPEN -> current == TournamentStatus.UPCOMING;
            case REGISTRATION_CLOSED -> current == TournamentStatus.REGISTRATION_OPEN;
            case ONGOING -> current == TournamentStatus.REGISTRATION_CLOSED;
            case COMPLETED -> current == TournamentStatus.ONGOING
                    || current == TournamentStatus.REGISTRATION_CLOSED;
            case CANCELLED -> current != TournamentStatus.COMPLETED
                    && current != TournamentStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new BusinessException(
                    "Cannot transition from " + current.name() + " to " + target.name());
        }
    }

    /**
     * Maps a Tournament entity to a TournamentResponse DTO.
     */
    private TournamentResponse mapToResponse(Tournament tournament) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .title(tournament.getTitle())
                .description(tournament.getDescription())
                .bannerImage(tournament.getBannerImage())
                .registrationStartDate(tournament.getRegistrationStartDate())
                .registrationEndDate(tournament.getRegistrationEndDate())
                .tournamentDate(tournament.getTournamentDate())
                .startTime(tournament.getStartTime())
                .endTime(tournament.getEndTime())
                .totalSlots(tournament.getTotalSlots())
                .availableSlots(tournament.getAvailableSlots())
                .entryFee(tournament.getEntryFee())
                .prizeAmount(tournament.getPrizeAmount())
                .winnerCount(tournament.getWinnerCount())
                .status(tournament.getStatus())
                .createdBy(tournament.getCreatedBy())
                .createdAt(tournament.getCreatedAt())
                .updatedAt(tournament.getUpdatedAt())
                .build();
    }
}
