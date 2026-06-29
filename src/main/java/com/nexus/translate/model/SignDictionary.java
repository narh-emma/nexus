package com.nexus.translate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sign_dictionary")
public class SignDictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sign_id", updatable = false, nullable = false)
    private UUID signId;

    @NotNull
    @Column(length = 100, unique = true, nullable = false)
    private String gloss;

    @Column(length = 20)
    private String dialect = "ASL";

    @NotNull
    @Column(name = "animation_key", length = 120, nullable = false)
    private String animationKey;

    @Column(name = "medical_tag", length = 50)
    private String medicalTag;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    // Getters, Setters, Constructors
}