package com.nexus.translate.repository;

import com.nexus.translate.model.TranslationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TranslationLogRepository extends JpaRepository<TranslationLog, UUID> {
    
    List<TranslationLog> findByUserId(UUID userId);
    
    List<TranslationLog> findByUserIdAndSourceModality(UUID userId, String sourceModality);
    
    List<TranslationLog> findByUserIdAndTargetModality(UUID userId, String targetModality);
    
    void deleteByUserId(UUID userId);
}