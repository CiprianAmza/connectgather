package com.example.demo.client;

import com.example.demo.web.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

import java.util.Map; // IMPORTANT: Importă Map

@Component
public class AuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestTemplate restTemplate;
    private static final String AUTH_SERVICE_NAME = "AUTH-SERVICE"; // Numele serviciului înregistrat în Eureka

    @Autowired
    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> authenticate(LoginRequest loginRequest) {
        String url = String.format("http://%s/api/auth/login", AUTH_SERVICE_NAME);
        logger.info("Sending authentication request to AuthService at URL: {}", url);
        try {
            // CORECTAT: Așteptăm un Map.class ca răspuns, nu String.class
            return restTemplate.postForEntity(url, loginRequest, Map.class); // Schimbat String.class la Map.class
        } catch (HttpClientErrorException e) {
            // Aceste erori sunt de obicei 4xx, cum ar fi 401 Unauthorized
            logger.warn("Authentication failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Returnăm ResponseEntity cu același status și body pentru a fi procesat de LoginController
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error during authentication request to AuthService: {}", e.getMessage(), e);
            // Pentru alte tipuri de erori (conexiune, etc.), returnăm un mesaj generic
            return ResponseEntity.internalServerError().body("Eroare la conectarea cu serviciul de autentificare: " + e.getMessage());
        }
    }
}
