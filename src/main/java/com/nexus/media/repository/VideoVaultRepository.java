package com.nexus.media.repository;

import com.nexus.media.model.VideoVault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoVaultRepository extends JpaRepository<VideoVault, UUID> {

    List<VideoVault> findByCategory(String category);

    List<VideoVault> findByIsOfflineAvailableTrue();

    @Query("SELECT DISTINCT v.category FROM VideoVault v")
    List<String> findAllCategories();

    @Query("SELECT v FROM VideoVault v WHERE LOWER(v.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<VideoVault> searchVideos(@Param("search") String search);

    @Query("SELECT v FROM VideoVault v ORDER BY v.viewCount DESC")
    List<VideoVault> findMostViewed();
}