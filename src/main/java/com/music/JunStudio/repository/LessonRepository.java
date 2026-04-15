package com.music.JunStudio.repository;


import com.music.JunStudio.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT l FROM Lesson l " +
            "LEFT JOIN l.semesterRegistration sr " +
            "WHERE ((sr.teacher.id = :teacherId) OR (l.studentEmail IN (SELECT u.email FROM User u WHERE u.assignedTeacher.id = :teacherId))) " +
            "AND l.status = :status")
    List<Lesson> findByTeacherIdAndStatus(@Param("teacherId") Long teacherId, @Param("status") String status);
}