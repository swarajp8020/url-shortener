package com.swaraj.url_shortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
public class UrlShortenerApplication {
	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerApplication.class, args);
	}
}
