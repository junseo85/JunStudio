package com.music.JunStudio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ActivityDTO {
    private LocalDateTime timestamp;
    private String userEmail;
    private String actionType;  // e.g., "LESSON", "VIDEO"
    private String description;
    private String statusLabel; // e.g., "SCHEDULED", "UPLOADED"
}