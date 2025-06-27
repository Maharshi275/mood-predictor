package com.IndiaNIC.mood_predictor.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="check_ins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "questions", nullable = false, columnDefinition = "TEXT")
    private String questions;

    @Column(name = "answers", nullable = false, columnDefinition = "TEXT")
    private String answers;

    @Column(name = "predicted_mood", nullable = false)
    private String predictedMood;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.checkInDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }
}
