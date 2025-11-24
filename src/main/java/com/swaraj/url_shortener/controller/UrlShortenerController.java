package com.swaraj.url_shortener.controller;

import com.swaraj.url_shortener.model.ShortUrl;
import com.swaraj.url_shortener.repository.ShortUrlRepository;
import com.swaraj.url_shortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {

        Optional<ShortUrl> record = shortUrlRepository.findByShortCode(code);

        if (record.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(record.get().getLongUrl()))
                .build();
    }


    @PostMapping("/shorten")
    public String shorten(@RequestParam String longUrl) {
        String code = urlShortenerService.generateShortCode();

        ShortUrl record = new ShortUrl();
        record.setLongUrl(longUrl);
        record.setShortCode(code);
        record.setCreatedAt(LocalDateTime.now());

        shortUrlRepository.save(record);

        return "http://localhost:8080/" + code;
    }
}
