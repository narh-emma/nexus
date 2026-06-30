package com.nexus.media.service;

import com.nexus.media.model.VideoVault;
import com.nexus.media.repository.VideoVaultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    @Autowired
    private VideoVaultRepository videoVaultRepository;  // ← Using VideoVaultRepository

    /**
     * Get all videos
     */
    public List<VideoVault> getAllVideos() {
        return videoVaultRepository.findAll();
    }

    /**
     * Get video by ID
     */
    public VideoVault getVideoById(UUID id) {
        return videoVaultRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
    }

    /**
     * Get videos by category
     */
    public List<VideoVault> getVideosByCategory(String category) {
        return videoVaultRepository.findByCategory(category);
    }

    /**
     * Upload a new video
     */
    public VideoVault uploadVideo(VideoVault video) {
        video.setCreatedAt(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());
        video.setViewCount(0L);
        return videoVaultRepository.save(video);
    }

    /**
     * Update video metadata
     */
    public VideoVault updateMetadata(UUID id, VideoVault videoDetails) {
        VideoVault existingVideo = getVideoById(id);
        
        if (videoDetails.getTitle() != null) {
            existingVideo.setTitle(videoDetails.getTitle());
        }
        if (videoDetails.getDescription() != null) {
            existingVideo.setDescription(videoDetails.getDescription());
        }
        if (videoDetails.getCategory() != null) {
            existingVideo.setCategory(videoDetails.getCategory());
        }
        if (videoDetails.getVideoUrl() != null) {
            existingVideo.setVideoUrl(videoDetails.getVideoUrl());
        }
        if (videoDetails.getThumbnailUrl() != null) {
            existingVideo.setThumbnailUrl(videoDetails.getThumbnailUrl());
        }
        if (videoDetails.getDuration() != null) {
            existingVideo.setDuration(videoDetails.getDuration());
        }
        if (videoDetails.getTags() != null && !videoDetails.getTags().isEmpty()) {
            existingVideo.setTags(videoDetails.getTags());
        }
        
        existingVideo.setOfflineAvailable(videoDetails.isOfflineAvailable());
        existingVideo.setUpdatedAt(LocalDateTime.now());
        
        return videoVaultRepository.save(existingVideo);
    }

    /**
     * Delete a video
     */
    public void deleteVideo(UUID id) {
        VideoVault video = getVideoById(id);
        videoVaultRepository.delete(video);
    }

    /**
     * Get all categories
     */
    public List<String> getCategories() {
        return videoVaultRepository.findAllCategories();
    }

    /**
     * Get offline bundle
     */
    public List<VideoVault> getOfflineBundle() {
        return videoVaultRepository.findByIsOfflineAvailableTrue();
    }

    /**
     * Search videos
     */
    public List<VideoVault> searchVideos(String searchTerm) {
        return videoVaultRepository.searchVideos(searchTerm);
    }

    /**
     * Increment view count
     */
    public void incrementViewCount(UUID id) {
        VideoVault video = getVideoById(id);
        video.setViewCount(video.getViewCount() + 1);
        videoVaultRepository.save(video);
    }

    /**
     * Get most viewed videos
     */
    public List<VideoVault> getMostViewedVideos() {
        return videoVaultRepository.findMostViewed();
    }
}