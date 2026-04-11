package com.music.JunStudio.repository;

import com.music.JunStudio.model.PracticeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeVideoRepository extends JpaRepository<PracticeVideo, Long> {
    // This helps power the search bar!
    List<PracticeVideo> findByTitleContainingIgnoreCaseOrUploaderNameContainingIgnoreCase(String title, String name);
    List<PracticeVideo> findAllByOrderByUploadedAtDesc();
}