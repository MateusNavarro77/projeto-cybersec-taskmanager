# Projeto Cybersec Taskmanager - Instructional Context

This document provides essential context and instructions for AI agents working on the **Projeto Cybersec Taskmanager**.

## 1. Project Overview
A secure task management application built with Spring Boot, focusing on cybersecurity best practices such as JWT authentication, password hashing, and secure data handling.

### Core Technologies
- **Language:** Java 21
- **Framework:** Spring Boot 4.0.6
- **Database:** PostgreSQL (with Flyway for migrations)
- **Security:** Spring Security + JWT (`java-jwt`) + BCrypt
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Utilities:** Lombok
- **Testing:** JUnit 5, Testcontainers (PostgreSQL)

### Architecture
- **Layered Structure:** `controller` -> `service` -> `repository` -> `entity`.
- **DTOs:** Used for API requests and responses (`dto` package), implemented as Java Records.
- **Security Scoping:** All resource-owning entities (Checklists, Tasks) are strictly scoped to the authenticated user in the service layer.
- **UUIDs:** All primary keys are UUIDs for improved security and decentralization.
- **Migrations:** Flyway manages schema changes in `src/main/resources/db/migration`.

## 2. Building and Running

### Prerequisites
- **Java 21** installed.
- **Docker** (required for Testcontainers-based dev mode and tests).

### Profiles
The application uses Spring Boot profiles to manage different environments:
- **dev (Default):** Local development using a local PostgreSQL database.
- **unittest:** Fast tests using an in-memory H2 database. Docker not required.
- **integrationtest:** Integration tests using Testcontainers (PostgreSQL 17). Docker is required.
- **prod:** Production environment using environment variables for sensitive configuration.

### Key Commands
- **Build and Install:** `./mvnw clean install`
- **Run Application (Dev):** `./mvnw spring-boot:run` (uses `dev` profile by default)
- **Run Application (Prod):** `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`
- **Run Unit Tests:** `./mvnw test -Dspring.profiles.active=unittest`
- **Run Integration Tests:** `./mvnw test -Dspring.profiles.active=integrationtest`

### API Documentation
When the app is running, Swagger UI is available at:
- `http://localhost:8080/swagger-ui.html`

## 3. Development Conventions

### Coding Style
- **Package Name:** `com.mateusnavarro77.projeto_cybersec_taskmanager` (note the underscore in the package vs. hyphen in artifact ID).
- **Lombok:** Use `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder` to reduce boilerplate.
- **REST standards:** Use appropriate HTTP methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) and status codes.
- **Security First:** Always verify that new endpoints are properly secured in `SecurityConfig.java`. Use `AuthenticatedUser` context when needed.

### Testing Practices
- **Integration Tests:** Prefer integration tests using Testcontainers to ensure database compatibility.
- **Unit Tests:** Use Mockito for service-level unit tests.
- **Naming:** Follow the `MethodName_StateUnderTest_ExpectedBehavior` convention (or similar clear naming).

### Contribution Guidelines
1.  **Migrations:** Never modify existing Flyway migration files. Create new ones for schema changes.
2.  **Validation:** Use `jakarta.validation` annotations (e.g., `@NotBlank`, `@Size`) in DTOs.
3.  **Exception Handling:** Centralized exception handling should be implemented (check for `@RestControllerAdvice` if it exists, or create one if missing).

## 4. Key Files
- `pom.xml`: Project dependencies and build configuration.
- `src/main/resources/application.yaml`: Main configuration (DB, JWT, etc.).
- `src/main/java/.../security/SecurityConfig.java`: Security filter chain and endpoint permissions.
- `src/main/resources/db/migration/V1__init_schema.sql`: Initial database schema.
- `AGENTS.md`: Technical summary for quick reference.
