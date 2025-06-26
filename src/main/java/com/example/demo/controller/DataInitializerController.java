package com.example.demo.controller;

import com.example.demo.domain.Location;
import com.example.demo.domain.Participant;
import com.example.demo.service.LocationService;
import com.example.demo.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class DataInitializerController {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializerController.class);

    private final LocationService locationService;
    private final ParticipantService participantService;

    @Autowired
    public DataInitializerController(LocationService locationService, ParticipantService participantService) {
        this.locationService = locationService;
        this.participantService = participantService;
    }

    /**
     * Endpoint pentru inițializarea datelor de test: 10 locații și 10 organizatori.
     * Această metodă ar trebui utilizată doar în medii de dezvoltare/test.
     * @return ResponseEntity cu un mesaj de succes sau eroare.
     */
    @GetMapping("/init-data")
    public ResponseEntity<String> initializeData() {
        logger.info("Initializing test data (locations and organizers)...");
        try {
            // Inițializare Locații
            for (int i = 1; i <= 100; i++) {
                Location location = new Location(
                        "Locatie Test " + i,
                        "Adresa Test " + i + ", Oras " + (i % 5 + 1), // Simulează 5 orașe diferite
                        "Descriere Locatie " + i
                );
                locationService.saveLocation(location);
                logger.debug("Saved location: {}", location.getName());
            }
            logger.info("100 test locations initialized successfully.");

            // Inițializare Organizatori (ca Participanți)
            for (int i = 1; i <= 10; i++) {
                Participant organizer = new Participant(
                        "Organizator Test " + i,
                        "organizator" + i + "@test.com",
                        "0700-123-0" + (i < 10 ? "0" : "") + i
                );
                participantService.saveParticipant(organizer);
                logger.debug("Saved organizer: {}", organizer.getName());
            }
            logger.info("10 test organizers initialized successfully.");

            return ResponseEntity.ok("Date de test inițializate cu succes (100 locații, 10 organizatori).");
        } catch (Exception e) {
            logger.error("Error during data initialization: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Eroare la inițializarea datelor: " + e.getMessage());
        }
    }
}
