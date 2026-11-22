package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"app.security.user.username=user",
		"app.security.user.password=userpass",
		"app.security.admin.username=admin",
		"app.security.admin.password=adminpass"
})
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
