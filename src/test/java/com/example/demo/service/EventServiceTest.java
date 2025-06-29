package com.example.demo.service;

import com.example.demo.domain.Event;
import com.example.demo.domain.Location;
import com.example.demo.domain.Organizer;
import com.example.demo.domain.Participant;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizerRepository organizerRepository;

    @Mock
    private EventParticipantRelationRepository eventParticipantRelationRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventDetailsRepository detailsRepository;

    private MeterRegistry meterRegistry;

    private Counter eventCreationCounter;

    private EventService eventService;

    private Event testEvent;
    private Organizer testOrganizer;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        testOrganizer = new Organizer("Test Organizer", "organizer@example.com", "1234567890");
        testOrganizer.setId(1L);

        testLocation = new Location("Test Location", "Test Address", "Test Description");
        testLocation.setId(10L);

        testEvent = new Event(
                "Test Event",
                "Description of Test Event",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                testLocation,
                testOrganizer
        );
        testEvent.setId(1L);
        testEvent.setEventParticipants(new HashSet<>());

        meterRegistry = new SimpleMeterRegistry();
        eventService = new EventService(eventRepository, eventParticipantRelationRepository, participantRepository, organizerRepository, meterRegistry);

        eventCreationCounter = meterRegistry.counter("events.created.total");
    }

    @Test
    void testFindAllEvents() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> foundEvents = eventService.findAllEvents();

        assertNotNull(foundEvents);
        assertFalse(foundEvents.isEmpty());
        assertEquals(1, foundEvents.size());
        assertEquals(testEvent.getName(), foundEvents.get(0).getName());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testFindAllEventsPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(Arrays.asList(testEvent), pageable, 1);
        when(eventRepository.findAll(pageable)).thenReturn(eventPage);

        Page<Event> foundPage = eventService.findAllEvents(pageable);

        assertNotNull(foundPage);
        assertEquals(1, foundPage.getTotalElements());
        assertEquals(testEvent.getName(), foundPage.getContent().get(0).getName());
        verify(eventRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindEventById() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        Optional<Event> foundEvent = eventService.findEventById(1L);

        assertTrue(foundEvent.isPresent());
        assertEquals(testEvent.getName(), foundEvent.get().getName());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testFindEventByIdNotFound() {
        when(eventRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Event> foundEvent = eventService.findEventById(2L);

        assertFalse(foundEvent.isPresent());
        verify(eventRepository, times(1)).findById(2L);
    }

    @Test
    void testSaveEvent() {
        Set<Long> selectedParticipantIds = new HashSet<>(Arrays.asList(2L, 3L));
        Participant p2 = new Participant("Participant 2", "p2@example.com", "111");
        p2.setId(2L);
        Participant p3 = new Participant("Participant 3", "p3@example.com", "222");
        p3.setId(3L);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event eventArg = invocation.getArgument(0);
            return eventArg;
        });
        when(participantRepository.findById(2L)).thenReturn(Optional.of(p2));
        when(participantRepository.findById(3L)).thenReturn(Optional.of(p3));
        doNothing().when(eventParticipantRelationRepository).deleteByEventId(anyLong());

        double initialCount = eventCreationCounter.count();

        Event savedEvent = eventService.saveEvent(testEvent, selectedParticipantIds);

        assertNotNull(savedEvent);
        assertEquals(testEvent.getName(), savedEvent.getName());
        assertEquals(2, savedEvent.getEventParticipants().size());
        verify(eventRepository, times(2)).save(testEvent);
        verify(eventParticipantRelationRepository, times(1)).deleteByEventId(testEvent.getId());
        verify(participantRepository, times(1)).findById(2L);
        verify(participantRepository, times(1)).findById(3L);

        assertEquals(initialCount + 1, eventCreationCounter.count());
    }

    @Test
    void testUpdateEvent() {
        Event updatedDetails = new Event(
                "Updated Event",
                "Updated Description",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                testLocation,
                testOrganizer
        );
        updatedDetails.setId(1L);

        Set<Long> newParticipantIds = new HashSet<>(Arrays.asList(5L));
        Participant p5 = new Participant("Participant 5", "p5@example.com", "333");
        p5.setId(5L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event eventArg = invocation.getArgument(0);
            return eventArg;
        });
        doNothing().when(eventParticipantRelationRepository).deleteByEventId(anyLong());
        when(participantRepository.findById(5L)).thenReturn(Optional.of(p5));

        Event result = eventService.updateEvent(1L, updatedDetails, newParticipantIds);

        assertNotNull(result);
        assertEquals("Updated Event", result.getName());
        assertEquals(1, result.getEventParticipants().size());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(testEvent);
        verify(eventParticipantRelationRepository, times(1)).deleteByEventId(testEvent.getId());
        verify(participantRepository, times(1)).findById(5L);
    }

    @Test
    void testDeleteEvent() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(1L);

        eventService.deleteEvent(1L);

        verify(eventRepository, times(1)).existsById(1L);
        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteEventNotFound() {
        when(eventRepository.existsById(2L)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.deleteEvent(2L);
        });

        assertEquals("Evenimentul nu a fost găsit cu id: 2", exception.getMessage());
        verify(eventRepository, times(1)).existsById(2L);
        verify(eventRepository, never()).deleteById(anyLong());
    }
}
