package com.music.JunStudio.controller;

import com.music.JunStudio.model.User;
import com.music.JunStudio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AdminUserControllerTests {
    private final UserRepository repository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AdminUserController controller = new AdminUserController();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "userRepository", repository);
        ReflectionTestUtils.setField(controller, "passwordEncoder", encoder);
    }

    @Test
    void rejectsMissingShortAndMismatchedPasswords() {
        assertTrue(controller.createUser(new User(), null, null).contains("error"));
        assertTrue(controller.createUser(new User(), "short", "short").contains("error"));
        assertTrue(controller.createUser(new User(), "long-enough-password", "different").contains("error"));
        verifyNoInteractions(repository);
    }

    @Test
    void storesAnEncodedSuppliedPassword() {
        User user = new User();
        String password = "a-unique-test-password";
        assertTrue(controller.createUser(user, password, password).contains("success"));
        assertTrue(encoder.matches(password, user.getPasswordHash()));
        verify(repository).save(user);
    }
}
