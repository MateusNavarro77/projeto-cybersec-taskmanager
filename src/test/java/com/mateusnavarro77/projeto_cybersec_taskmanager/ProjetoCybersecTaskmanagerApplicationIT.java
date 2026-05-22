package com.mateusnavarro77.projeto_cybersec_taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("integrationtest")
class ProjetoCybersecTaskmanagerApplicationIT {

	@Test
	void contextLoads() {
	}

}
