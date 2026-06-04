package com.nexus.authservice.repo;

import com.nexus.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByIndexNumber(String indexNumber);
    
    boolean existsByEmail(String email);
}