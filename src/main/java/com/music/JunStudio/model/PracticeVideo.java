package com.music.JunStudio.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "practice_videos")
public class PracticeVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    // Will be "YOUTUBE" or "LOCAL_FILE"
    private String videoType;

    // The YouTube Embed URL or the local file path
    private String videoUrl;

    private String uploaderEmail;
    private String uploaderName;
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // NEW: Privacy flag
    private boolean isPrivate = false;

}