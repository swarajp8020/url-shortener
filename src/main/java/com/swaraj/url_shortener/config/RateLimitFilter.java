package com.swaraj.url_shortener.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private static final int LIMIT = 10; // max requests
    private static final long TIME_WINDOW = 60_000; // 1 min

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (req.getRequestURI().startsWith("/shorten")) {
            String ip = req.getRemoteAddr();
            long now = Instant.now().toEpochMilli();

            requestCounts.putIfAbsent(ip, new RequestInfo(0, now));
            RequestInfo info = requestCounts.get(ip);

            long elapsed = now - info.startTime;

            if (elapsed > TIME_WINDOW) {
                info.count = 0;
                info.startTime = now;
            }

            if (info.count >= LIMIT) {
                res.setStatus(429);
                res.getWriter().write("⏳ Too many requests. Try again in a minute.");
                return;
            }

            info.count++;
        }

        chain.doFilter(request, response);
    }

    private static class RequestInfo {
        int count;
        long startTime;

        RequestInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}
