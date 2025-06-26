package com.example.demo.service;

import com.example.demo.domain.Participant;
import com.example.demo.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final Counter participantCreationCounter;
    private final Counter participantDeletionCounter;

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository, MeterRegistry meterRegistry) {
        this.participantRepository = participantRepository;
        this.participantCreationCounter = Counter.builder("participants.created.total")
                .description("Total number of participants created or updated")
                .register(meterRegistry);
        this.participantDeletionCounter = Counter.builder("participants.deleted.total")
                .description("Total number of participants deleted")
                .register(meterRegistry);
    }

    @Transactional(readOnly = true)
    public List<Participant> findAllParticipants() {
        return participantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Participant> findAllParticipants(Pageable pageable) {
        return participantRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Participant> findParticipantById(Long id) {
        return participantRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Participant> findParticipantByName(String name) {
        return participantRepository.findByName(name);
    }

    @Transactional
    public Participant saveParticipant(Participant participant) {
        Participant savedParticipant = participantRepository.save(participant);
        participantCreationCounter.increment(); // Incrementăm contorul la adăugare/salvare
        return savedParticipant;
    }

    @Transactional
    public Participant updateParticipant(Long id, Participant participantDetails) {
        Participant existingParticipant = participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + id));

        existingParticipant.setName(participantDetails.getName());
        existingParticipant.setEmail(participantDetails.getEmail());
        existingParticipant.setPhone(participantDetails.getPhone());

        return participantRepository.save(existingParticipant);
    }

    @Transactional
    public void deleteParticipant(Long id) {
        if (!participantRepository.existsById(id)) {
            throw new IllegalArgumentException("Participant not found with id: " + id);
        }
        participantRepository.deleteById(id);
        participantDeletionCounter.increment(); // Incrementăm contorul la ștergere
    }
}
