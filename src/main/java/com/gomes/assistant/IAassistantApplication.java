package com.gomes.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IAassistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(IAassistantApplication.class, args);
	}

}
