package com.mateusnavarro77.projeto_cybersec_taskmanager;

import org.springframework.boot.SpringApplication;

public class TestProjetoCybersecTaskmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProjetoCybersecTaskmanagerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
