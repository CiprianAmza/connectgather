package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "organizers")
public class Organizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele nu poate fi gol")
    @Size(min = 3, max = 100, message = "Numele trebuie să aibă între 3 și 100 de caractere")
    private String name;

    @NotBlank(message = "Email-ul nu poate fi gol")
    @Email(message = "Format email invalid")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Numărul de telefon nu poate fi gol")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Format telefon invalid")
    private String phone;

    public Organizer() {
    }

    public Organizer(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Organizer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
