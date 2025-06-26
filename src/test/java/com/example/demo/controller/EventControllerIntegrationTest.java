package com.example.demo.controller;

import com.example.demo.domain.Event;
import com.example.demo.domain.Location;
import com.example.demo.domain.Organizer;
import com.example.demo.domain.Participant;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.LocationRepository;
import com.example.demo.repository.OrganizerRepository;
import com.example.demo.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // Asigură că rulează cu un profil de test, de obicei cu H2
public class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private OrganizerRepository organizerRepository; // NOU: Injectează OrganizerRepository

    private Organizer testOrganizer;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        // Creează un organizator de test și salvează-l
        testOrganizer = new Organizer("Test Organizer", "integr_org@example.com", "0987654321");
        testOrganizer = organizerRepository.save(testOrganizer);

        // Creează o locație de test și salvează-o
        testLocation = new Location("Test Location", "Test Address", "Description");
        testLocation = locationRepository.save(testLocation);
    }


    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"}) // Simulează un utilizator autentificat
    void testListEvents() throws Exception {
        // Creează un eveniment de test și salvează-l în baza de date de test
        Event event = new Event(
                "Integration Test Event",
                "Description",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6),
                testLocation,
                testOrganizer
        );
        eventRepository.save(event);

        // Efectuează o cerere GET către /events și verifică răspunsul
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk()) // Așteaptă un status HTTP 200 OK
                .andExpect(view().name("events/list")) // Așteaptă că a fost returnată vizualizarea 'events/list'
                .andExpect(model().attributeExists("eventsPage")) // Verifică dacă modelul conține 'eventsPage'
                .andExpect(content().string(containsString("Integration Test Event"))); // Verifică conținutul HTML
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"}) // Simulează un utilizator cu rol ADMIN
    void testShowCreateForm() throws Exception {
        // Efectuează o cerere GET către /events/new pentru a afișa formularul de creare
        mockMvc.perform(get("/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeExists("event")) // Verifică dacă modelul conține un obiect 'event'
                .andExpect(model().attribute("event", hasProperty("id", is(nullValue())))) // Verifică dacă este un eveniment nou (ID null)
                .andExpect(model().attributeExists("allParticipants")) // Verifică prezența listei de participanți
                .andExpect(model().attributeExists("locations")) // Verifică prezența listei de locații
                .andExpect(model().attributeExists("allOrganizers")); // NOU: Verifică prezența listei de organizatori
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testSaveEventSuccess() throws Exception {
        String eventName = "New Event";
        String description = "New Event Description";
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Efectuează o cerere POST pentru a salva un eveniment nou
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", eventName)
                        .param("description", description)
                        .param("startTime", startTime.format(formatter))
                        .param("endTime", endTime.format(formatter))
                        .param("locationId", testLocation.getId().toString())
                        .param("organizerId", testOrganizer.getId().toString()) // Utilizează ID-ul organizatorului de test
                        .param("selectedParticipantIds", "") // Poate fi gol sau cu ID-uri
                        .param("eventDetails.venueCapacity", "100")
                        .param("eventDetails.cateringOption", "true")
                        .param("eventDetails.requiredEquipment", "Projector, Microphones")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // Adaugă token CSRF
                .andExpect(status().is3xxRedirection()) // Așteaptă o redirecționare (3xx)
                .andExpect(redirectedUrl("/events")) // Verifică URL-ul de redirecționare
                .andExpect(flash().attributeExists("message")); // Verifică prezența unui mesaj flash de succes
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testSaveEventValidationFailure() throws Exception {
        String startTime = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String endTime = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // Efectuează o cerere POST cu date invalide (nume gol)
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "") // Nume gol, ar trebui să eșueze validarea
                        .param("description", "Descriere valida")
                        .param("startTime", startTime)
                        .param("endTime", endTime)
                        .param("locationId", testLocation.getId().toString())
                        .param("organizerId", testOrganizer.getId().toString())
                        .param("selectedParticipantIds", "")
                        .param("eventDetails.venueCapacity", "100")
                        .param("eventDetails.cateringOption", "true")
                        .param("eventDetails.requiredEquipment", "")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk()) // Așteaptă un status HTTP 200 OK (reafisează formularul)
                .andExpect(view().name("events/form")) // Verifică că a fost returnată vizualizarea formularului
                .andExpect(model().attributeHasFieldErrors("event", "name")); // Verifică dacă există erori de validare pe câmpul 'name'
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testDeleteEvent() throws Exception {
        // Creează și salvează un eveniment de șters
        Event eventToDelete = new Event(
                "Event to Delete",
                "Description",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                testLocation,
                testOrganizer
        );
        eventToDelete = eventRepository.save(eventToDelete);

        // Efectuează o cerere GET către endpoint-ul de ștergere
        mockMvc.perform(get("/events/delete/{id}", eventToDelete.getId())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attributeExists("message"));

        // Verifică dacă evenimentul a fost șters din baza de date
        assertFalse(eventRepository.findById(eventToDelete.getId()).isPresent());
    }

}
