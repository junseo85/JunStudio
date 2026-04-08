package com.music.JunStudio.controller;

import com.music.JunStudio.model.LessonRequest;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.LessonRequestRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*")
public class LessonRequestController {

    @Autowired
    private LessonRequestRepository lessonRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<String> createLessonRequest(@RequestParam Long studentId, @RequestParam String requestedTime) {
        User student = userRepository.findById(studentId).orElseThrow();

        if (student.getLessonCredits() <= 0) {
            return ResponseEntity.badRequest().body("Not enough lesson credits. Please purchase more.");
        }

        // Deduct a credit
        student.setLessonCredits(student.getLessonCredits() - 1);
        userRepository.save(student);

        // Create the request
        LessonRequest request = new LessonRequest();
        request.setStudent(student);
        request.setRequestedTime(requestedTime);
        request.setStatus("PENDING");
        lessonRepository.save(request);

        return ResponseEntity.ok("Lesson requested successfully!");
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<LessonRequest>> getStudentRequests(@PathVariable Long studentId) {
        return ResponseEntity.ok(lessonRepository.findByStudentId(studentId));
    }
}