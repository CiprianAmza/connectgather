package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class ConnectGatherMonolithicApplication {

	public static void main(String[] args) {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String rawPassword = "123"; // <-- Aici pui parola pe care vrei s-o folosești
			String encodedPassword = encoder.encode(rawPassword);
			System.out.println("Parola codificată pentru '" + rawPassword + "': " + encodedPassword);

		SpringApplication.run(ConnectGatherMonolithicApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
