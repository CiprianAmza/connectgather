package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ConnectGatherMonolithicApplication {

	public static void main(String[] args) {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String rawPassword = "123"; // <-- Aici pui parola pe care vrei s-o folosești
			String encodedPassword = encoder.encode(rawPassword);
			System.out.println("Parola codificată pentru '" + rawPassword + "': " + encodedPassword);

		SpringApplication.run(ConnectGatherMonolithicApplication.class, args);
	}

}
