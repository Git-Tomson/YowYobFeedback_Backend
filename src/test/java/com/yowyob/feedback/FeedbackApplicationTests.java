package com.yowyob.feedback;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.elasticsearch.repositories.enabled=false",
        "spring.elasticsearch.enabled=false"
})
@Disabled("En attente de la configuration complète des services Docker")
class FeedbackApplicationTests {

	@Test
	void contextLoads() {
	}

}
