package com.music.JunStudio.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "video_feedback")
public class VideoFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long videoId; // Links back to PracticeVideo

    @Column(columnDefinition = "TEXT")
    private String commentText;

    private String commenterEmail;
    private String commenterName;
    private LocalDateTime postedAt = LocalDateTime.now();
}