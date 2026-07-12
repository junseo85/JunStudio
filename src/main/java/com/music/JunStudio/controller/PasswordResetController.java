package com.music.JunStudio.controller;

import com.music.JunStudio.model.PasswordResetToken;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.PasswordResetTokenRepository;
import com.music.JunStudio.repository.UserRepository;
import com.music.JunStudio.service.EmailService;
<<<<<<< HEAD
=======
import jakarta.servlet.http.HttpServletRequest;
>>>>>>> origin/master
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
<<<<<<< HEAD
@RequestMapping("/password-reset")
=======
>>>>>>> origin/master
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

<<<<<<< HEAD
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
=======
    // Step 1: Receive the email, generate token, send email
    @PostMapping("/password-reset/request")
    public String requestPasswordReset(@RequestParam("email") String email,
                                       HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Remove any existing token for this user to keep the table clean
            tokenRepository.findAll().stream()
                    .filter(t -> t.getUser().getId().equals(user.getId()))
                    .forEach(tokenRepository::delete);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(60));
            tokenRepository.save(resetToken);

            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();

            String resetLink;
            if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
                resetLink = scheme + "://" + serverName + contextPath + "/password-reset/confirm?token=" + token;
            } else {
                resetLink = scheme + "://" + serverName + ":" + serverPort + contextPath + "/password-reset/confirm?token=" + token;
            }

            String subject = "Jun Studio - Password Reset Request";
            String body = "Hi " + user.getFirstName() + ",\n\n"
                    + "We received a request to reset your Jun Studio account password.\n\n"
                    + "Click the link below to set a new password (valid for 60 minutes):\n"
                    + resetLink + "\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Jun Studio Team";
            emailService.sendSimpleEmail(user.getEmail(), subject, body);
        }

        // Always redirect with the same message to avoid user enumeration
        return "redirect:/login?resetRequested=true";
    }

    // Step 2a: Show the password-reset form (validate token)
    @GetMapping("/password-reset/confirm")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            return "redirect:/login?error=invalidResetToken";
        }
        model.addAttribute("token", token);
        return "password-reset";
    }

    // Step 2b: Handle the new password submission
    @PostMapping("/password-reset/confirm")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("newPassword") String newPassword,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            return "redirect:/login?error=invalidResetToken";
        }

        if (newPassword == null || newPassword.length() < 6) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "password-reset";
>>>>>>> origin/master
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match.");
<<<<<<< HEAD
            return "password-reset-confirm";
=======
            return "password-reset";
>>>>>>> origin/master
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

<<<<<<< HEAD
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        model.addAttribute("success", "Your password has been reset successfully. You can now log in with your new password.");
        return "password-reset-confirm";
=======
        // Invalidate the used token
        tokenRepository.delete(resetToken);

        return "redirect:/login?passwordResetSuccess=true";
>>>>>>> origin/master
    }
}
