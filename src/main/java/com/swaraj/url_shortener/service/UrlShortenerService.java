package com.swaraj.url_shortener.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class UrlShortenerService {

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 7;
    private final Random random = new Random();

    public String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
