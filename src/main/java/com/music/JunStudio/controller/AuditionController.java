package com.music.JunStudio.controller;

import com.music.JunStudio.model.AuditionVideo;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.AuditionVideoRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Controller
@RequestMapping("/audition")
public class AuditionController {

    @Autowired
    private AuditionVideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Show the Gallery (with optional Search)
    @GetMapping
    public String showAuditionGallery(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            Model model,
            Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(currentUser.getRole());

        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<AuditionVideo> videoPage;

        if (isAdmin) {
            if (search != null && !search.trim().isEmpty()) {
                videoPage = videoRepository.findByTitleContainingIgnoreCaseOrUploaderNameContainingIgnoreCase(search, search, pageable);
            } else {
                videoPage = videoRepository.findAll(pageable);
            }
        } else if (isTeacher) {
            if (search != null && !search.trim().isEmpty()) {
                videoPage = videoRepository.searchForTeacher(search, currentUser.getEmail(), currentUser.getId(), pageable);
            } else {
                videoPage = videoRepository.findAllForTeacher(currentUser.getEmail(), currentUser.getId(), pageable);
            }
        } else {
            if (search != null && !search.trim().isEmpty()) {
                videoPage = videoRepository.searchForStudent(search, currentUser.getEmail(), pageable);
            } else {
                videoPage = videoRepository.findAllForStudent(currentUser.getEmail(), pageable);
            }
        }

        model.addAttribute("videoPage", videoPage);
        model.addAttribute("search", search);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);

        return "audition-gallery";
    }

    // 2. View a single video
    @GetMapping("/{id}")
    public String viewVideo(@PathVariable Long id, Model model, Principal principal) {
        AuditionVideo video = videoRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();

        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        boolean isTeacher = "ROLE_TEACHER".equals(currentUser.getRole());
        boolean isAssignedTeacher = false;

        if (isTeacher) {
            User uploader = userRepository.findByEmail(video.getUploaderEmail()).orElse(null);
            if (uploader != null && uploader.getAssignedTeacher() != null) {
                isAssignedTeacher = uploader.getAssignedTeacher().getId().equals(currentUser.getId());
            }
        }

        if (video.isPrivate()
                && !isAdmin
                && !isAssignedTeacher
                && !video.getUploaderEmail().equals(currentUser.getEmail())) {
            return "redirect:/audition?error=unauthorized";
        }

        model.addAttribute("video", video);
        model.addAttribute("currentUserEmail", currentUser.getEmail());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);

        return "audition-detail";
    }

    // 3. Upload a video
    @PostMapping("/upload")
    public String uploadAuditionVideo(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String videoType,
            @RequestParam(required = false) String youtubeUrl,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") boolean isPrivate,
            Principal principal) {

        User uploader = userRepository.findByEmail(principal.getName()).orElseThrow();
        AuditionVideo video = new AuditionVideo();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoType(videoType);
        video.setUploaderEmail(uploader.getEmail());
        video.setUploaderName(uploader.getFirstName() + " " + uploader.getLastName());
        video.setPrivate(isPrivate);

        try {
            if ("YOUTUBE".equals(videoType)) {
                String embedUrl = youtubeUrl;
                // Use URL parsing instead of regex to avoid ReDoS vulnerability
                if (youtubeUrl != null && youtubeUrl.length() <= 2048) {
                    try {
                        java.net.URI uri = new java.net.URI(youtubeUrl);
                        String host = uri.getHost();
                        String path = uri.getPath();
                        String query = uri.getQuery();
                        String videoId = null;

                        if (host != null && (host.contains("youtube.com") || host.contains("youtu.be"))) {
                            if (host.contains("youtu.be") && path != null) {
                                videoId = path.replaceFirst("^/", "");
                            } else if (path != null && path.startsWith("/shorts/")) {
                                videoId = path.substring("/shorts/".length());
                            } else if (path != null && (path.startsWith("/v/") || path.startsWith("/embed/"))) {
                                videoId = path.substring(path.lastIndexOf("/") + 1);
                            } else if (query != null) {
                                for (String param : query.split("&")) {
                                    if (param.startsWith("v=")) {
                                        videoId = param.substring(2);
                                        break;
                                    }
                                }
                            }
                        }
                        if (videoId != null && videoId.matches("[A-Za-z0-9_\\-]{11}")) {
                            embedUrl = "https://www.youtube.com/embed/" + videoId;
                        }
                    } catch (Exception e) {
                        // Keep original URL if parsing fails
                    }
                }
                video.setVideoUrl(embedUrl);

            } else if ("LOCAL_FILE".equals(videoType) && file != null && !file.isEmpty()) {
Path uploadPath = Paths.get("uploads/audition-videos/").toAbsolutePath().normalize();
if (!Files.exists(uploadPath)) {
    Files.createDirectories(uploadPath);
}
// Sanitize filename: extract only the extension and strip unsafe characters
String originalFilename = file.getOriginalFilename();
originalFilename = (originalFilename == null) ? "" : Paths.get(originalFilename).getFileName().toString();
int dotIndex = originalFilename.lastIndexOf(".");
String extension = (dotIndex >= 0) ? originalFilename.substring(dotIndex).replaceAll("[^a-zA-Z0-9.]", "") : "";
String newFilename = UUID.randomUUID().toString() + extension;
Path filePath = uploadPath.resolve(newFilename).normalize();
if (!filePath.startsWith(uploadPath)) {
    return "redirect:/audition?error=uploadFailed";
}
Files.copy(file.getInputStream(), filePath);
                video.setVideoUrl("/uploads/audition-videos/" + newFilename);
            }

            videoRepository.save(video);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/audition?error=uploadFailed";
        }

        return "redirect:/audition?uploadSuccess=true";
    }

    // 4. Delete a video
    @PostMapping("/{id}/delete")
    public String deleteVideo(@PathVariable Long id, Principal principal) {
        AuditionVideo video = videoRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        if (!video.getUploaderEmail().equals(currentUser.getEmail()) && !isAdmin) {
            return "redirect:/audition/" + id + "?error=unauthorized";
        }

        if ("LOCAL_FILE".equals(video.getVideoType())) {
            try {
                String filename = video.getVideoUrl().substring(video.getVideoUrl().lastIndexOf("/") + 1);
                java.nio.file.Path filePath = java.nio.file.Paths.get("uploads/audition-videos/").resolve(filename);
                java.nio.file.Files.deleteIfExists(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        videoRepository.delete(video);
        return "redirect:/audition?deleted=true";
    }

    // 5. Edit video details
    @PostMapping("/{id}/edit")
    public String editVideoDetails(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(required = false, defaultValue = "false") boolean isPrivate,
            Principal principal) {

        AuditionVideo video = videoRepository.findById(id).orElseThrow();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        if (!video.getUploaderEmail().equals(currentUser.getEmail()) && !isAdmin) {
            return "redirect:/audition/" + id + "?error=unauthorized";
        }

        video.setTitle(title);
        video.setDescription(description);
        video.setPrivate(isPrivate);
        videoRepository.save(video);

        return "redirect:/audition/" + id + "?updated=true";
    }
}
