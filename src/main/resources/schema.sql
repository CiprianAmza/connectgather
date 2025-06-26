-- Ordinea este importantă din cauza cheilor străine
-- Se șterg întâi tabelele care depind de altele

DROP TABLE IF EXISTS event_details;
DROP TABLE IF EXISTS event_participants;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS organizers;
-- DROP TABLE IF EXISTS authorities; -- ELIMINAT: gestionat de AuthService
-- DROP TABLE IF EXISTS app_users;   -- ELIMINAT: gestionat de AuthService


-- Tabele pentru Spring Security (ELIMINATE DIN ACEST SCHEMA.SQL, acum sunt în AuthService)
-- CREATE TABLE app_users ( ... );
-- CREATE TABLE authorities ( ... );
-- CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

-- Tabel pentru Participanți
CREATE TABLE participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(25) NOT NULL
);

-- Tabel pentru Organizatori
CREATE TABLE organizers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(25) NOT NULL
);

-- Tabel pentru Locații
CREATE TABLE locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);

-- Tabel pentru Evenimente (cu foreign keys către locations și organizers)
CREATE TABLE events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    location_id BIGINT NOT NULL,
    organizer_id BIGINT NOT NULL,
    CONSTRAINT fk_event_location FOREIGN KEY (location_id) REFERENCES locations (id),
    CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id) REFERENCES organizers (id)
);

-- Tabel pentru EventDetails (One-to-One cu Events)
CREATE TABLE event_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id BIGINT NOT NULL UNIQUE,
    venue_capacity INT,
    catering_option BOOLEAN NOT NULL,
    required_equipment VARCHAR(500),
    CONSTRAINT fk_event_details_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);


-- Tabel de joncțiune pentru relația Many-to-Many Event-Participant
CREATE TABLE event_participants (
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    PRIMARY KEY (event_id, participant_id),
    CONSTRAINT fk_event_participants_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_participants_participant FOREIGN KEY (participant_id) REFERENCES participants (id) ON DELETE CASCADE
);
