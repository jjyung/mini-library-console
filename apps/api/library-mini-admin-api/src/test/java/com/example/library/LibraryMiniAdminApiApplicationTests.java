package com.example.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:sqlite:target/library-mini-admin-api-test-context.db",
    "spring.datasource.driver-class-name=org.sqlite.JDBC"
})
class LibraryMiniAdminApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
