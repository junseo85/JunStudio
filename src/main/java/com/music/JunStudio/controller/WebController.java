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
    public String registerNewUser(@RequestParam String email, @RequestParam String password) {

        // Check if user already exists to prevent duplicate errors
        if (userRepository.findByEmail(email).isPresent()) {
            return "redirect:/register?error=exists";
        }

        User newUser = new User();
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
    public String showDashboard(Model model) {
        // The 'Model' allows us to pass Java variables to the HTML page
        model.addAttribute("pageTitle", "Student Practice Dashboard");
        model.addAttribute("credits", 5);

        // This tells Spring to look for an HTML file named "dashboard.html"
        return "dashboard";
    }

    @GetMapping("/schedule")
    public String showSchedulePage(){
        return "schedule";
    }

    @PostMapping("/schedule")
    public String requestLesson(
            @RequestParam LocalDate lessonDate, @RequestParam LocalTime lessonTime, Principal principal){
        Lesson newLesson = new Lesson();

        //principal.getName() automatically grabs the logged-in user's email
        newLesson.setStudentEmail(principal.getName());
        newLesson.setLessonDate(lessonDate);
        newLesson.setLessonTime(lessonTime);

        lessonRepository.save(newLesson);

        //Redirect to the home page with a success flag
        return "redirect:/?lessonRequested=true";
    }

}