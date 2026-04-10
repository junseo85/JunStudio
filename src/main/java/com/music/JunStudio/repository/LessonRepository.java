package com.music.JunStudio.repository;


import com.music.JunStudio.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    //For the Admin: Find all lessons waiting for approval
    List<Lesson> findByStatus(String status);

    //For the Student: Find all their specific approved lessons
    List<Lesson> findByStudentEmailAndStatus(String email, String status);

    // Find all lessons for a specific student
    List<Lesson> findByStudentEmail(String email);
}