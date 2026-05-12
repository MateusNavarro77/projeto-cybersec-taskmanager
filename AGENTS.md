# Agent Guide - projeto-cybersec-taskmanager

## Stack & Commands
- **Framework:** Spring Boot 4.0.6 (Java 21)
- **Database:** PostgreSQL (uses Testcontainers for testing/dev)
- **Primary Tool:** Maven (use `./mvnw` wrapper)
- **Build/Test:** `./mvnw clean install`
- **Run Single Test:** `./mvnw test -Dtest=ClassName`
- **Dev Mode:** `./mvnw spring-boot:test-run` (starts app with Testcontainers-managed DB)
- **API Docs:** Swagger/OpenAPI (springdoc-openapi) available at `/swagger-ui.html` when running.

## Critical Notes
- **Package Name:** Use `com.mateusnavarro77.projeto_cybersec_taskmanager` (note the underscore; the artifact ID uses hyphens).
- **Lombok:** Enabled. Ensure annotation processing is active in your context.
- **Testcontainers:** Local Docker is required for tests. If Docker is unavailable, tests will fail.

## Architecture
- **Entrypoint:** `ProjetoCybersecTaskmanagerApplication.java`
- **Configuration:** `src/main/resources/application.yaml`
- **Test Entrypoint:** `TestProjetoCybersecTaskmanagerApplication.java` (configured for dev-time Testcontainers).
