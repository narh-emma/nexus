package com.nexus.media.service;

import com.nexus.media.model.VideoVault;
import com.nexus.media.repository.VideoVaultRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    private final VideoVaultRepository repository;

    public MediaService(VideoVaultRepository repository) {
        this.repository = repository;
    }

    public List<VideoVault> getAllVideos() {
        return repository.findAll();
    }

    public VideoVault getVideoById(UUID id) {
        VideoVault video = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        // Simulating signed URL injection logic
        video.setStoragePath("https://s3.nexus-storage.local/signed-url/" + video.getStoragePath());
        return video;
    }

    public List<String> getCategories() {
        return repository.findDistinctCategories();
    }

    public List<VideoVault> getOfflineBundle() {
        return repository.findByIsOfflineCacheableTrue();
    }

    public VideoVault uploadVideo(VideoVault video) {
        return repository.save(video);
    }

    public VideoVault updateMetadata(UUID id, VideoVault updatedDetails) {
        VideoVault existing = getVideoById(id);
        if (updatedDetails.getTitle() != null) existing.setTitle(updatedDetails.getTitle());
        if (updatedDetails.getDescription() != null) existing.setDescription(updatedDetails.getDescription());
        if (updatedDetails.getCategory() != null) existing.setCategory(updatedDetails.getCategory());
        return repository.save(existing);
    }

    public void deleteVideo(UUID id) {
        repository.deleteById(id);
    }
}