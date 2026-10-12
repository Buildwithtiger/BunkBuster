package com.bunkbuster.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data @NoArgsConstructor @AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private Status status;           // PRESENT, ABSENT, LATE, MANUAL

    private Double latitude;
    private Double longitude;
    private Double distanceFromClass;

    @Column(name = "marked_at")
    private LocalDateTime markedAt = LocalDateTime.now();

    public enum Status {
        PRESENT, ABSENT, LATE, MANUAL_OVERRIDE
    }
}