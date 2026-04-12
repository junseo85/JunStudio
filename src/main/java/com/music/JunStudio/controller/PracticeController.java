package com.music.JunStudio.controller;

import com.music.JunStudio.model.PracticeVideo;
import com.music.JunStudio.model.VideoFeedback;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.PracticeVideoRepository;
import com.music.JunStudio.repository.VideoFeedbackRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/practice")
public class PracticeController {

    @Autowired
    private PracticeVideoRepository videoRepository;

    @Autowired
    private VideoFeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Show the Gallery (with optional Search)
    @GetMapping
    public String showPracticeGallery(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,  // Default to Page 1 (Index 0)
            @RequestParam(defaultValue = "2") int size, // Show 12 videos per page ----I manually set as 2 for testing purpose
            Model model,
            Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        // Build the pagination request (sorted newest first)
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());

        Page<PracticeVideo> videoPage;

        if (isAdmin) {
            if (search != null && !search.trim().isEmpty()) {
                videoPage = videoRepository.findByTitleContainingIgnoreCaseOrUploaderNameContainingIgnoreCase(search, search, pageable);
            } else {
                videoPage = videoRepository.findAll(pageable); // Built-in method
            }
        } else {
            if (search != null && !search.trim().isEmpty()) {
                videoPage = videoRepository.searchForStudent(search, currentUser.getEmail(), pageable);
            } else {
                videoPage = videoRepository.findAllForStudent(currentUser.getEmail(), pageable);
            }
        }

        // Pass the entire Page object to Thymeleaf, along with the search term
        model.addAttribute("videoPage", videoPage);
        model.addAttribute("search", search);

        //THE FIX: Tell the HTML navigation bar this is an Admin
        model.addAttribute("isAdmin", isAdmin);

        return "practice-gallery";
    }

    // 2. View a single video and its comments
    @GetMapping("/{id}")
    public String viewVideo(@PathVariable Long id, Model model, Principal principal) {
        PracticeVideo video = videoRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        // NEW: Hard security check to prevent students from guessing URLs of private videos
        if (video.isPrivate() && !isAdmin && !video.getUploaderEmail().equals(currentUser.getEmail())) {
            return "redirect:/practice?error=unauthorized";
        }

        List<VideoFeedback> comments = feedbackRepository.findByVideoIdOrderByPostedAtAsc(id);

        model.addAttribute("video", video);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUserEmail", currentUser.getEmail());
        model.addAttribute("isAdmin", isAdmin);

        return "practice-detail";
    }

    // 3. Post a comment
    @PostMapping("/{id}/comment")
    public String postComment(@PathVariable Long id, @RequestParam String commentText, Principal principal) {
        User commenter = userRepository.findByEmail(principal.getName()).orElseThrow();

        VideoFeedback feedback = new VideoFeedback();
        feedback.setVideoId(id);
        feedback.setCommentText(commentText);
        feedback.setCommenterEmail(commenter.getEmail());
        feedback.setCommenterName(commenter.getFirstName() + " " + commenter.getLastName());

        feedbackRepository.save(feedback);
        return "redirect:/practice/" + id;
    }

    @PostMapping("/upload")
    public String uploadPracticeVideo(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String videoType,
            @RequestParam(required = false) String youtubeUrl,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") boolean isPrivate, // NEW
            Principal principal) {

        User uploader = userRepository.findByEmail(principal.getName()).orElseThrow();
        PracticeVideo video = new PracticeVideo();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoType(videoType);
        video.setUploaderEmail(uploader.getEmail());
        video.setUploaderName(uploader.getFirstName() + " " + uploader.getLastName());
        video.setPrivate(isPrivate);

        try {
            if ("YOUTUBE".equals(videoType)) {

                String embedUrl = youtubeUrl;
                // This Regex looks for the 11-character Video ID across all common YouTube formats
                String regex = "(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/|youtube\\.com\\/shorts\\/)([^\"&?\\/\\s]{11})";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(youtubeUrl);

                if (matcher.find()) {
                    // Extract the clean ID and build the official embed link
                    String videoId = matcher.group(1);
                    embedUrl = "https://www.youtube.com/embed/" + videoId;
                }

                video.setVideoUrl(embedUrl);

            } else if ("LOCAL_FILE".equals(videoType) && file != null && !file.isEmpty()) {

                // 1. Ensure the upload directory exists
                Path uploadPath = Paths.get("uploads/practice-videos/");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 2. Generate a unique filename to prevent overwriting
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = UUID.randomUUID().toString() + extension;

                // 3. Save the physical file
                Path filePath = uploadPath.resolve(newFilename);
                Files.copy(file.getInputStream(), filePath);

                // 4. Save the URL path for the database
                video.setVideoUrl("/uploads/practice-videos/" + newFilename);
            }

            videoRepository.save(video);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/practice?error=uploadFailed";
        }

        return "redirect:/practice?uploadSuccess=true";
    }
    // 4. Delete a video
    @PostMapping("/{id}/delete")
    public String deleteVideo(@PathVariable Long id, Principal principal) {
        PracticeVideo video = videoRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        // HARD SECURITY CHECK: Are they the owner or an Admin?
        if (!video.getUploaderEmail().equals(currentUser.getEmail()) && !isAdmin) {
            return "redirect:/practice/" + id + "?error=unauthorized";
        }

        // Clean up 1: Delete physical file from the hard drive (if it's an MP4)
        if ("LOCAL_FILE".equals(video.getVideoType())) {
            try {
                // Extract just the filename from "/uploads/practice-videos/filename.mp4"
                String filename = video.getVideoUrl().substring(video.getVideoUrl().lastIndexOf("/") + 1);
                java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/practice-videos/").resolve(filename);
                java.nio.file.Files.deleteIfExists(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Clean up 2: Delete all attached comments to prevent database constraint errors
        List<VideoFeedback> comments = feedbackRepository.findByVideoIdOrderByPostedAtAsc(id);
        feedbackRepository.deleteAll(comments);

        // Clean up 3: Delete the video record itself
        videoRepository.delete(video);

        return "redirect:/practice?deleted=true";
    }

    // 5. Edit video details
    @PostMapping("/{id}/edit")
    public String editVideoDetails(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false, defaultValue = "false") boolean isPrivate, // NEW
            Principal principal) {

        PracticeVideo video = videoRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        // HARD SECURITY CHECK: Are they the owner or an Admin?
        if (!video.getUploaderEmail().equals(currentUser.getEmail()) && !isAdmin) {
            return "redirect:/practice/" + id + "?error=unauthorized";
        }

        // Update the fields and save
        video.setTitle(title);
        video.setDescription(description);
        video.setPrivate(isPrivate);
        videoRepository.save(video);

        // Send them back to the video page with a success flag
        return "redirect:/practice/" + id + "?updated=true";
    }
}