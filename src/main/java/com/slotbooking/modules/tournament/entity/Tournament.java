package com.slotbooking.modules.tournament.entity;

import com.slotbooking.modules.tournament.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String bannerImage;

    @Column(nullable = false)
    private LocalDateTime registrationStartDate;

    @Column(nullable = false)
    private LocalDateTime registrationEndDate;

    @Column(nullable = false)
    private LocalDate tournamentDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer totalSlots;

    @Column(nullable = false)
    private Integer availableSlots;

    @Column(nullable = false)
    private BigDecimal entryFee;

    @Column(nullable = false)
    private BigDecimal prizeAmount;

    private Integer winnerCount;

    @Enumerated(EnumType.STRING)
    private TournamentStatus status;

    @Column(nullable = false)
    private Long createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
