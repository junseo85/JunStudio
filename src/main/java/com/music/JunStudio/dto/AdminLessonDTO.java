package com.music.JunStudio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AdminLessonDTO {
    private Long lessonId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate lessonDate;
    private LocalTime lessonTime;
}
