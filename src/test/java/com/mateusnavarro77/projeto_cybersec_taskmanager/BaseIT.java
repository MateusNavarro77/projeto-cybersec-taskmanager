package com.mateusnavarro77.projeto_cybersec_taskmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.ChecklistRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.TaskRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.UserRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.security.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integrationtest")
public abstract class BaseIT {

    @Autowired
    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ChecklistRepository checklistRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected TokenService tokenService;

    @AfterEach
    void tearDownBase() {
        taskRepository.deleteAll();
        checklistRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createTestUser(String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash("hashedPassword") // Not actually used for JWT auth in tests
                .role("USER")
                .build();
        return userRepository.save(user);
    }

    protected String getAuthHeader(User user) {
        String token = tokenService.generateToken(user);
        return "Bearer " + token;
    }
}
