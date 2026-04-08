package com.music.JunStudio.repository;

import com.music.JunStudio.model.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeSessionRepository extends JpaRepository<PracticeSession, Long> {
    // Allows us to load the student's dashboard with all their past recordings
    List<PracticeSession> findByStudentId(Long studentId);

    // Allows the admin to see everything that needs feedback
    List<PracticeSession> findByStatus(String status);
}