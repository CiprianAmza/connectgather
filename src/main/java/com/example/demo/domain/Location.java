package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele locației nu poate fi gol")
    @Size(min = 3, max = 100, message = "Numele locației trebuie să aibă între 3 și 100 de caractere")
    @Column(unique = true)
    private String name;

    @NotBlank(message = "Adresa nu poate fi goală")
    @Size(min = 5, max = 255, message = "Adresa trebuie să aibă între 5 și 255 de caractere")
    private String address;

    @Size(max = 255, message = "Descrierea poate avea maxim 255 de caractere")
    private String description;

    public Location() {
    }

    public Location(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
