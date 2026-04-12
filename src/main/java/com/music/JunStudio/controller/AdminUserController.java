package com.music.JunStudio.controller;

import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Show all users
    @GetMapping
    public String viewAllUsers(Model model, Principal principal) {
        // Security check
        User admin = userRepository.findByEmail(principal.getName()).orElseThrow();
        if (!"ROLE_ADMIN".equals(admin.getRole())) {
            return "redirect:/dashboard?error=unauthorized";
        }

        //THE FIX: Tell Thymeleaf this is an Admin!
        model.addAttribute("isAdmin", true);

        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin-users";
    }

    // 2. Create a new user manually
    @PostMapping("/create")
    public String createUser(@ModelAttribute User user) {
        // Encode a default password (e.g., "password123")
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);
        return "redirect:/admin/users?success=User created successfully";
    }

    // 3. Edit an existing user
    @PostMapping("/{id}/edit")
    public String editUser(@PathVariable Long id, @ModelAttribute User updatedUser) {
        User existingUser = userRepository.findById(id).orElseThrow();

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setLessonCredits(updatedUser.getLessonCredits());

        userRepository.save(existingUser);
        return "redirect:/admin/users?success=User updated successfully";
    }

    // 4. Delete a user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users?success=User deleted successfully";
    }
}