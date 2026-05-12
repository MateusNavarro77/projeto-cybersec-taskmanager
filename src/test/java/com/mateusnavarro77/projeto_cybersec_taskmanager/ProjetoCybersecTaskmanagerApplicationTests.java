package com.mateusnavarro77.projeto_cybersec_taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProjetoCybersecTaskmanagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
