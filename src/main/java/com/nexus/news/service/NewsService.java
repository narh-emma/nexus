package com.nexus.news.service;

import com.nexus.news.model.HealthTicker;
import com.nexus.news.repo.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    public List<HealthTicker> getLatestNews(String language, String category) {
        if (language != null && category != null) {
            return newsRepository.findTop50ByLanguageAndCategoryOrderByDatePostedDesc(language, category);
        } else if (language != null) {
            return newsRepository.findTop50ByLanguageOrderByDatePostedDesc(language);
        } else if (category != null) {
            return newsRepository.findTop50ByCategoryOrderByDatePostedDesc(category);
        }
        return newsRepository.findTop50ByOrderByDatePostedDesc();
    }

    public HealthTicker getNewsById(UUID id) {
        return newsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Headline not found"));
    }

    public List<HealthTicker> getAlerts() {
        return newsRepository.findByPriorityGreaterThanEqualOrderByDatePostedDesc(1);
    }

    public HealthTicker publishHeadline(HealthTicker ticker) {
        return newsRepository.save(ticker);
    }

    public void deleteHeadline(UUID id) {
        if (!newsRepository.existsById(id)) {
            throw new RuntimeException("Headline not found");
        }
        newsRepository.deleteById(id);
    }

    @Scheduled(fixedRate = 900000)
    public void fetchExternalNews() {
        seedSampleNewsIfEmpty();
    }

    public void seedSampleNewsIfEmpty() {
        if (newsRepository.count() > 0) return;

        newsRepository.save(new HealthTicker(
            "WHO Issues Global Health Advisory on Respiratory Infections",
            "The World Health Organization has issued a new advisory urging vigilance around seasonal respiratory infections.",
            "WHO", "https://www.who.int", "en", "advisory", 1
        ));
        newsRepository.save(new HealthTicker(
            "CDC Updates Guidelines for CPR Administration",
            "Updated CPR guidelines emphasize the importance of hands-only CPR for bystanders.",
            "CDC", "https://www.cdc.gov", "en", "first_aid", 0
        ));
        newsRepository.save(new HealthTicker(
            "New Study Links Air Quality to Increased Asthma Cases",
            "Researchers found a strong correlation between urban air pollution and rising asthma diagnoses.",
            "MedlinePlus", "https://medlineplus.gov", "en", "research", 0
        ));
        newsRepository.save(new HealthTicker(
            "CRITICAL: Meningitis Outbreak Reported in Three Regions",
            "Health authorities are responding to a meningitis outbreak. Vaccination urged immediately.",
            "GHS", "https://www.ghs.gov.gh", "en", "outbreak", 2
        ));
        newsRepository.save(new HealthTicker(
            "First Aid Tip: How to Treat Burns at Home",
            "Cool the burn under running water for 20 minutes. Do not apply ice or butter.",
            "Red Cross", "https://www.redcross.org", "en", "first_aid", 0
        ));
    }
}