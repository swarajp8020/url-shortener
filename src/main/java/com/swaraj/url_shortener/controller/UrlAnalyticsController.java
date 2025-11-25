package com.swaraj.url_shortener.controller;

import com.swaraj.url_shortener.model.ShortUrl;
import com.swaraj.url_shortener.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
public class UrlAnalyticsController {

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @GetMapping
    public ResponseEntity<List<ShortUrl>> getAllStats() {
        return ResponseEntity.ok(shortUrlRepository.findAll());
    }

    @GetMapping("/top")
    public ResponseEntity<List<ShortUrl>> getTopLinks() {
        return ResponseEntity.ok(shortUrlRepository.findAll(
                Sort.by(Sort.Direction.DESC, "clickCount")
        ));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ShortUrl>> getPaginatedStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<ShortUrl> paged = shortUrlRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        );
        return ResponseEntity.ok(paged);
    }
}
