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
import java.time.Duration;
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

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("⚠️ This link has expired. Create a new one.");
        }

        // Analytics update
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
        if (record.isEmpty()) return ResponseEntity.notFound().build();

        ShortUrl url = record.get();
        String shortUrl = "http://localhost:8080/" + code;
        String qrCodeBase64 = QrCodeGenerator.generateQRCodeImage(shortUrl);

        LocalDateTime now = LocalDateTime.now();
        long secondsLeft = (url.getExpiresAt() == null)
                ? -1
                : Duration.between(now, url.getExpiresAt()).getSeconds();

        String timerScript = "";

        if (secondsLeft > 0) {
            timerScript =
                    "let secondsLeft = " + secondsLeft + ";" +
                            "const timer = setInterval(() => {" +
                            "if(secondsLeft <= 0) {" +
                            "document.getElementById('timer').innerHTML = '⛔ Expired';" +
                            "document.getElementById('bar').style.width = '0%';" +
                            "clearInterval(timer);" +
                            "return;" +
                            "}" +
                            "secondsLeft--;" +
                            "document.getElementById('timer').innerHTML = 'Expires in ' + secondsLeft + 's';" +

                            // Progress bar shrink
                            "let percent = (secondsLeft / " + secondsLeft + ") * 100;" +
                            "document.getElementById('bar').style.width = percent + '%';" +

                            // Color changes
                            "if(percent < 60) document.getElementById('bar').style.background='yellow';" +
                            "if(percent < 30) document.getElementById('bar').style.background='red';" +

                            "}, 1000);";
        }

        String expiryText = (url.getExpiresAt() == null)
                ? "Never Expires"
                : "Expires at: " + url.getExpiresAt();

        String html = "<html><head><title>Preview</title>" +
                "<style>" +
                "body{font-family:Arial;text-align:center;padding:25px;background:#F4F4F4}" +
                ".card{background:white;padding:20px;border-radius:12px;display:inline-block;" +
                "box-shadow:0 4px 12px rgba(0,0,0,0.15);width:350px;}" +
                ".bar-container{width:100%;height:12px;background:#ddd;border-radius:6px;margin-top:10px;}" +
                ".bar{height:100%;width:100%;background:green;border-radius:6px;transition:width 1s;}" +
                "button{margin-top:12px;padding:10px 20px;font-size:15px;background:black;color:white;border:none;border-radius:6px;cursor:pointer;}" +
                "a{text-decoration:none;color:#0066FF;font-size:18px;}" +
                "</style></head><body>" +

                "<div class='card'>" +
                "<h2>Your Short Link</h2>" +
                "<a href='" + shortUrl + "'>" + shortUrl + "</a>" +

                "<p><strong>Clicks:</strong> " + url.getClickCount() + "</p>" +
                "<p><strong>Expiry:</strong> <span id='timer'>" + expiryText + "</span></p>" +

                "<div class='bar-container'><div id='bar' class='bar'></div></div>" +

                "<img id='qrImage' src='data:image/png;base64," + qrCodeBase64 + "' width='220'>" +
                "<br><button onclick=\"downloadQR()\">Download QR</button>" +
                "</div>" +

                "<script>" + timerScript +
                "function downloadQR(){const link=document.createElement('a');link.href=document.getElementById('qrImage').src;" +
                "link.download='qr-code.png';link.click();}" +
                "</script></body></html>";

        return ResponseEntity.ok(html);
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> shorten(
            @RequestParam String longUrl,
            @RequestParam(required = false) String customCode,
            @RequestParam(required = false) Integer expiryMinutes
    ) {

        String code = (customCode != null && !customCode.isBlank())
                ? customCode
                : urlShortenerService.generateShortCode();

        ShortUrl url = new ShortUrl();
        url.setLongUrl(longUrl);
        url.setShortCode(code);
        url.setCreatedAt(LocalDateTime.now());

        if (expiryMinutes != null) {
            url.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        }

        shortUrlRepository.save(url);
        return ResponseEntity.ok("http://localhost:8080/preview/" + code);
    }
}
