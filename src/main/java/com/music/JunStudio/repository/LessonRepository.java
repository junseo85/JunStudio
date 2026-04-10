package com.music.JunStudio.repository;


import com.music.JunStudio.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    //For the Admin: Find all lessons waiting for approval
    List<Lesson> findByStatus(String status);

    //For the Student: Find all their specific approved lessons
    List<Lesson> findByStudentEmailAndStatus(String email, String status);

    // Find all lessons for a specific student
    List<Lesson> findByStudentEmail(String email);

    // Checks if a slot is taken by ANY lesson that hasn't been canceled
    boolean existsByLessonDateAndLessonTimeAndStatusNot(LocalDate date, LocalTime time, String status);

    // Add this to your existing LessonRepository
    List<Lesson> findByLessonDateAndStatusNot(LocalDate date, String status);
}