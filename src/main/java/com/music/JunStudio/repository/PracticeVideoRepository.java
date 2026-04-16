package com.music.JunStudio.repository;

import com.music.JunStudio.model.PracticeVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeVideoRepository extends JpaRepository<PracticeVideo, Long> {

    // ==========================================
    // FOR ADMINS (Can see everything)
    // ==========================================
    Page<PracticeVideo> findByTitleContainingIgnoreCaseOrUploaderNameContainingIgnoreCase(String title, String name, Pageable pageable);

    // ==========================================
    // FOR TEACHERS (See public, their own private, and their students' private videos)
    // ==========================================
    @Query("SELECT v FROM PracticeVideo v WHERE v.isPrivate = false " +
            "OR v.uploaderEmail = :teacherEmail " +
            "OR v.uploaderEmail IN (SELECT u.email FROM User u WHERE u.assignedTeacher.id = :teacherId)")
    Page<PracticeVideo> findAllForTeacher(
            @Param("teacherEmail") String teacherEmail,
            @Param("teacherId") Long teacherId,
            Pageable pageable);

    @Query("SELECT v FROM PracticeVideo v WHERE (v.isPrivate = false " +
            "OR v.uploaderEmail = :teacherEmail " +
            "OR v.uploaderEmail IN (SELECT u.email FROM User u WHERE u.assignedTeacher.id = :teacherId)) " +
            "AND (LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.uploaderName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PracticeVideo> searchForTeacher(
            @Param("search") String search,
            @Param("teacherEmail") String teacherEmail,
            @Param("teacherId") Long teacherId,
            Pageable pageable);

    // ==========================================
    // FOR STUDENTS (Only see public videos + their own private videos)
    // ==========================================
    @Query("SELECT v FROM PracticeVideo v WHERE v.isPrivate = false OR v.uploaderEmail = :email")
    Page<PracticeVideo> findAllForStudent(@Param("email") String email, Pageable pageable);

    @Query("SELECT v FROM PracticeVideo v WHERE (v.isPrivate = false OR v.uploaderEmail = :email) AND (LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.uploaderName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PracticeVideo> searchForStudent(@Param("search") String search, @Param("email") String email, Pageable pageable);
}