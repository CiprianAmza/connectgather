package com.example.demo.service;

import com.example.demo.domain.Event;
import com.example.demo.domain.EventDetails;
import com.example.demo.domain.EventParticipantRelation;
import com.example.demo.domain.Location;
import com.example.demo.domain.Organizer; // NOU: Import pentru Organizer
import com.example.demo.domain.Participant;
import com.example.demo.repository.EventParticipantRelationRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.OrganizerRepository; // NOU: Import pentru OrganizerRepository
import com.example.demo.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final EventParticipantRelationRepository eventParticipantRelationRepository;
    private final ParticipantRepository participantRepository;
    private final OrganizerRepository organizerRepository; // NOU: Injectează OrganizerRepository
    private final Counter eventCreationCounter;
    private final Counter eventDeletionCounter;


    @Autowired
    public EventService(EventRepository eventRepository,
                        EventParticipantRelationRepository eventParticipantRelationRepository,
                        ParticipantRepository participantRepository,
                        OrganizerRepository organizerRepository, // NOU: Adaugă în constructor
                        MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.eventParticipantRelationRepository = eventParticipantRelationRepository;
        this.participantRepository = participantRepository;
        this.organizerRepository = organizerRepository; // NOU: Inițializare
        this.eventCreationCounter = Counter.builder("events.created.total")
                .description("Total number of events created or updated")
                .register(meterRegistry);
        this.eventDeletionCounter = Counter.builder("events.deleted.total")
                .description("Total number of events deleted")
                .register(meterRegistry);
    }

    @Transactional(readOnly = true)
    public List<Event> findAllEvents() {
        logger.info("Fetching all events.");
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Event> findAllEvents(Pageable pageable) {
        logger.debug("Fetching events with pageable: {}", pageable);
        return eventRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Event> findEventById(Long id) {
        logger.info("Fetching event by ID: {}", id);
        return eventRepository.findById(id);
    }

    @Transactional
    public Event saveEvent(Event event, Set<Long> selectedParticipantIds) {
        logger.info("Attempting to save event: {}", event.getName());

        // Asigurăm legătura bidirecțională înainte de a salva evenimentul principal.
        if (event.getEventDetails() != null) {
            event.getEventDetails().setEvent(event);
        }

        Event savedEvent = eventRepository.save(event);
        logger.debug("Event initially saved/updated with ID: {}", savedEvent.getId());

        if (selectedParticipantIds != null) {
            logger.debug("Processing {} selected participant IDs for event ID: {}", selectedParticipantIds.size(), savedEvent.getId());
            eventParticipantRelationRepository.deleteByEventId(savedEvent.getId());
            savedEvent.getEventParticipants().clear();

            for (Long participantId : selectedParticipantIds) {
                participantRepository.findById(participantId).ifPresentOrElse(participant -> {
                    EventParticipantRelation relation = new EventParticipantRelation(savedEvent, participant);
                    savedEvent.getEventParticipants().add(relation);
                    logger.debug("Added participant {} (ID: {}) to event {}", participant.getName(), participant.getId(), savedEvent.getName());
                }, () -> logger.warn("Participant with ID {} not found for event {}. Skipping.", participantId, savedEvent.getName()));
            }
        } else {
            eventParticipantRelationRepository.deleteByEventId(savedEvent.getId());
            savedEvent.getEventParticipants().clear();
            logger.debug("No participants selected for event {}. Clearing existing relations.", savedEvent.getName());
        }

        eventCreationCounter.increment();
        return eventRepository.save(savedEvent);
    }


    @Transactional
    public Event updateEvent(Long id, Event eventDetailsUpdate, Set<Long> selectedParticipantIds) {
        logger.info("Updating event with ID: {}", id);
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Attempted to update non-existent event with ID: {}", id);
                    return new IllegalArgumentException("Evenimentul nu a fost găsit cu id: " + id);
                });

        existingEvent.setName(eventDetailsUpdate.getName());
        existingEvent.setDescription(eventDetailsUpdate.getDescription());
        existingEvent.setStartTime(eventDetailsUpdate.getStartTime());
        existingEvent.setEndTime(eventDetailsUpdate.getEndTime());
        existingEvent.setLocation(eventDetailsUpdate.getLocation());
        // NOU: Setăm organizatorul pe baza Organizer-ului trimis, nu a Participant-ului
        existingEvent.setOrganizer(eventDetailsUpdate.getOrganizer());

        if (eventDetailsUpdate.getEventDetails() != null) {
            if (existingEvent.getEventDetails() == null) {
                existingEvent.setEventDetails(eventDetailsUpdate.getEventDetails());
                existingEvent.getEventDetails().setEvent(existingEvent);
                logger.debug("New EventDetails set for event ID: {}", id);
            } else {
                existingEvent.getEventDetails().setVenueCapacity(eventDetailsUpdate.getEventDetails().getVenueCapacity());
                existingEvent.getEventDetails().setCateringOption(eventDetailsUpdate.getEventDetails().getCateringOption());
                existingEvent.getEventDetails().setRequiredEquipment(eventDetailsUpdate.getEventDetails().getRequiredEquipment());
                logger.debug("Existing EventDetails updated for event ID: {}", id);
            }
        } else {
            if (existingEvent.getEventDetails() != null) {
                existingEvent.setEventDetails(null);
                logger.debug("EventDetails removed for event ID: {} as no details were provided.", id);
            } else {
                logger.debug("No EventDetails to update or remove for event ID: {}.", id);
            }
        }

        logger.debug("Processing {} selected participant IDs for event ID {} during update.", selectedParticipantIds != null ? selectedParticipantIds.size() : 0, existingEvent.getId());
        eventParticipantRelationRepository.deleteByEventId(existingEvent.getId());
        existingEvent.getEventParticipants().clear();

        if (selectedParticipantIds != null && !selectedParticipantIds.isEmpty()) {
            for (Long participantId : selectedParticipantIds) {
                participantRepository.findById(participantId).ifPresentOrElse(participant -> {
                    EventParticipantRelation relation = new EventParticipantRelation(existingEvent, participant);
                    existingEvent.getEventParticipants().add(relation);
                    logger.debug("Added participant {} (ID: {}) to event {} during update.", participant.getName(), participant.getId(), existingEvent.getName());
                }, () -> logger.warn("Participant with ID {} not found for event ID {} during update. Skipping.", participantId, existingEvent.getId()));
            }
        } else {
            logger.debug("No participants selected for event ID {} during update. Clearing existing relations.", existingEvent.getId());
        }

        Event updatedEvent = eventRepository.save(existingEvent);
        logger.info("Event with ID {} updated successfully with participants and details.", id);
        return updatedEvent;
    }

    @Transactional
    public void deleteEvent(Long id) {
        logger.info("Attempting to delete event with ID: {}", id);
        if (!eventRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent event with ID: {}", id);
            throw new IllegalArgumentException("Evenimentul nu a fost găsit cu id: " + id);
        }
        eventRepository.deleteById(id);
        logger.info("Event with ID {} deleted successfully.", id);
        eventDeletionCounter.increment();
    }
}
