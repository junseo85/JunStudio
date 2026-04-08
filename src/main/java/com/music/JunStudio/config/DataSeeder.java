package com.music.JunStudio.config;

import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if our test user already exists
        if (userRepository.findByEmail("admin@music.com").isEmpty()) {

            User admin = new User();
            admin.setEmail("admin@music.com");

            // Let Spring handle the messy hashing!
            admin.setPasswordHash(passwordEncoder.encode("password123"));

            admin.setRole("ROLE_ADMIN");
            admin.setLessonCredits(999);

            userRepository.save(admin);
            System.out.println("✅ Admin user seeded successfully!");
        }
    }
}