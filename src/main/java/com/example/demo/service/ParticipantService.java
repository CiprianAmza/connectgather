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

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
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
        // Poți adăuga aici logica de business, ex: verificare unicitate email înainte de salvare
        return participantRepository.save(participant);
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
    }
}
