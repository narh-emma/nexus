package com.nexus.authservice.repo;

import com.nexus.authservice.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {

    boolean existsByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.userEmail = :email")
    void deleteByUserEmail(@Param("email") String email);
}