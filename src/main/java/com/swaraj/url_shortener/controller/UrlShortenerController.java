package com.swaraj.url_shortener.controller;

import com.swaraj.url_shortener.model.ShortUrl;
import com.swaraj.url_shortener.repository.ShortUrlRepository;
import com.swaraj.url_shortener.service.UrlShortenerService;
import com.swaraj.url_shortener.util.QrCodeGenerator;
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
    public ResponseEntity<?> redirect(@PathVariable String code) {

        Optional<ShortUrl> record = shortUrlRepository.findByShortCode(code);

        if (record.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ShortUrl url = record.get();

        // Check if expired → return 410 with custom message
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("⚠️ This short link has expired. Please generate a new one.");
        }

        // Update analytics
        url.setClickCount(url.getClickCount() + 1);
        url.setLastAccessedAt(LocalDateTime.now());
        shortUrlRepository.save(url);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getLongUrl()))
                .build();
    }

    @GetMapping("/preview/{code}")
    public ResponseEntity<String> preview(@PathVariable String code) {
        Optional<ShortUrl> record = shortUrlRepository.findByShortCode(code);

        if (record.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ShortUrl url = record.get();
        String shortUrl = "http://localhost:8080/" + code;
        String qrCodeBase64 = QrCodeGenerator.generateQRCodeImage(shortUrl);

        String html = "<html>" +
                "<head>" +
                "<title>URL Preview</title>" +
                "<style>" +
                "body{font-family:Arial;padding:30px;text-align:center;background:#FAFAFA;}" +
                "a{color:#007BFF;font-size:18px;text-decoration:none;}" +
                "img{margin-top:20px;border:3px solid #000;border-radius:10px;}" +
                ".box{background:#FFF;padding:25px;border-radius:12px;display:inline-block;box-shadow:0 4px 10px rgba(0,0,0,0.1);}" +
                "button{margin-top:20px;padding:10px 16px;font-size:16px;background:black;color:white;border:none;border-radius:6px;cursor:pointer;}" +
                "</style>" +
                "</head><body>" +
                "<div class='box'>" +
                "<h2>Short Link Ready</h2>" +
                "<p><strong>Short URL:</strong><br><a href='" + shortUrl + "'>" + shortUrl + "</a></p>" +
                "<p><strong>Original:</strong><br>" + url.getLongUrl() + "</p>" +
                "<img id='qrImage' src='data:image/png;base64," + qrCodeBase64 + "' width='220'/>" +
                "<br>" +
                "<button onclick=\"downloadQR()\">Download QR Code</button>" +
                "</div>" +

                "<script>" +
                "function downloadQR() {" +
                "  const img = document.getElementById('qrImage');" +
                "  const link = document.createElement('a');" +
                "  link.href = img.src;" +
                "  link.download = 'qr-code.png';" +
                "  link.click();" +
                "}" +
                "</script>" +
                "</body></html>";

        return ResponseEntity.ok().body(html);
    }



    @PostMapping("/shorten")
    public ResponseEntity<String> shorten(
            @RequestParam String longUrl,
            @RequestParam(required = false) String customCode,
            @RequestParam(required = false) Integer expiryMinutes
    ) {

        String code;

        if (customCode != null && !customCode.isBlank()) {

            // Validate: only letters + numbers allowed
            if (!customCode.matches("^[a-zA-Z0-9_-]{4,20}$")) {
                return ResponseEntity.badRequest()
                        .body("Custom code must be 4-20 characters and alphanumeric (allowed: - _)");
            }

            // Check if already used
            if (shortUrlRepository.findByShortCode(customCode).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("This custom code is already taken. Try another.");
            }

            code = customCode;
        } else {
            code = urlShortenerService.generateShortCode();
        }

        ShortUrl record = new ShortUrl();
        record.setLongUrl(longUrl);
        record.setShortCode(code);
        record.setCreatedAt(LocalDateTime.now());

        if (expiryMinutes != null) {
            record.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        }

        shortUrlRepository.save(record);
        String shortUrl = "http://localhost:8080/" + code;

        // Generate QR code for this short URL
        String qrCodeBase64 = QrCodeGenerator.generateQRCodeImage(shortUrl);

        String responseJson = "{ \"shortUrl\": \"" + shortUrl + "\", " +
                "\"qrCode\": \"data:image/png;base64," + qrCodeBase64 + "\" }";
        String previewUrl = "http://localhost:8080/preview/" + code;
        return ResponseEntity.ok(previewUrl);

    }


    @GetMapping("/stats/{code}")
    public ResponseEntity<ShortUrl> stats(@PathVariable String code) {
        return shortUrlRepository.findByShortCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
