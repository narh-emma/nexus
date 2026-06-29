package com.nexus.media.repository;

import com.nexus.media.model.VideoVault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface VideoVaultRepository extends JpaRepository<VideoVault, UUID> {
    
    @Query("SELECT DISTINCT v.category FROM VideoVault v")
    List<String> findDistinctCategories();

    List<VideoVault> findByIsOfflineCacheableTrue();
}