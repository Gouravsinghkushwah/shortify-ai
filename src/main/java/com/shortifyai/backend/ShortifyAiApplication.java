package com.shortifyai.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShortifyAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortifyAiApplication.class, args);
	}

}
