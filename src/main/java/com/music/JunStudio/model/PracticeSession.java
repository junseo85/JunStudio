package com.music.JunStudio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "practice_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links the practice session back to the specific student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "s3_audio_url", nullable = false, length = 500)
    private String s3AudioUrl;

    @Column(name = "admin_feedback", columnDefinition = "TEXT")
    private String adminFeedback;

    @Column(nullable = false)
    private String status = "NEEDS_REVIEW";

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}