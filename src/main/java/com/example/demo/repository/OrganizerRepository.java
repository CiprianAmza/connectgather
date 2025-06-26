package com.example.demo.repository;

import com.example.demo.domain.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    Optional<Organizer> findByName(String name);
    Optional<Organizer> findByEmail(String email);
}
