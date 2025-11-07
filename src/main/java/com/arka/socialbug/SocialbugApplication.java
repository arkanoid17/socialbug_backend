package com.arka.socialbug;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.arka.socialbug")
@EntityScan(basePackages = "com.arka.socialbug.model")
@EnableJpaRepositories(basePackages = "com.arka.socialbug.repository")
public class SocialbugApplication {

	public static void main(String[] args) {
		SpringApplication.run(SocialbugApplication.class, args);
	}

}
