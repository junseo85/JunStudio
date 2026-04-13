package com.music.JunStudio.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "semester_registrations")
@Data
public class SemesterRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // e.g., "SPRING", "SUMMER", "FALL"
    @Column(nullable = false)
    private String term;

    // e.g., 2026
    @Column(nullable = false)
    private int year;

    // Student Preferences
    @Column(name = "preferred_day_one", nullable = false)
    private String preferredDayOne;

    @Column(name = "preferred_day_two")
    private String preferredDayTwo;

    @Column(length = 500)
    private String memo;

    // Teacher Assignments (Null until the teacher approves)
    @Column(name = "assigned_day")
    @Enumerated(EnumType.STRING)
    private DayOfWeek assignedDay;

    @Column(name = "assigned_time")
    private LocalTime assignedTime;

    // "PENDING", "ASSIGNED", "REJECTED"
    @Column(nullable = false)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime requestedAt;
}