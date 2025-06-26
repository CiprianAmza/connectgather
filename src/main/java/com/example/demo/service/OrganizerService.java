package com.example.demo.service;

import com.example.demo.domain.Organizer;
import com.example.demo.repository.OrganizerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizerService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizerService.class);

    private final OrganizerRepository organizerRepository;
    private final Counter organizerCreationCounter;
    private final Counter organizerDeletionCounter;

    @Autowired
    public OrganizerService(OrganizerRepository organizerRepository, MeterRegistry meterRegistry) {
        this.organizerRepository = organizerRepository;
        this.organizerCreationCounter = Counter.builder("organizers.created.total")
                .description("Total number of organizers created or updated")
                .register(meterRegistry);
        this.organizerDeletionCounter = Counter.builder("organizers.deleted.total")
                .description("Total number of organizers deleted")
                .register(meterRegistry);
    }

    @Transactional(readOnly = true)
    public List<Organizer> findAllOrganizers() {
        logger.info("Fetching all organizers.");
        return organizerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Organizer> findAllOrganizers(Pageable pageable) {
        logger.debug("Fetching organizers with pageable: {}", pageable);
        return organizerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Organizer> findOrganizerById(Long id) {
        logger.info("Fetching organizer by ID: {}", id);
        return organizerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Organizer> findOrganizerByEmail(String email) {
        logger.info("Fetching organizer by email: {}", email);
        return organizerRepository.findByEmail(email);
    }

    @Transactional
    public Organizer saveOrganizer(Organizer organizer) {
        logger.info("Attempting to save organizer: {}", organizer.getName());
        Organizer savedOrganizer = organizerRepository.save(organizer);
        organizerCreationCounter.increment();
        logger.info("Organizer {} saved successfully with ID: {}", savedOrganizer.getName(), savedOrganizer.getId());
        return savedOrganizer;
    }

    @Transactional
    public Organizer updateOrganizer(Long id, Organizer organizerDetails) {
        logger.info("Updating organizer with ID: {}", id);
        Organizer existingOrganizer = organizerRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Attempted to update non-existent organizer with ID: {}", id);
                    return new IllegalArgumentException("Organizatorul nu a fost găsit cu id: " + id);
                });

        existingOrganizer.setName(organizerDetails.getName());
        existingOrganizer.setEmail(organizerDetails.getEmail());
        existingOrganizer.setPhone(organizerDetails.getPhone());

        Organizer updatedOrganizer = organizerRepository.save(existingOrganizer);
        logger.info("Organizer with ID {} updated successfully.", id);
        return updatedOrganizer;
    }

    @Transactional
    public void deleteOrganizer(Long id) {
        logger.info("Attempting to delete organizer with ID: {}", id);
        if (!organizerRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent organizer with ID: {}", id);
            throw new IllegalArgumentException("Organizatorul nu a fost găsit cu id: " + id);
        }
        organizerRepository.deleteById(id);
        organizerDeletionCounter.increment();
        logger.info("Organizer with ID {} deleted successfully.", id);
    }
}
