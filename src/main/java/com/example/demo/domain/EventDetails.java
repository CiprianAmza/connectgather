package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "event_details")
public class EventDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID propriu generat
    private Long id;

    // Acum, EventDetails deține cheia străină către Event
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", unique = true, nullable = false) // event_id va fi cheie străină unică și obligatorie
    private Event event;

    @Min(value = 1, message = "Capacitatea minimă este 1")
    @Max(value = 100000, message = "Capacitatea maximă este 100000")
    private Integer venueCapacity;

    @NotNull(message = "Opțiunea de catering este obligatorie")
    private Boolean cateringOption;

    @Size(max = 500, message = "Echipamentul necesar nu poate depăși 500 de caractere")
    private String requiredEquipment;

    public EventDetails() {
    }

    public EventDetails(Event event, Integer venueCapacity, Boolean cateringOption, String requiredEquipment) {
        this.event = event;
        this.venueCapacity = venueCapacity;
        this.cateringOption = cateringOption;
        this.requiredEquipment = requiredEquipment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Integer getVenueCapacity() {
        return venueCapacity;
    }

    public void setVenueCapacity(Integer venueCapacity) {
        this.venueCapacity = venueCapacity;
    }

    public Boolean getCateringOption() {
        return cateringOption;
    }

    public void setCateringOption(Boolean cateringOption) {
        this.cateringOption = cateringOption;
    }

    public String getRequiredEquipment() {
        return requiredEquipment;
    }

    public void setRequiredEquipment(String requiredEquipment) {
        this.requiredEquipment = requiredEquipment;
    }

    @Override
    public String toString() {
        return "EventDetails{" +
                "id=" + id +
                ", venueCapacity=" + venueCapacity +
                ", cateringOption=" + cateringOption +
                ", requiredEquipment='" + requiredEquipment + '\'' +
                '}';
    }
}
