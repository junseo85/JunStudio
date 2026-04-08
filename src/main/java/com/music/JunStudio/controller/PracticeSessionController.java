package com.music.JunStudio.controller;

import com.music.JunStudio.model.PracticeSession;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.PracticeSessionRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practice")
@CrossOrigin(origins = "*")
public class PracticeSessionController {

    @Autowired
    private PracticeSessionRepository practiceRepository;
    @Autowired
    private UserRepository userRepository;

    // Student submits a new recording
    @PostMapping("/submit")
    public ResponseEntity<PracticeSession> submitPractice(
            @RequestParam Long studentId,
            @RequestParam String s3Url) { // The frontend uploads to S3 and sends the URL here

        User student = userRepository.findById(studentId).orElseThrow();

        PracticeSession session = new PracticeSession();
        session.setStudent(student);
        session.setS3AudioUrl(s3Url);
        session.setStatus("NEEDS_REVIEW");

        return ResponseEntity.ok(practiceRepository.save(session));
    }

    // Admin leaves feedback
    @PutMapping("/{sessionId}/feedback")
    public ResponseEntity<PracticeSession> leaveFeedback(
            @PathVariable Long sessionId,
            @RequestBody String feedbackText) {

        PracticeSession session = practiceRepository.findById(sessionId).orElseThrow();
        session.setAdminFeedback(feedbackText);
        session.setStatus("REVIEWED");

        return ResponseEntity.ok(practiceRepository.save(session));
    }

    // Admin fetches all sessions that need review
    @GetMapping("/pending")
    public ResponseEntity<List<PracticeSession>> getPendingReviews() {
        return ResponseEntity.ok(practiceRepository.findByStatus("NEEDS_REVIEW"));
    }
}