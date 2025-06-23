package com.example.demo.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects; // Asigură-te că importi java.util.Objects

@Entity
@Table(name = "event_participants")
@IdClass(EventParticipantRelation.EventParticipantId.class)
public class EventParticipantRelation {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    public EventParticipantRelation() {
    }

    public EventParticipantRelation(Event event, Participant participant) {
        this.event = event;
        this.participant = participant;
    }

    // Getters și Setters
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    // NOU: Implementarea metodelor equals() și hashCode() pentru funcționarea corectă a Set-urilor
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventParticipantRelation that = (EventParticipantRelation) o;
        // Comparam id-urile, nu referințele întregi de obiecte, pentru a evita LazyInitializationException
        // și pentru a asigura unicitatea bazată pe cheile primare logice.
        // Asigură-te că event și participant au id-urile setate înainte de a fi adăugate într-un set
        // în testele unitare sau în Service.
        return Objects.equals(event != null ? event.getId() : null, that.event != null ? that.event.getId() : null) &&
                Objects.equals(participant != null ? participant.getId() : null, that.participant != null ? that.participant.getId() : null);
    }

    @Override
    public int hashCode() {
        // Hashing bazat pe id-urile evenimentului și participantului
        return Objects.hash(event != null ? event.getId() : null, participant != null ? participant.getId() : null);
    }

    // Clasa pentru cheia compozită (rămâne neschimbată, e deja OK)
    public static class EventParticipantId implements Serializable {
        private Long event;
        private Long participant;

        public EventParticipantId() {
        }

        public EventParticipantId(Long event, Long participant) {
            this.event = event;
            this.participant = participant;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventParticipantId that = (EventParticipantId) o;
            return event.equals(that.event) && participant.equals(that.participant);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(event, participant);
        }
    }
}
