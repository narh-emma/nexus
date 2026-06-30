package com.nexus.media.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "video_vault")
public class VideoVault {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "video_id", updatable = false, nullable = false)
    private UUID videoId;

    @NotNull
    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(length = 50, nullable = false)
    private String category;

    @NotNull
    @Column(name = "storage_path", length = 500, nullable = false)
    private String storagePath;

    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;

    @NotNull
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "sign_track_path", length = 500)
    private String signTrackPath;

    @Column(length = 10)
    private String language = "en";

    @Column(name = "is_offline_cacheable")
    private Boolean isOfflineCacheable = true;

    @CreationTimestamp
@Column(name = "uploaded_at", insertable = true, updatable = true)    private OffsetDateTime uploadedAt;

    public Object getTitle() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTitle'");
    }

    public String getStoragePath() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStoragePath'");
    }

    public void setStoragePath(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStoragePath'");
    }

    public void setTitle(Object title2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTitle'");
    }

    public Object getDescription() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDescription'");
    }

    public void setDescription(Object description2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDescription'");
    }

    public Object getCategory() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCategory'");
    }

    public void setCategory(Object category2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCategory'");
    }

    // Getters, Setters, Constructors
}