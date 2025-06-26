package com.example.demo.controller;

import com.example.demo.client.AuthServiceClient;
import com.example.demo.web.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final AuthServiceClient authServiceClient;

    @Autowired
    public LoginController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        logger.info("Accessing login page. Error: {}, Logout: {}", error, logout);
        if (error != null) {
            model.addAttribute("error", "Nume de utilizator sau parolă incorecte.");
        }
        if (logout != null) {
            model.addAttribute("message", "Ai fost delogat cu succes.");
        }
        model.addAttribute("loginRequest", new LoginRequest()); // Asigură că obiectul loginRequest este în model
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               Model model) {
        logger.info("Processing login attempt for user: {}", loginRequest.getUsername());

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors for login attempt by {}: {}", loginRequest.getUsername(), bindingResult.getAllErrors());
            // Reafisează formularul cu erori și păstrează datele introduse
            model.addAttribute("loginRequest", loginRequest);
            return "login";
        }

        logger.debug("Attempting to call AuthService for user {}", loginRequest.getUsername());
        ResponseEntity<?> authResponse = authServiceClient.authenticate(loginRequest);
        logger.debug("Received response from AuthService for user {}: Status {}", loginRequest.getUsername(), authResponse.getStatusCode());


        if (authResponse.getStatusCode().is2xxSuccessful()) {
            logger.info("Authentication successful for user: {}", loginRequest.getUsername());
            Map<String, Object> responseBody = (Map<String, Object>) authResponse.getBody();
            String username = (String) responseBody.get("username");
            List<String> roles = (List<String>) responseBody.get("roles");

            // Construiește un obiect Authentication pentru Spring Security local
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Parola este null după autentificarea externă, deoarece nu o stocăm local
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // Setează autentificarea în SecurityContextHolder
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Creează o sesiune HTTP pentru contextul de securitate
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            logger.info("User {} successfully authenticated and security context updated.", username);
            return "redirect:/events"; // Redirecționează către pagina de evenimente
        } else {
            String errorMessage = "Eroare la autentificare. Credențiale invalide.";
            if (authResponse.getBody() instanceof String) {
                errorMessage = (String) authResponse.getBody();
            } else if (authResponse.getBody() instanceof Map) {
                // AuthService ar putea returna un JSON cu un câmp 'message'
                Map<String, String> errorMap = (Map<String, String>) authResponse.getBody();
                if (errorMap.containsKey("message")) {
                    errorMessage = errorMap.get("message");
                }
            }
            logger.warn("Authentication failed for user {}: {}", loginRequest.getUsername(), errorMessage);
            model.addAttribute("error", errorMessage);
            model.addAttribute("loginRequest", loginRequest); // Re-populează formularul cu datele introduse
            return "login"; // Reafisează formularul de login cu eroare
        }
    }
}
