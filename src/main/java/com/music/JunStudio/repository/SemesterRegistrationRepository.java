package com.music.JunStudio.repository;

import com.music.JunStudio.model.SemesterRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemesterRegistrationRepository extends JpaRepository<SemesterRegistration, Long> {

    // We will need these later for the Teacher and Admin dashboards!
    List<SemesterRegistration> findByTeacherIdAndStatus(Long teacherId, String status);
    List<SemesterRegistration> findByStudentId(Long studentId);
    List<SemesterRegistration> findByStatus(String status);
}