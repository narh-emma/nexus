package com.nexus.translate.repository;

import com.nexus.translate.model.SignDictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignDictionaryRepository extends JpaRepository<SignDictionary, UUID> {
    
    List<SignDictionary> findByLanguage(String language);
    
    List<SignDictionary> findByWordContainingIgnoreCase(String word);
    
    boolean existsByWordAndLanguage(String word, String language);
}