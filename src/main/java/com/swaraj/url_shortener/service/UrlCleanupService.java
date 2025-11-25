package com.swaraj.url_shortener.service;

import com.swaraj.url_shortener.repository.ShortUrlRepository;
import com.swaraj.url_shortener.model.ShortUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UrlCleanupService {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    // Runs every midnight 🕛
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanExpiredLinks() {
        List<ShortUrl> expiredUrls = shortUrlRepository.findAll().stream()
                .filter(url -> url.getExpiresAt() != null &&
                        url.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();

        if (!expiredUrls.isEmpty()) {
            shortUrlRepository.deleteAll(expiredUrls);
            System.out.println("🧹 Removed " + expiredUrls.size() + " expired URLs");
        }
    }
}
