package com.slotbooking.modules.booking.entity;

import com.slotbooking.modules.booking.enums.BookingStatus;
import com.slotbooking.modules.tournament.entity.Tournament;
import com.slotbooking.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(unique = true)
    private String razorpayOrderId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private Integer seatNumber;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
