package com.music.JunStudio.controller;

import com.music.JunStudio.model.SemesterRegistration;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.SemesterRegistrationRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/semester/register")
public class SemesterRegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SemesterRegistrationRepository registrationRepository;

    // 1. Show the Form
    @GetMapping
    public String showRegistrationForm(Model model, Principal principal) {
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("isAdmin", "ROLE_ADMIN".equals(currentUser.getRole()));

        // Filter the user table for ACTIVE teachers only
        List<User> activeTeachers = userRepository.findAll().stream()
                .filter(u -> "ROLE_TEACHER".equals(u.getRole()) && u.isActive())
                .toList();

        model.addAttribute("teachers", activeTeachers);

        // Pass the current year to the form so they can select 2026 or 2027
        int currentYear = LocalDate.now().getYear();
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("nextYear", currentYear + 1);

        return "semester-registration";
    }

    // 2. Handle the Form Submission
    @PostMapping
    @Transactional//Ensures both database saves succeed , or neither do
    public String submitRegistration(
            @RequestParam Long teacherId,
            @RequestParam String term,
            @RequestParam int year,
            @RequestParam String preferredDayOne,
            @RequestParam(required = false) String preferredDayTwo,
            @RequestParam(required = false) String memo,
            Principal principal) {

        User student = userRepository.findByEmail(principal.getName()).orElseThrow();
        User teacher = userRepository.findById(teacherId).orElseThrow();

        // ==========================================
        // 1. THE GUARD CLAUSE (Credit Check)
        // ==========================================
        if (student.getLessonCredits() < 16) {
            return "redirect:/dashboard?nocredit=true";
        }

        // Build the registration request
        SemesterRegistration registration = new SemesterRegistration();
        registration.setStudent(student);
        registration.setTeacher(teacher);
        registration.setTerm(term);
        registration.setYear(year);
        registration.setPreferredDayOne(preferredDayOne);
        registration.setPreferredDayTwo(preferredDayTwo);
        registration.setMemo(memo);
        registration.setStatus("PENDING");

        registrationRepository.save(registration);

        // ==========================================
        // 2. THE CHARGE (Deduct Credits)
        // ==========================================
        student.setLessonCredits(student.getLessonCredits() - 16);
        userRepository.save(student);

        // Redirect back to dashboard with a success flag
        return "redirect:/dashboard?semesterRequested=true";
    }
}