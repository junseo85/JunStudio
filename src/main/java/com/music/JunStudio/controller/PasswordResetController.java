package com.music.JunStudio.controller;

import com.music.JunStudio.model.PasswordResetToken;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.PasswordResetTokenRepository;
import com.music.JunStudio.repository.UserRepository;
import com.music.JunStudio.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/password-reset")
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Show the "enter your email" form
    @GetMapping("/request")
    public String showRequestForm() {
        return "password-reset-request";
    }

    // Process the email submission and send the reset link
    @PostMapping("/request")
    public String processRequest(@RequestParam String email, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        // Always show the same message to avoid revealing whether an account exists
        model.addAttribute("message", "If an account with that email exists, a password reset link has been sent. Please check your inbox and spam folder.");

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(1));
            tokenRepository.save(resetToken);

            String resetLink = "http://localhost:8080/password-reset/confirm?token=" + token;
            emailService.sendSimpleEmail(
                    email,
                    "Jun Studio - Password Reset Request",
                    "Hello " + user.getFirstName() + ",\n\n"
                    + "You requested a password reset for your Jun Studio account.\n\n"
                    + "Click the link below to reset your password (valid for 1 hour):\n"
                    + resetLink + "\n\n"
                    + "If you did not request this, you can ignore this email.\n\n"
                    + "Jun Studio Team"
            );
        }

        return "password-reset-request";
    }

    // Show the "enter new password" form (reached via email link)
    @GetMapping("/confirm")
    public String showConfirmForm(@RequestParam String token, Model model) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isUsed() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "This password reset link is invalid or has expired. Please request a new one.");
            return "password-reset-confirm";
        }
        model.addAttribute("token", token);
        return "password-reset-confirm";
    }

    // Process the new password submission
    @PostMapping("/confirm")
    public String processConfirm(@RequestParam String token,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  Model model) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isUsed() || tokenOpt.get().isExpired()) {
            model.addAttribute("error", "This password reset link is invalid or has expired. Please request a new one.");
            return "password-reset-confirm";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "password-reset-confirm";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match.");
            return "password-reset-confirm";
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        model.addAttribute("success", "Your password has been reset successfully. You can now log in with your new password.");
        return "password-reset-confirm";
    }
}
