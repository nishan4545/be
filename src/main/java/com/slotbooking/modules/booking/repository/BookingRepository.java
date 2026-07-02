package com.slotbooking.modules.booking.repository;

import com.slotbooking.modules.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import com.slotbooking.modules.booking.enums.BookingStatus;
import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByUserIdAndTournamentIdAndStatusIn(Long userId, Long tournamentId, Collection<BookingStatus> statuses);

    java.util.List<Booking> findByUserIdAndTournamentId(Long userId, Long tournamentId);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByTournamentId(Long tournamentId);

    long countByStatus(BookingStatus status);

    long countByTournamentIdAndStatus(Long tournamentId, BookingStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.tournament WHERE b.status = :status AND b.createdAt < :threshold")
    List<Booking> findByStatusAndCreatedAtBefore(
            @org.springframework.data.repository.query.Param("status") BookingStatus status,
            @org.springframework.data.repository.query.Param("threshold") java.time.LocalDateTime threshold
    );

    long countByCreatedAtAfter(LocalDateTime startOfDay);
}
