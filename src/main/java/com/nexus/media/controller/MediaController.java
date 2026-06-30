package com.nexus.media.controller;

import com.nexus.media.model.VideoVault;
import com.nexus.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Media", description = "Media service for first-aid videos and resources")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @GetMapping("/first-aid")
    @Operation(summary = "Get all first-aid videos")
    public ResponseEntity<?> getAllFirstAidVideos() {
        try {
            List<VideoVault> videos = mediaService.getAllVideos();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", videos
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/first-aid/{id}")
    @Operation(summary = "Get first-aid video by ID")
    public ResponseEntity<?> getFirstAidVideoById(@PathVariable UUID id) {
        try {
            VideoVault video = mediaService.getVideoById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", video
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<?> getCategories() {
        try {
            List<String> categories = mediaService.getCategories();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categories
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/offline-bundle")
    @Operation(summary = "Get offline bundle")
    public ResponseEntity<?> getOfflineBundle() {
        try {
            List<VideoVault> videos = mediaService.getOfflineBundle();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", videos
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search videos")
    public ResponseEntity<?> searchVideos(@RequestParam String q) {
        try {
            List<VideoVault> videos = mediaService.searchVideos(q);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", videos
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/most-viewed")
    @Operation(summary = "Get most viewed videos")
    public ResponseEntity<?> getMostViewed() {
        try {
            List<VideoVault> videos = mediaService.getMostViewedVideos();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", videos
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/first-aid")
    @Operation(summary = "Upload a first-aid video (Admin only)")
    public ResponseEntity<?> uploadVideo(@RequestBody VideoVault video) {
        try {
            VideoVault savedVideo = mediaService.uploadVideo(video);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "success", true,
                    "message", "Video uploaded successfully",
                    "data", savedVideo
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PatchMapping("/first-aid/{id}")
    @Operation(summary = "Update video metadata (Admin only)")
    public ResponseEntity<?> updateMetadata(@PathVariable UUID id, @RequestBody VideoVault video) {
        try {
            VideoVault updatedVideo = mediaService.updateMetadata(id, video);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Video updated successfully",
                "data", updatedVideo
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/first-aid/{id}")
    @Operation(summary = "Delete a video (Admin only)")
    public ResponseEntity<?> deleteVideo(@PathVariable UUID id) {
        try {
            mediaService.deleteVideo(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Video deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/first-aid/{id}/view")
    @Operation(summary = "Increment view count")
    public ResponseEntity<?> incrementViewCount(@PathVariable UUID id) {
        try {
            mediaService.incrementViewCount(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "View count incremented"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}