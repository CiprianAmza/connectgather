package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage() {

        return "login"; // Aceasta se va mapa la src/main/resources/templates/login.html
    }

    // Poți adăuga și un controller pentru pagina principală dacă nu ai deja unul
    @GetMapping("/")
    public String showHomePage() {
        return "home"; // Asigură-te că ai un fișier home.html în templates
    }
}