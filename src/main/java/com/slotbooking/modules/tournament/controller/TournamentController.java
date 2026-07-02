package com.slotbooking.modules.tournament.controller;

import com.slotbooking.common.response.ApiResponse;
import com.slotbooking.modules.tournament.dto.CreateTournamentRequest;
import com.slotbooking.modules.tournament.dto.TournamentResponse;
import com.slotbooking.modules.tournament.dto.UpdateTournamentRequest;
import com.slotbooking.modules.tournament.service.TournamentService;
import com.slotbooking.modules.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for admin tournament management.
 * All endpoints are restricted to ROLE_ADMIN via SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    /**
     * Creates a new tournament. The authenticated admin's ID is stored
     * as the tournament creator.
     *
     * @param request the creation request body
     * @param admin   the authenticated admin user
     * @return the created tournament
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TournamentResponse>> createTournament(
            @RequestBody @Valid CreateTournamentRequest request,
            @AuthenticationPrincipal User admin) {
        TournamentResponse response = tournamentService.createTournament(request, admin.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tournament created successfully", response));
    }

    /**
     * Updates an existing tournament by ID.
     *
     * @param id      the tournament ID
     * @param request the update request body
     * @return the updated tournament
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> updateTournament(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTournamentRequest request) {
        TournamentResponse response = tournamentService.updateTournament(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tournament updated successfully", response));
    }

    /**
     * Deletes a tournament by ID.
     * Only UPCOMING or CANCELLED tournaments can be deleted.
     *
     * @param id the tournament ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTournament(@PathVariable Long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament deleted successfully", null));
    }

    /**
     * Retrieves all tournaments.
     *
     * @return list of all tournaments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TournamentResponse>>> getAllTournaments() {
        List<TournamentResponse> tournaments = tournamentService.getAllTournaments();
        return ResponseEntity.ok(ApiResponse.success("Tournaments retrieved successfully", tournaments));
    }

    /**
     * Retrieves a single tournament by ID.
     *
     * @param id the tournament ID
     * @return the tournament details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TournamentResponse>> getTournament(@PathVariable Long id) {
        TournamentResponse response = tournamentService.getTournament(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament retrieved successfully", response));
    }

    /**
     * Opens registration for a tournament.
     * Transitions the tournament from UPCOMING to REGISTRATION_OPEN.
     *
     * @param id the tournament ID
     * @return the updated tournament
     */
    @PatchMapping("/{id}/open")
    public ResponseEntity<ApiResponse<TournamentResponse>> openRegistration(@PathVariable Long id) {
        TournamentResponse response = tournamentService.openRegistration(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament registration opened", response));
    }

    /**
     * Closes registration for a tournament.
     * Transitions the tournament from REGISTRATION_OPEN to REGISTRATION_CLOSED.
     *
     * @param id the tournament ID
     * @return the updated tournament
     */
    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<TournamentResponse>> closeRegistration(@PathVariable Long id) {
        TournamentResponse response = tournamentService.closeRegistration(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament registration closed", response));
    }

    /**
     * Cancels a tournament. Can be performed from any non-terminal state.
     *
     * @param id the tournament ID
     * @return the updated tournament
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TournamentResponse>> cancelTournament(@PathVariable Long id) {
        TournamentResponse response = tournamentService.cancelTournament(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament cancelled", response));
    }

    /**
     * Marks a tournament as completed.
     * Transitions from ONGOING or REGISTRATION_CLOSED to COMPLETED.
     *
     * @param id the tournament ID
     * @return the updated tournament
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TournamentResponse>> completeTournament(@PathVariable Long id) {
        TournamentResponse response = tournamentService.completeTournament(id);
        return ResponseEntity.ok(ApiResponse.success("Tournament completed", response));
    }
}
