package com.nexus.news.repo;

import com.nexus.news.model.HealthTicker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NewsRepository extends JpaRepository<HealthTicker, UUID> {

    List<HealthTicker> findTop50ByOrderByDatePostedDesc();

    List<HealthTicker> findTop50ByLanguageOrderByDatePostedDesc(String language);

    List<HealthTicker> findTop50ByCategoryOrderByDatePostedDesc(String category);

    List<HealthTicker> findTop50ByLanguageAndCategoryOrderByDatePostedDesc(String language, String category);

    List<HealthTicker> findByPriorityGreaterThanEqualOrderByDatePostedDesc(Integer priority);

    boolean existsByHeadlineAndSource(String headline, String source);
}