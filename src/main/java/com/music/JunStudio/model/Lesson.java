package com.music.JunStudio.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We will save the email of the person who requested it
    @Column(nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private LocalDate lessonDate;

    @Column(nullable = false)
    private LocalTime lessonTime;

    // Automatically set new requests to "PENDING"
    @Column(nullable = false)
    private String status = "PENDING";
}