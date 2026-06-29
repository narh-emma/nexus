package com.nexus.translate.repository;

import com.nexus.translate.model.SignDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SignDictionaryRepository extends JpaRepository<SignDictionary, UUID> {
    Optional<SignDictionary> findByGlossAndDialect(String gloss, String dialect);
}