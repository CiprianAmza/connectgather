package com.example.demo.controller;

import com.example.demo.domain.Event;
import com.example.demo.domain.Location;
import com.example.demo.domain.Participant;
import com.example.demo.service.EventService;
import com.example.demo.service.LocationService;
import com.example.demo.service.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/events")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class); // Instanțiază logger-ul

    private final EventService eventService;
    private final ParticipantService participantService;
    private final LocationService locationService;

    @Autowired
    public EventController(EventService eventService, ParticipantService participantService, LocationService locationService) {
        this.eventService = eventService;
        this.participantService = participantService;
        this.locationService = locationService;
    }

    @GetMapping
    public String listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        logger.info("Request to list events. Page: {}, Size: {}, SortBy: {}, SortDir: {}", page, size, sortBy, sortDir); // Logging statement
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Event> eventsPage = eventService.findAllEvents(pageable);

        model.addAttribute("eventsPage", eventsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "events/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        logger.info("Displaying create new event form."); // Logging statement
        model.addAttribute("event", new Event());
        model.addAttribute("allParticipants", participantService.findAllParticipants());
        model.addAttribute("locations", locationService.findAllLocations());
        return "events/form";
    }

    @PostMapping
    public String saveEvent(@Valid @ModelAttribute("event") Event event,
                            BindingResult bindingResult,
                            @RequestParam("organizerId") Long organizerId,
                            @RequestParam("locationId") Long locationId,
                            @RequestParam(value = "selectedParticipantIds", required = false) Set<Long> selectedParticipantIds,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        logger.info("Attempting to save event: {}", event.getName()); // Logging statement
        logger.debug("Received event details: {}, Organizer ID: {}, Location ID: {}, Selected Participants IDs: {}",
                event, organizerId, locationId, selectedParticipantIds); // Logging statement

        // Re-populăm listele pentru formular, în caz de eroare
        model.addAttribute("allParticipants", participantService.findAllParticipants());
        model.addAttribute("locations", locationService.findAllLocations());

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred for event {}: {}", event.getName(), bindingResult.getAllErrors()); // Logging statement
            return "events/form";
        }

        // Validare suplimentară pentru endTime > startTime
        if (event.getStartTime() != null && event.getEndTime() != null && event.getEndTime().isBefore(event.getStartTime())) {
            bindingResult.rejectValue("endTime", "error.event", "Data și ora de sfârșit trebuie să fie după data și ora de început.");
            logger.warn("End time is before start time for event: {}", event.getName()); // Logging statement
            return "events/form";
        }

        // Căutăm participantul organizator după ID și îl setăm pe eveniment
        Optional<Participant> organizerOptional = participantService.findParticipantById(organizerId);
        if (organizerOptional.isEmpty()) {
            bindingResult.rejectValue("organizer", "error.event", "Organizatorul selectat nu există.");
            logger.warn("Organizer with ID {} not found for event {}.", organizerId, event.getName()); // Logging statement
            return "events/form";
        }
        event.setOrganizer(organizerOptional.get());

        // Căutăm locația după ID și o setăm pe eveniment
        Optional<Location> locationOptional = locationService.findLocationById(locationId);
        if (locationOptional.isEmpty()) {
            bindingResult.rejectValue("location", "error.event", "Locația selectată nu există.");
            logger.warn("Location with ID {} not found for event {}.", locationId, event.getName()); // Logging statement
            return "events/form";
        }
        event.setLocation(locationOptional.get());

        eventService.saveEvent(event, selectedParticipantIds);

        redirectAttributes.addFlashAttribute("message", "Eveniment salvat cu succes!");
        logger.info("Event {} saved successfully, redirecting to /events.", event.getName()); // Logging statement
        return "redirect:/events";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Displaying edit form for event ID: {}", id); // Logging statement
        Optional<Event> event = eventService.findEventById(id);
        if (event.isPresent()) {
            Event existingEvent = event.get();
            model.addAttribute("event", existingEvent);
            model.addAttribute("allParticipants", participantService.findAllParticipants());
            model.addAttribute("locations", locationService.findAllLocations());

            Set<Long> currentParticipantIds = existingEvent.getEventParticipants().stream()
                    .map(epr -> epr.getParticipant().getId())
                    .collect(Collectors.toSet());
            model.addAttribute("currentParticipantIds", currentParticipantIds);

            return "events/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Evenimentul nu a fost găsit!");
            logger.warn("Attempted to edit non-existent event with ID: {}", id); // Logging statement
            return "redirect:/events";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("event") Event eventDetails,
                              BindingResult bindingResult,
                              @RequestParam("organizerId") Long organizerId,
                              @RequestParam("locationId") Long locationId,
                              @RequestParam(value = "selectedParticipantIds", required = false) Set<Long> selectedParticipantIds,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        logger.info("Attempting to update event with ID: {}", id); // Logging statement
        logger.debug("Received update details for event ID {}: Event: {}, Organizer ID: {}, Location ID: {}, Selected Participants IDs: {}",
                id, eventDetails, organizerId, locationId, selectedParticipantIds); // Logging statement

        model.addAttribute("allParticipants", participantService.findAllParticipants());
        model.addAttribute("locations", locationService.findAllLocations());
        model.addAttribute("currentParticipantIds", selectedParticipantIds);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred during update for event ID {}: {}", id, bindingResult.getAllErrors()); // Logging statement
            return "events/form";
        }

        if (eventDetails.getStartTime() != null && eventDetails.getEndTime() != null && eventDetails.getEndTime().isBefore(eventDetails.getStartTime())) {
            bindingResult.rejectValue("endTime", "error.event", "Data și ora de sfârșit trebuie să fie după data și ora de început.");
            logger.warn("End time is before start time for event ID: {}", id); // Logging statement
            return "events/form";
        }

        Optional<Participant> organizerOptional = participantService.findParticipantById(organizerId);
        if (organizerOptional.isEmpty()) {
            bindingResult.rejectValue("organizer", "error.event", "Organizatorul selectat nu există.");
            logger.warn("Organizer with ID {} not found during update for event ID {}.", organizerId, id); // Logging statement
            return "events/form";
        }
        eventDetails.setOrganizer(organizerOptional.get());

        Optional<Location> locationOptional = locationService.findLocationById(locationId);
        if (locationOptional.isEmpty()) {
            bindingResult.rejectValue("location", "error.event", "Locația selectată nu există.");
            logger.warn("Location with ID {} not found during update for event ID {}.", locationId, id); // Logging statement
            return "events/form";
        }
        eventDetails.setLocation(locationOptional.get());

        eventService.updateEvent(id, eventDetails, selectedParticipantIds);
        redirectAttributes.addFlashAttribute("message", "Eveniment actualizat cu succes!");
        logger.info("Event with ID {} updated successfully, redirecting to /events.", id); // Logging statement
        return "redirect:/events";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Request to delete event with ID: {}", id); // Logging statement
        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("message", "Eveniment șters cu succes!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            logger.error("Error deleting event with ID {}: {}", id, e.getMessage()); // Logging statement
        }
        return "redirect:/events";
    }
}