package com.nexus.media.controller;

import com.nexus.media.model.VideoVault;
import com.nexus.media.service.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/first-aid")
    public ResponseEntity<List<VideoVault>> getAllFirstAidVideos() {
        return ResponseEntity.ok(mediaService.getAllVideos());
    }

    @GetMapping("/first-aid/{id}")
    public ResponseEntity<VideoVault> getFirstAidVideoById(@PathVariable UUID id) {
        return ResponseEntity.ok(mediaService.getVideoById(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(mediaService.getCategories());
    }

    @GetMapping("/offline-bundle")
    public ResponseEntity<List<VideoVault>> getOfflineBundle() {
        return ResponseEntity.ok(mediaService.getOfflineBundle());
    }

    @PostMapping("/first-aid")
    public ResponseEntity<VideoVault> uploadVideo(@RequestBody VideoVault video) {
        return ResponseEntity.ok(mediaService.uploadVideo(video));
    }

    @PatchMapping("/first-aid/{id}")
    public ResponseEntity<VideoVault> updateMetadata(@PathVariable UUID id, @RequestBody VideoVault video) {
        return ResponseEntity.ok(mediaService.updateMetadata(id, video));
    }

    @DeleteMapping("/first-aid/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID id) {
        mediaService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}