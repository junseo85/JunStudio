package com.music.JunStudio.repository;

import com.music.JunStudio.model.VideoFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VideoFeedbackRepository extends JpaRepository<VideoFeedback, Long> {
    List<VideoFeedback> findByVideoIdOrderByPostedAtAsc(Long videoId);
}