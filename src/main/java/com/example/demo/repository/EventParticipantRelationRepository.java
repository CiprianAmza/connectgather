package com.example.demo.repository;

import com.example.demo.domain.EventParticipantRelation;
import com.example.demo.domain.EventParticipantRelation.EventParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventParticipantRelationRepository extends JpaRepository<EventParticipantRelation, EventParticipantId> {
    List<EventParticipantRelation> findByEventId(Long eventId);
    void deleteByEventId(Long eventId);
}
