package com.music.JunStudio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // THIS IS THE MAGIC LINE
class JunStudioApplicationTests {

	@Test
	void contextLoads() {
		// If this passes, it means Spring successfully built your app
		// using the fake H2 database!
	}
}