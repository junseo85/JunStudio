package com.music.JunStudio.controller;

import com.music.JunStudio.dto.AdminLessonDTO;
import com.music.JunStudio.model.Lesson;
import com.music.JunStudio.model.ScheduleOverride;
import com.music.JunStudio.model.SemesterRegistration;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.LessonRepository;
import com.music.JunStudio.repository.ScheduleOverrideRepository;
import com.music.JunStudio.repository.SemesterRegistrationRepository;
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
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.temporal.TemporalAdjusters;
import java.time.LocalDate;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.DayOfWeek;

@Controller
public class WebController {

    @Autowired
    private SemesterRegistrationRepository registrationRepository; //inject new semester registration repository

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
        boolean isTeacher = "ROLE_TEACHER".equals(currentUser.getRole());

        // Pass both flags to the HTML
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);

        if (isAdmin) {
            // ==========================================
            // ADMIN VIEW: Sees entire studio
            // ==========================================
            List<AdminLessonDTO> pendingLessons = lessonRepository.findByStatus("PENDING")
                    .stream().map(this::convertToAdminDTO).toList();
            model.addAttribute("pendingLessons", pendingLessons);

            List<ScheduleOverride> overrides = overrideRepository.findAll();
            model.addAttribute("overrides", overrides);

            // It's better to ask the DB to filter rather than Java Streams
            model.addAttribute("pendingRequests", registrationRepository.findByStatus("PENDING"));

        } else if (isTeacher) {
            // ==========================================
            // TEACHER VIEW: Sees ONLY their own studio
            // ==========================================
            // Uses the custom query we just wrote!
            List<AdminLessonDTO> pendingLessons = lessonRepository.findByTeacherIdAndStatus(currentUser.getId(), "PENDING")
                    .stream().map(this::convertToAdminDTO).toList();
            model.addAttribute("pendingLessons", pendingLessons);

            // Fetch overrides for this specific teacher PLUS global closures
            List<ScheduleOverride> overrides = overrideRepository.findByTeacherIdOrTeacherIsNull(currentUser.getId());
            model.addAttribute("overrides", overrides);

            model.addAttribute("pendingRequests", registrationRepository.findByTeacherIdAndStatus(currentUser.getId(), "PENDING"));

        } else {
            // ==========================================
            // STUDENT VIEW
            // ==========================================
            List<Lesson> scheduledLessons = lessonRepository.findByStudentEmailAndStatus(currentUser.getEmail(), "SCHEDULED");

            // Figure out the teacher's name for each lesson so the UI displays it
            for (Lesson lesson : scheduledLessons) {
                if (lesson.getSemesterRegistration() != null) {
                    User teacher = lesson.getSemesterRegistration().getTeacher();
                    lesson.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
                } else if (currentUser.getAssignedTeacher() != null) {
                    User teacher = currentUser.getAssignedTeacher();
                    lesson.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
                } else {
                    lesson.setTeacherName("Studio Staff");
                }
            }

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
    public String cancelLesson(@RequestParam Long lessonId, HttpServletRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // 1. Change status to CANCELED and save
        lesson.setStatus("CANCELED");
        lessonRepository.save(lesson);

        // 2. Refund the student
        userRepository.findByEmail(lesson.getStudentEmail()).ifPresent(student -> {
            student.setLessonCredits(student.getLessonCredits() + 1);
            userRepository.save(student);
        });

        // 3. Send the cancellation email
        String subject = "Jun Studio: Lesson Update";
        String body = "Hello,\n\nUnfortunately, we had to cancel your lesson request for " +
                lesson.getLessonDate() + " at " + lesson.getLessonTime() + " due to a scheduling conflict.\n\n" +
                "Your lesson credit has been fully refunded to your account.\n\n" +
                "Please log in to schedule a new time.\n- Jun Studio";

        emailService.sendSimpleEmail(lesson.getStudentEmail(), subject, body);

        // 4. THE FIX: Smart Redirect
        // Check where the admin clicked the button from, and send them back there!
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/my-schedule")) {
            return "redirect:/my-schedule?canceled=true";
        }

        return "redirect:/dashboard?canceled=true";
    }

    @GetMapping("/schedule")
    public String showSchedulePage(Model model, Principal principal) {

        // 1. Find who is currently looking at the page
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if they are an Admin
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());

        // 3. Pass that flag to your Header.html!
        model.addAttribute("isAdmin", isAdmin);

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

        // ---------------------------------------------------------
        // 3. HARD SECURITY: Enforce Schedule Rules (Overrides & Weekends)
        // ---------------------------------------------------------
        List<ScheduleOverride> overrides = overrideRepository.findByOverrideDate(lessonDate);
        User teacher = currentUser.getAssignedTeacher(); // Find out who their teacher is

        for (ScheduleOverride override : overrides) {
            // Check if this override is Global (null) OR belongs to this student's teacher
            if (override.getTeacher() == null ||
                    (teacher != null && override.getTeacher().getId().equals(teacher.getId()))) {

                // Rule A: Is the day fully closed?
                if (override.isClosed()) {
                    return "redirect:/schedule?error=studioClosed";
                }
                // Rule B: Ensure they aren't booking outside custom hours
                if (lessonTime.isBefore(override.getStartTime()) || lessonTime.isAfter(override.getEndTime())) {
                    return "redirect:/schedule?error=outsideHours";
                }
            }
        }

        // Rule C & D: Standard rules if no override was found above
        DayOfWeek day = lessonDate.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return "redirect:/schedule?error=weekendBlocked";
        }
        if (lessonTime.isBefore(LocalTime.of(9, 0)) || lessonTime.isAfter(LocalTime.of(23, 0))) {
            return "redirect:/schedule?error=outsideHours";
        }
        // ---------------------------------------------------------
        // ---------------------------------------------------------

        // 4. Check for Double-Booking
        boolean isTimeTaken = lessonRepository.existsByLessonDateAndLessonTimeAndStatusNot(lessonDate, lessonTime, "CANCELED");
        if (isTimeTaken) {
            return "redirect:/schedule?error=timeTaken";
        }

        // 5. Everything is valid! Save the lesson.
        Lesson newLesson = new Lesson();
        newLesson.setStudentEmail(currentUser.getEmail());
        newLesson.setLessonDate(lessonDate);
        newLesson.setLessonTime(lessonTime);
        newLesson.setStatus("PENDING");
        lessonRepository.save(newLesson);

        // 6. Deduct the credit
        currentUser.setLessonCredits(currentUser.getLessonCredits() - 1);
        userRepository.save(currentUser);

        return "redirect:/?lessonRequested=true";
    }

    // Show the custom login page
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/my-schedule")
    public String showMySchedule(Model model, Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole());
        model.addAttribute("isAdmin", isAdmin);

        boolean isTeacher = "ROLE_TEACHER".equals(currentUser.getRole());
        model.addAttribute("isTeacher", isTeacher);

        if (isAdmin) {
            // ==========================================
            // ADMIN VIEW: Sees entire studio (Optimized SQL)
            // ==========================================
            List<AdminLessonDTO> scheduled = lessonRepository.findByStatus("SCHEDULED")
                    .stream().map(this::convertToAdminDTO).toList();

            List<AdminLessonDTO> pending = lessonRepository.findByStatus("PENDING")
                    .stream().map(this::convertToAdminDTO).toList();

            List<AdminLessonDTO> canceled = lessonRepository.findByStatus("CANCELED")
                    .stream().map(this::convertToAdminDTO).toList();

            model.addAttribute("scheduledLessons", scheduled);
            model.addAttribute("pendingLessons", pending);
            model.addAttribute("canceledLessons", canceled);

        } else if (isTeacher) {
            // ==========================================
            // TEACHER VIEW: Sees ONLY their own students
            // ==========================================
            // Note: You will need to add these custom finder methods to your LessonRepository!
            List<AdminLessonDTO> scheduled = lessonRepository.findByTeacherIdAndStatus(currentUser.getId(), "SCHEDULED")
                    .stream().map(this::convertToAdminDTO).toList();

            List<AdminLessonDTO> pending = lessonRepository.findByTeacherIdAndStatus(currentUser.getId(), "PENDING")
                    .stream().map(this::convertToAdminDTO).toList();

            List<AdminLessonDTO> canceled = lessonRepository.findByTeacherIdAndStatus(currentUser.getId(), "CANCELED")
                    .stream().map(this::convertToAdminDTO).toList();

            model.addAttribute("scheduledLessons", scheduled);
            model.addAttribute("pendingLessons", pending);
            model.addAttribute("canceledLessons", canceled);

        } else {
            // ==========================================
            // STUDENT VIEW
            // ==========================================
            List<Lesson> scheduledLessons = lessonRepository.findByStudentEmailAndStatus(currentUser.getEmail(), "SCHEDULED");

            // NEW: Figure out the teacher's name for each lesson
            for (Lesson lesson : scheduledLessons) {
                if (lesson.getSemesterRegistration() != null) {
                    // It's a semester lesson: pull the teacher from the registration block
                    User teacher = lesson.getSemesterRegistration().getTeacher();
                    lesson.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
                } else if (currentUser.getAssignedTeacher() != null) {
                    // It's a single makeup lesson: pull their default assigned teacher
                    User teacher = currentUser.getAssignedTeacher();
                    lesson.setTeacherName(teacher.getFirstName() + " " + teacher.getLastName());
                } else {
                    // Fallback just in case
                    lesson.setTeacherName("Studio Staff");
                }
            }

            model.addAttribute("scheduledLessons", scheduledLessons);
        }

        return "my-schedule";
    }

    // HELPER METHOD: Looks up the user to attach their first and last name to the lesson data
    // Helper method to build the DTO
    private AdminLessonDTO convertToAdminDTO(Lesson lesson) {
        User student = userRepository.findByEmail(lesson.getStudentEmail()).orElse(new User());

        // 1. Determine the teacher's name safely
        String teacherName = "Studio Staff";
        if (lesson.getSemesterRegistration() != null) {
            User teacher = lesson.getSemesterRegistration().getTeacher();
            teacherName = teacher.getFirstName() + " " + teacher.getLastName();
        } else if (student.getAssignedTeacher() != null) {
            User teacher = student.getAssignedTeacher();
            teacherName = teacher.getFirstName() + " " + teacher.getLastName();
        }

        // 2. Return the DTO with the new 8th argument included
        return new AdminLessonDTO(
                lesson.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getPhoneNumber(),
                lesson.getLessonDate(),
                lesson.getLessonTime(),
                teacherName // NEW
        );
    }

    @GetMapping("/api/available-times")
    @ResponseBody
    public List<String> getAvailableTimes(@RequestParam LocalDate date, Principal principal) {

        // 1. Find the current user and their assigned teacher
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User teacher = currentUser.getAssignedTeacher();

        // Standard weekday hours
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(23, 0);

        // 2. Block weekends by default
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return Collections.emptyList();
        }

        // 3. THE FIX: Check for Global or Teacher-specific overrides using a List
        List<ScheduleOverride> overrides = overrideRepository.findByOverrideDate(date);

        for (ScheduleOverride override : overrides) {
            // Does this apply to the whole studio (null) OR this specific teacher?
            if (override.getTeacher() == null ||
                    (teacher != null && override.getTeacher().getId().equals(teacher.getId()))) {

                // If they are closed, return the empty list immediately
                if (override.isClosed()) return Collections.emptyList();

                // Otherwise, adjust the start and end times
                start = override.getStartTime();
                end = override.getEndTime();
            }
        }

        // 4. Get times that are already booked
        List<Lesson> bookedLessons = lessonRepository.findByLessonDateAndStatusNot(date, "CANCELED");
        List<LocalTime> bookedTimes = bookedLessons.stream().map(Lesson::getLessonTime).toList();

        // 5. Generate the hourly slots safely
        List<String> availableSlots = new ArrayList<>();
        LocalTime current = start;

        while (!current.isAfter(end)) {
            boolean isPastToday = date.isEqual(LocalDate.now()) && current.isBefore(LocalTime.now());

            if (!isPastToday && !bookedTimes.contains(current)) {
                availableSlots.add(current.toString());
            }

            if (current.equals(end)) {
                break;
            }

            current = current.plusHours(1);
        }

        return availableSlots;
    }

    @PostMapping("/admin/override")
    public String setScheduleOverride(
            @RequestParam LocalDate overrideDate,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(required = false, defaultValue = "false") boolean isClosed,
            Principal principal) {

        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        boolean isTeacher = "ROLE_TEACHER".equals(currentUser.getRole());

        // Find existing overrides for this date
        List<ScheduleOverride> existingOverrides = overrideRepository.findByOverrideDate(overrideDate);

        // Find if THIS specific user already has an override for this date, otherwise create new
        ScheduleOverride override = existingOverrides.stream()
                .filter(o -> isTeacher ?
                        (o.getTeacher() != null && o.getTeacher().getId().equals(currentUser.getId())) :
                        (o.getTeacher() == null)) // Admins manage the null (global) override
                .findFirst()
                .orElse(new ScheduleOverride());

        override.setOverrideDate(overrideDate);
        override.setClosed(isClosed);

        if (!isClosed) {
            override.setStartTime(startTime);
            override.setEndTime(endTime);
        } else {
            override.setStartTime(null);
            override.setEndTime(null);
        }

        // The Magic Rule: Bind it to the teacher if they are the one creating it
        if (isTeacher) {
            override.setTeacher(currentUser);
        }

        overrideRepository.save(override);

        return "redirect:/dashboard?overrideSaved=true";
    }

    // ==========================================
    // RECEIPT: DOWNLOAD PDF
    // ==========================================
    @GetMapping("/receipt/pdf")
    public void downloadReceiptPDF(
            @RequestParam(defaultValue = "1") int qty,
            HttpServletResponse response,
            Principal principal) {

        try {
            User user = userRepository.findByEmail(principal.getName()).orElseThrow();
            int total = qty * 75; // Assuming $75 per credit

            // Tell the browser to download this as a PDF file
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"JunStudio_Receipt.pdf\"");

            // Build the PDF Document
            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // Draw the PDF content
            document.add(new Paragraph("JUN STUDIO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24)));
            document.add(new Paragraph("Official Payment Receipt\n\n"));

            document.add(new Paragraph("Date: " + LocalDate.now()));
            document.add(new Paragraph("Customer: " + user.getFirstName() + " " + user.getLastName()));
            document.add(new Paragraph("Email: " + user.getEmail() + "\n\n"));

            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("Item: Lesson Credit"));
            document.add(new Paragraph("Quantity: " + qty));
            document.add(new Paragraph("Price per unit: $75.00"));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("TOTAL PAID: $" + total + ".00", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

            document.add(new Paragraph("\n\nThank you for choosing Jun Studio!"));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // RECEIPT: SEND EMAIL
    // ==========================================
    @PostMapping("/receipt/email")
    public String emailReceipt(@RequestParam(defaultValue = "1") int qty, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        int total = qty * 75;

        String subject = "Your Receipt from Jun Studio";
        String body = "Hello " + user.getFirstName() + ",\n\n" +
                "Thank you for your purchase! Here is your receipt:\n\n" +
                "Date: " + LocalDate.now() + "\n" +
                "Item: " + qty + "x Lesson Credit(s)\n" +
                "Total Paid: $" + total + ".00\n\n" +
                "Your credits have been added to your account.\n- Jun Studio";

        emailService.sendSimpleEmail(user.getEmail(), subject, body);

        // Redirect back to dashboard with a new success flag
        return "redirect:/dashboard?receiptEmailed=true";
    }

    //Teacher Assigns a Time Slot
    @PostMapping("/semester/assign")
    public String assignSemesterSlot( @RequestParam Long registrationId, @RequestParam DayOfWeek assignedDay, @RequestParam LocalTime assignedTime){
        SemesterRegistration registration = registrationRepository.findById(registrationId).orElseThrow(() -> new RuntimeException("Registration not found"));

        //update the registration with the permanent schedule
        registration.setAssignedDay(assignedDay);
        registration.setAssignedTime(assignedTime);
        registration.setStatus("ASSIGNED");

        registrationRepository.save(registration);

        // ==========================================
        // 2. THE LESSON GENERATOR (16 Weeks)
        // ==========================================

        // A. Determine the exact start date based on the term
        int startMonth = switch (registration.getTerm()) {
            case "SPRING" -> 1; // January 1st
            case "SUMMER" -> 6; // June 1st
            case "FALL" -> 9;   // September 1st
            default -> LocalDate.now().getMonthValue();
        };

        LocalDate semesterStart = LocalDate.of(registration.getYear(), startMonth, 1);

        // B. Find the very first occurrence of the assigned day in that month
        // (e.g., If Sept 1st is a Wednesday, and they chose Friday, this finds Sept 3rd)
        LocalDate firstLessonDate = semesterStart.with(TemporalAdjusters.nextOrSame(assignedDay));

        // C. Generate the 16 lessons in memory (Smart Loop)
        List<Lesson> semesterLessons = new ArrayList<>();
        LocalDate currentDate = firstLessonDate;
        int lessonsBooked = 0;

        // Keep looping until we successfully book exactly 16 lessons
        while (lessonsBooked < 16) {

            // 1. Check if this specific date has an override (Holiday OR Teacher Blocked)
            List<ScheduleOverride> dailyOverrides = overrideRepository.findByOverrideDate(currentDate);
            boolean isDateBlocked = false;

            for (ScheduleOverride override : dailyOverrides) {

                // Does this override apply to us? (Either it's global, or it belongs to our teacher)
                boolean appliesToUs = (override.getTeacher() == null) ||
                        (override.getTeacher().getId().equals(registration.getTeacher().getId()));

                if (appliesToUs) {
                    if (override.isClosed()) {
                        isDateBlocked = true;
                        break; // Stop checking, the day is dead
                    } else if (assignedTime.isBefore(override.getStartTime()) || assignedTime.isAfter(override.getEndTime())) {
                        isDateBlocked = true;
                        break; // Stop checking, the time is outside their custom hours
                    }
                }
            }

            // 2. If the date is NOT blocked, create the lesson
            if (!isDateBlocked) {
                Lesson lesson = new Lesson();
                lesson.setStudentEmail(registration.getStudent().getEmail());
                lesson.setLessonDate(currentDate);
                lesson.setLessonTime(assignedTime);
                lesson.setStatus("SCHEDULED");
                lesson.setSemesterRegistration(registration);

                semesterLessons.add(lesson);

                // Only count up when we successfully place a lesson!
                lessonsBooked++;
            }

            // 3. Always jump exactly one week forward to check the next date
            currentDate = currentDate.plusWeeks(1);

            // Safety measure: Prevent infinite loops just in case the studio is closed for a whole year
            if (currentDate.isAfter(semesterStart.plusYears(1))) {
                break;
            }
        }

        // D. Save all 16 lessons to the database in one highly efficient query
        lessonRepository.saveAll(semesterLessons);

        return "redirect://dashboard?slotAssigned=true";
    }


}