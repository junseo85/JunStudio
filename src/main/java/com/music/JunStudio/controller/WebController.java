package com.music.JunStudio.controller;

import com.music.JunStudio.model.Lesson;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.LessonRepository;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class WebController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LessonRepository lessonRepository;

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
    public String registerNewUser(@RequestParam String firstName, @RequestParam String lastName, @RequestParam String phoneNumber, @RequestParam String email, @RequestParam String password) {

        // Check if user already exists to prevent duplicate errors
        if (userRepository.findByEmail(email).isPresent()) {
            return "redirect:/register?error=exists";
        }

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setEmail(email);

        // THIS IS THE MAGIC LINE: We hash the plain text password right before saving
        newUser.setPasswordHash(passwordEncoder.encode(password));

        newUser.setRole("ROLE_STUDENT");
        newUser.setLessonCredits(0);

        userRepository.save(newUser);

        // After saving, redirect them to the login page
        return "redirect:/login";
    }


    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {

        // 1. Look up the currently logged-in user
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. LOGIC CHECK: Does the user have a first name?
        String displayName = currentUser.getFirstName();

        // Check if the name is literally null, or if it's just an empty string ""
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = currentUser.getEmail(); // Fallback to email
        }

        // 3. Pass the safe display name and their credits into the HTML
        model.addAttribute("pageTitle", "Welcome, " + displayName);
        model.addAttribute("credits", currentUser.getLessonCredits());

        return "dashboard";
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

        // 1. Look up the user making the request
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. HARD SECURITY CHECK: Did they bypass the front-end?
        // If they have no credits, immediately bounce them out.
        if (currentUser.getLessonCredits() <= 0) {
            return "redirect:/dashboard?nocredit=true";
        }

        // 3. They have credits! Create the lesson request.
        Lesson newLesson = new Lesson();
        newLesson.setStudentEmail(currentUser.getEmail());
        newLesson.setLessonDate(lessonDate);
        newLesson.setLessonTime(lessonTime);
        lessonRepository.save(newLesson);

        // 4. THE IMPORTANT MATH: Deduct 1 credit from their account
        currentUser.setLessonCredits(currentUser.getLessonCredits() - 1);

        // 5. Save the updated user back to the database
        userRepository.save(currentUser);

        // 6. Redirect to the home page with a success flag
        return "redirect:/?lessonRequested=true";
    }

}