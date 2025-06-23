package com.example.demo.controller;

import com.example.demo.domain.Event;
import com.example.demo.domain.Location;
import com.example.demo.domain.Participant;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Pentru a simula utilizator autentificat
import org.springframework.test.context.ActiveProfiles; // Poate fi util pentru profile de test
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional; // Pentru a rula teste tranzacțional

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // Încarcă întregul context Spring Boot
@AutoConfigureMockMvc // Configurează MockMvc pentru a simula cereri HTTP
@Transactional // Fiecare test rulează într-o tranzacție, care este rollback-ată la final
@ActiveProfiles("test") // Poți crea un application-test.properties dacă ai nevoie de config specifice test
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Injectează MockMvc pentru a face cereri HTTP simulate

    @Autowired
    private EventRepository eventRepository; // Injectează repository-ul pentru a pregăti date
    @Autowired
    private ParticipantRepository participantRepository; // Injectează repository-ul participantului
    @Autowired
    private LocationRepository locationRepository; // Injectează repository-ul locației

    private Participant testOrganizer;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        // Asigură-te că există un organizator și o locație în baza de date pentru test
        testOrganizer = new Participant("Test Organizer", "integr_org@example.com", "0987654321");
        testOrganizer = participantRepository.save(testOrganizer); // Salvează și actualizează ID-ul

        testLocation = new Location("Test Location", "Test Address", "Description");
        testLocation = locationRepository.save(testLocation); // Salvează și actualizează ID-ul
    }


    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"}) // Simulează un utilizator autentificat cu rol ADMIN
    void testListEvents() throws Exception {
        // Creează un eveniment în baza de date pentru a-l testa
        Event event = new Event(
                "Integration Test Event",
                "Description",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6),
                testLocation,
                testOrganizer
        );
        eventRepository.save(event);

        mockMvc.perform(get("/events")) // Efectuează o cerere GET la /events
                .andExpect(status().isOk()) // Așteaptă un status HTTP 200 OK
                .andExpect(view().name("events/list")) // Așteaptă ca vederea să fie "events/list"
                .andExpect(model().attributeExists("eventsPage")) // Verifică existența atributului în model
                .andExpect(content().string(containsString("Integration Test Event"))); // Verifică conținutul HTML
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"}) // Doar ADMIN poate accesa /events/new
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attribute("event", hasProperty("id", is(nullValue())))) // Verifică că evenimentul este nou
                .andExpect(model().attributeExists("allParticipants"))
                .andExpect(model().attributeExists("locations"));
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testSaveEventSuccess() throws Exception {
        String eventName = "New Event";
        String description = "New Event Description";
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);

        // Formatează LocalDateTime pentru a fi compatibil cu input[type=datetime-local]
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        mockMvc.perform(post("/events") // Efectuează o cerere POST la /events
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Tipul conținutului
                        .param("name", eventName)
                        .param("description", description)
                        .param("startTime", startTime.format(formatter))
                        .param("endTime", endTime.format(formatter))
                        .param("locationId", testLocation.getId().toString()) // Folosește ID-ul locației de test
                        .param("organizerId", testOrganizer.getId().toString()) // Folosește ID-ul organizatorului de test
                        .param("selectedParticipantIds", "") // Poate fi gol sau cu ID-uri
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // Adaugă token CSRF
                .andExpect(status().is3xxRedirection()) // Așteaptă redirecționare (302)
                .andExpect(redirectedUrl("/events")) // Așteaptă redirecționare către /events
                .andExpect(flash().attributeExists("message")); // Verifică mesajul flash
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testSaveEventValidationFailure() throws Exception {
        // Încearcă să salvezi un eveniment cu nume gol (va eșua validarea)
        String startTime = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String endTime = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "") // Nume gol, va eșua @NotBlank
                        .param("description", "Descriere valida")
                        .param("startTime", startTime)
                        .param("endTime", endTime)
                        .param("locationId", testLocation.getId().toString())
                        .param("organizerId", testOrganizer.getId().toString())
                        .param("selectedParticipantIds", "")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()) // Va rămâne pe aceeași pagină (200 OK)
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeHasFieldErrors("event", "name")); // Verifică erorile de validare
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testDeleteEvent() throws Exception {
        // Creează un eveniment de șters
        Event eventToDelete = new Event(
                "Event to Delete",
                "Description",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                testLocation,
                testOrganizer
        );
        eventToDelete = eventRepository.save(eventToDelete);

        mockMvc.perform(get("/events/delete/{id}", eventToDelete.getId())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // CSRF e necesar și pentru GET-uri de delete
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attributeExists("message"));

        // Verifică că evenimentul a fost șters din baza de date
        assertFalse(eventRepository.findById(eventToDelete.getId()).isPresent());
    }

}
