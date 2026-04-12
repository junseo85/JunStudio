package com.music.JunStudio.controller;

import com.music.JunStudio.model.ActivityDTO;
import com.music.JunStudio.model.Lesson;
import com.music.JunStudio.model.PracticeVideo;
import com.music.JunStudio.repository.LessonRepository;
import com.music.JunStudio.repository.PracticeVideoRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private PracticeVideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String viewReports(Model model, Principal principal) {
        // Security Check
        if (!"ROLE_ADMIN".equals(userRepository.findByEmail(principal.getName()).get().getRole())) {
            return "redirect:/dashboard";
        }

        //THE FIX: Tell Thymeleaf this is an Admin
        model.addAttribute("isAdmin", true);

        List<Lesson> allLessons = lessonRepository.findAll();
        List<PracticeVideo> allVideos = videoRepository.findAll();

        // 1. DATA FOR THE DONUT CHART
        long scheduledCount = allLessons.stream().filter(l -> "SCHEDULED".equals(l.getStatus())).count();
        long pendingCount = allLessons.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long canceledCount = allLessons.stream().filter(l -> "CANCELED".equals(l.getStatus())).count();
        long videoCount = allVideos.size();

        model.addAttribute("scheduledCount", scheduledCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("canceledCount", canceledCount);
        model.addAttribute("videoCount", videoCount);

        // ==========================================
        // NEW: Create a lookup map to translate Emails to Full Names
        // ==========================================
        Map<String, String> nameLookup = userRepository.findAll().stream()
                .collect(Collectors.toMap(
                        user -> user.getEmail(),
                        user -> user.getFirstName() + " " + user.getLastName()
                ));

        // ==========================================
        // UPDATED: DRILL-DOWN DATA FOR THE CHART
        // ==========================================

        // We use nameLookup.getOrDefault() so if a user was deleted,
        // it safely falls back to showing their email address instead of crashing.

        Map<String, Long> scheduledByUser = allLessons.stream()
                .filter(l -> "SCHEDULED".equals(l.getStatus()))
                .collect(Collectors.groupingBy(
                        l -> nameLookup.getOrDefault(l.getStudentEmail(), l.getStudentEmail()),
                        Collectors.counting()
                ));

        Map<String, Long> pendingByUser = allLessons.stream()
                .filter(l -> "PENDING".equals(l.getStatus()))
                .collect(Collectors.groupingBy(
                        l -> nameLookup.getOrDefault(l.getStudentEmail(), l.getStudentEmail()),
                        Collectors.counting()
                ));

        Map<String, Long> canceledByUser = allLessons.stream()
                .filter(l -> "CANCELED".equals(l.getStatus()))
                .collect(Collectors.groupingBy(
                        l -> nameLookup.getOrDefault(l.getStudentEmail(), l.getStudentEmail()),
                        Collectors.counting()
                ));

        Map<String, Long> videosByUser = allVideos.stream()
                .collect(Collectors.groupingBy(
                        v -> nameLookup.getOrDefault(v.getUploaderEmail(), v.getUploaderEmail()),
                        Collectors.counting()
                ));

        // Pass these maps to Thymeleaf (Thymeleaf will automatically convert them to JSON for our JavaScript)
        model.addAttribute("scheduledByUser", scheduledByUser);
        model.addAttribute("pendingByUser", pendingByUser);
        model.addAttribute("canceledByUser", canceledByUser);
        model.addAttribute("videosByUser", videosByUser);
        // 2. DATA FOR THE UNIFIED HISTORY TABLE
        List<ActivityDTO> activityHistory = new ArrayList<>();

        // Map Lessons to Activity
        for (Lesson l : allLessons) {
            LocalDateTime dt = l.getLessonDate().atTime(l.getLessonTime());
            String desc = "Lesson requested for " + l.getLessonDate();
            activityHistory.add(new ActivityDTO(dt, l.getStudentEmail(), "LESSON", desc, l.getStatus()));
        }

        // Map Videos to Activity
        for (PracticeVideo v : allVideos) {
            String desc = "Uploaded practice video: " + v.getTitle();
            activityHistory.add(new ActivityDTO(v.getUploadedAt(), v.getUploaderEmail(), "VIDEO", desc, "UPLOADED"));
        }

        // Sort everything by Timestamp (Newest first)
        activityHistory.sort(Comparator.comparing(ActivityDTO::getTimestamp).reversed());

        model.addAttribute("activities", activityHistory);
        return "admin-reports";
    }
}