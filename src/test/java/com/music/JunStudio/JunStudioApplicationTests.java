package com.music.JunStudio;

import com.music.JunStudio.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test") // Run the context checks against the isolated test profile.
class JunStudioApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
		// If this passes, it means Spring successfully built your app
		// using the fake H2 database!
	}

	@Test
	void noDefaultAdminIsSeeded() {
		assertTrue(userRepository.findAll().isEmpty());
	}
}