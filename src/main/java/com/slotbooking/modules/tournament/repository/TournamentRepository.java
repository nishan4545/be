package com.slotbooking.modules.tournament.repository;

import com.slotbooking.modules.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tournament t WHERE t.id = :id")
    Optional<Tournament> findByIdWithLock(@Param("id") Long id);

    java.util.List<Tournament> findByTournamentDate(java.time.LocalDate date);

    java.util.List<Tournament> findByStatusAndRegistrationEndDateBefore(
            com.slotbooking.modules.tournament.enums.TournamentStatus status,
            java.time.LocalDateTime dateTime
    );
}
