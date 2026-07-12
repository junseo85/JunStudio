package com.music.JunStudio.controller;

import com.music.JunStudio.model.PasswordResetToken;
import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.PasswordResetTokenRepository;
import com.music.JunStudio.repository.UserRepository;
import com.music.JunStudio.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Passwords do not match.");
            return "password-reset";
        }

        PasswordResetToken resetToken = tokenOpt.get();
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate the used token
        tokenRepository.delete(resetToken);

        return "redirect:/login?passwordResetSuccess=true";
    }
}
