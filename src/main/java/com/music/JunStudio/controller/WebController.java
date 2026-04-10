package com.music.JunStudio.controller;

import com.music.JunStudio.dto.AdminLessonDTO;
import com.music.JunStudio.model.Lesson;
import com.music.JunStudio.model.ScheduleOverride;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.LessonRepository;
import com.music.JunStudio.repository.ScheduleOverrideRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.music.JunStudio.service.EmailService;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ScheduleOverrideRepository overrideRepository;

    @GetMapping("/")
    public String MainPage(){
        return "index";
    }

    // 1. Show the HTML page when they go to localhost:8080/register
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register"; // Looks for register.html in your templates folder
    }

    // 2. Catch the form data when they click submit
    @PostMapping("/register")
    public String registerNewUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam("password") String password) {

        try {
            // 1. Check if the user already exists
            if (userRepository.findByEmail(email).isPresent()) {
                return "redirect:/login?error=emailExists";
            }

            // 2. Create and populate the new User entity
            User newUser = new User();
            newUser.setFirstName(firstName.trim());
            newUser.setLastName(lastName.trim());
            newUser.setPhoneNumber(phoneNumber.trim());
            newUser.setEmail(email.trim().toLowerCase());

            // 3. Hash the password and set defaults
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setRole("ROLE_STUDENT");
            newUser.setLessonCredits(0);

            // 4. Save to the database
            userRepository.save(newUser);

            // 5. Success! Redirect to login with the registered flag
            return "redirect:/login?registered=true";

        } catch (Exception e) {
            // If the database rejects the save (e.g., column constraint violation), catch it here
            System.err.println("REGISTRATION FAILED: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=dbError";
        }
    }



    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String displayName = currentUser.getFirstName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = currentUser.getEmail();
        }
        model.addAttribute("pageTitle", "Welcome, " + displayName);
        model.addAttribute("credits", currentUser.getLessonCredits());

        // Check the role
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            // ADMIN VIEW: Grab pending requests and attach user details
            List<Lesson> pendingLessons = lessonRepository.findByStatus("PENDING");
            List<AdminLessonDTO> adminLessons = new ArrayList<>();

            for (Lesson lesson : pendingLessons) {
                // Find the user who made the request
                User student = userRepository.findByEmail(lesson.getStudentEmail()).orElse(new User());
                adminLessons.add(new AdminLessonDTO(
                        lesson.getId(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getEmail(),
                        student.getPhoneNumber(),
                        lesson.getLessonDate(),
                        lesson.getLessonTime()
                ));
            }
            model.addAttribute("pendingLessons", adminLessons);
            // NEW: Fetch all schedule overrides to display
            List<ScheduleOverride> overrides = overrideRepository.findAll();
            model.addAttribute("overrides", overrides);
        } else {
            // STUDENT VIEW: Grab their approved scheduled lessons
            List<Lesson> scheduledLessons = lessonRepository.findByStudentEmailAndStatus(currentUser.getEmail(), "SCHEDULED");
            model.addAttribute("scheduledLessons", scheduledLessons);
        }

        return "dashboard";
    }

    // NEW ENDPOINT: Handle the Admin clicking "Approve"
    @PostMapping("/lesson/approve")
    public String approveLesson(@RequestParam Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // 1. Change status to SCHEDULED and save!
        lesson.setStatus("SCHEDULED");
        lessonRepository.save(lesson);

        // 2. Draft and send the Approval email!
        String subject = "Jun Studio: Lesson Approved!";
        String body = "Great news!\n\nYour music lesson on " + lesson.getLessonDate() +
                " at " + lesson.getLessonTime() + " has been officially SCHEDULED.\n\n" +
                "We look forward to seeing you!\n- Jun Studio";

        // 3. The send action might take a second or two to connect to Gmail
        emailService.sendSimpleEmail(lesson.getStudentEmail(), subject, body);

        // 4. Redirect back to the admin dashboard with a success flag
        return "redirect:/dashboard?approved=true";
    }

    // NEW ENDPOINT: Handle the Admin clicking "Cancel"
    @PostMapping("/lesson/cancel")
    public String cancelLesson(@RequestParam Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // 1. Change lesson status to CANCELED and save
        lesson.setStatus("CANCELED");
        lessonRepository.save(lesson);

        // 2. THE REFUND LOGIC: Find the student and return their credit
        userRepository.findByEmail(lesson.getStudentEmail()).ifPresent(student -> {
            student.setLessonCredits(student.getLessonCredits() + 1);
            userRepository.save(student);
        });

        // 3. Draft and send the Cancellation email!
        String subject = "Jun Studio: Lesson Update";
        String body = "Hello,\n\nUnfortunately, we had to cancel your lesson request for " +
                lesson.getLessonDate() + " at " + lesson.getLessonTime() + " due to a scheduling conflict.\n\n" +
                "Your lesson credit has been fully refunded to your account.\n\n" +
                "Please log in to schedule a new time.\n- Jun Studio";

        emailService.sendSimpleEmail(lesson.getStudentEmail(), subject, body);

        // 4. Redirect back to the admin dashboard
        return "redirect:/dashboard?canceled=true";
    }
    @GetMapping("/schedule")
    public String showSchedulePage(){
        return "schedule";
    }

    @PostMapping("/schedule")
    public String requestLesson(
            @RequestParam LocalDate lessonDate,
            @RequestParam LocalTime lessonTime,
            Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Check for credits
        if (currentUser.getLessonCredits() <= 0) {
            return "redirect:/dashboard?nocredit=true";
        }

        // 2. Check for past dates
        if (lessonDate.isBefore(LocalDate.now()) ||
                (lessonDate.isEqual(LocalDate.now()) && lessonTime.isBefore(LocalTime.now()))) {
            return "redirect:/schedule?error=pastDate";
        }

        // 3. NEW: Check for Double-Booking!
        // We look for any existing lesson at this exact date and time that is NOT canceled.
        boolean isTimeTaken = lessonRepository.existsByLessonDateAndLessonTimeAndStatusNot(lessonDate, lessonTime, "CANCELED");

        if (isTimeTaken) {
            // Bounce them back to the form with a specific error flag
            return "redirect:/schedule?error=timeTaken";
        }

        // 4. Everything is valid! Save the lesson.
        Lesson newLesson = new Lesson();
        newLesson.setStudentEmail(currentUser.getEmail());
        newLesson.setLessonDate(lessonDate);
        newLesson.setLessonTime(lessonTime);
        newLesson.setStatus("PENDING"); // Explicitly mark as pending
        lessonRepository.save(newLesson);

        // 5. Deduct the credit
        currentUser.setLessonCredits(currentUser.getLessonCredits() - 1);
        userRepository.save(currentUser);

        return "redirect:/?lessonRequested=true";
    }

    // Show the custom login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // Show the My Schedule page
    @GetMapping("/my-schedule")
    public String showMySchedule(Model model, Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            // ADMIN VIEW: Fetch all lessons and categorize them by status
            List<Lesson> allLessons = lessonRepository.findAll();

            List<Lesson> scheduled = allLessons.stream().filter(l -> "SCHEDULED".equals(l.getStatus())).toList();
            List<Lesson> pending = allLessons.stream().filter(l -> "PENDING".equals(l.getStatus())).toList();
            List<Lesson> canceled = allLessons.stream().filter(l -> "CANCELED".equals(l.getStatus())).toList();

            model.addAttribute("scheduledLessons", scheduled);
            model.addAttribute("pendingLessons", pending);
            model.addAttribute("canceledLessons", canceled);
        } else {
            // STUDENT VIEW: Fetch only their personal lessons
            List<Lesson> myLessons = lessonRepository.findByStudentEmail(currentUser.getEmail());
            model.addAttribute("studentLessons", myLessons);
        }

        return "my-schedule";
    }

    @GetMapping("/api/available-times")
    @ResponseBody
    public List<String> getAvailableTimes(@RequestParam LocalDate date) {

        // 1. Set Defaults (9 AM to 11 PM)
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(23, 0); // 11 PM

        // 2. Check if Admin overrode this specific date
        ScheduleOverride override = overrideRepository.findByOverrideDate(date).orElse(null);
        if (override != null) {
            if (override.isClosed()) return Collections.emptyList();
            start = override.getStartTime();
            end = override.getEndTime();
        }

        // 3. Get times that are already booked by other students
        List<Lesson> bookedLessons = lessonRepository.findByLessonDateAndStatusNot(date, "CANCELED");
        List<LocalTime> bookedTimes = bookedLessons.stream().map(Lesson::getLessonTime).toList();

        // 4. Generate the hourly slots
        List<String> availableSlots = new ArrayList<>();
        LocalTime current = start;

        while (!current.isAfter(end)) {

            // 1. Check if this specific hour has already passed today
            boolean isPastToday = date.isEqual(LocalDate.now()) && current.isBefore(LocalTime.now());

            // 2. If it's in the future AND nobody else booked it, add it to the list
            if (!isPastToday && !bookedTimes.contains(current)) {
                availableSlots.add(current.toString());
            }

            // 3. THE FIX: Stop the loop if we just processed the final hour.
            // This prevents 23:00 from wrapping around to 00:00 and looping infinitely!
            if (current.equals(end)) {
                break;
            }

            // 4. Move to the next hour
            current = current.plusHours(1);
        }

        return availableSlots;
    }

    @PostMapping("/admin/override")
    public String setScheduleOverride(
            @RequestParam LocalDate overrideDate,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(required = false, defaultValue = "false") boolean isClosed) {

        // Check if an override already exists for this date, or create a new one
        ScheduleOverride override = overrideRepository.findByOverrideDate(overrideDate)
                .orElse(new ScheduleOverride());

        override.setOverrideDate(overrideDate);
        override.setClosed(isClosed);

        // If they are closed, we don't care about the hours
        if (!isClosed) {
            override.setStartTime(startTime);
            override.setEndTime(endTime);
        } else {
            override.setStartTime(null);
            override.setEndTime(null);
        }

        overrideRepository.save(override);

        return "redirect:/dashboard?overrideSaved=true";
    }
}