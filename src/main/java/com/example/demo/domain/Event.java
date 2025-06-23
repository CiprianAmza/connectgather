package com.example.demo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet; // Sau ArrayList
import java.util.Set; // Sau List

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Start date and time cannot be null")
    @FutureOrPresent(message = "Start date must be in the future or present")
    private LocalDateTime startTime;

    @NotNull(message = "End date and time cannot be null")
    @FutureOrPresent(message = "End date must be in the future or present")
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
//    @NotNull(message = "Un eveniment trebuie să aibă o locație!")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
//    @NotNull(message = "Un eveniment trebuie să aibă un organizator!")
    private Participant organizer;

    // NOU: Relația One-to-Many către EventParticipantRelation
    // 'mappedBy' indică câmpul din EventParticipantRelation care mapează această relație
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EventParticipantRelation> eventParticipants = new HashSet<>();


    // Constructor implicit necesar pentru JPA
    public Event() {
    }

    // Constructor cu argumente
    public Event(String name, String description, LocalDateTime startTime, LocalDateTime endTime, Location location, Participant organizer) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.organizer = organizer;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Participant getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Participant organizer) {
        this.organizer = organizer;
    }

    // NOU: Getter și Setter pentru eventParticipants
    public Set<EventParticipantRelation> getEventParticipants() {
        return eventParticipants;
    }

    public void setEventParticipants(Set<EventParticipantRelation> eventParticipants) {
        this.eventParticipants = eventParticipants;
    }

    // Metode ajutătoare pentru a adăuga/elimina participanți într-o manieră bidirectională
    public void addParticipant(Participant participant) {
        EventParticipantRelation relation = new EventParticipantRelation(this, participant);
        this.eventParticipants.add(relation);
    }

    public void removeParticipant(Participant participant) {
        EventParticipantRelation relation = new EventParticipantRelation(this, participant);
        this.eventParticipants.remove(relation);
        relation.setEvent(null);
        relation.setParticipant(null);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", location=" + (location != null ? location.getName() : "null") +
                ", organizer=" + (organizer != null ? organizer.getName() : "null") +
                ", numParticipants=" + eventParticipants.size() + // Afișăm numărul de participanți
                '}';
    }
}
