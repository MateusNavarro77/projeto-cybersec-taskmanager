package com.mateusnavarro77.projeto_cybersec_taskmanager.repository;

import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
