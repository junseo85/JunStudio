package com.music.JunStudio.repository;
import com.music.JunStudio.model.LessonRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonRequestRepository extends JpaRepository<LessonRequest, Long> {
    // Allows us to fetch all scheduling requests for a specific student
    List<LessonRequest> findByStudentId(Long studentId);
}