package com.music.JunStudio.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "schedule_overrides")
public class ScheduleOverride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDate overrideDate;

    private LocalTime startTime;
    private LocalTime endTime;

    // If true, the studio is completely closed this day
    private boolean isClosed;
}