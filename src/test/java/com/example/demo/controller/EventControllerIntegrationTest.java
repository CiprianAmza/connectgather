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
@ActiveProfiles("test")
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
    private OrganizerRepository organizerRepository;

    private Organizer testOrganizer;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        testOrganizer = new Organizer("Test Organizer", "integr_org@example.com", "0987654321");
        testOrganizer = organizerRepository.save(testOrganizer);

        testLocation = new Location("Test Location", "Test Address", "Description");
        testLocation = locationRepository.save(testLocation);
    }


    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
    void testListEvents() throws Exception {
        Event event = new Event(
                "Integration Test Event",
                "Description",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6),
                testLocation,
                testOrganizer
        );
        eventRepository.save(event);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(model().attributeExists("eventsPage"))
                .andExpect(content().string(containsString("Integration Test Event")));
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attribute("event", hasProperty("id", is(nullValue()))))
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", eventName)
                        .param("description", description)
                        .param("startTime", startTime.format(formatter))
                        .param("endTime", endTime.format(formatter))
                        .param("locationId", testLocation.getId().toString())
                        .param("organizerId", testOrganizer.getId().toString())
                        .param("selectedParticipantIds", "")
                        .param("eventDetails.venueCapacity", "100")
                        .param("eventDetails.cateringOption", "true")
                        .param("eventDetails.requiredEquipment", "Projector, Microphones")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testSaveEventValidationFailure() throws Exception {
        String startTime = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String endTime = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
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
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().attributeHasFieldErrors("event", "name"));
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void testDeleteEvent() throws Exception {
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
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attributeExists("message"));

        assertFalse(eventRepository.findById(eventToDelete.getId()).isPresent());
    }

}
