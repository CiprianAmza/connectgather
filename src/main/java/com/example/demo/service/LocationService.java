package com.example.demo.service;

import com.example.demo.domain.Location;
import com.example.demo.repository.LocationRepository;
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
public class LocationService {

    private final LocationRepository locationRepository;
    private final Counter locationCreationCounter;
    private final Counter locationDeletionCounter;

    @Autowired
    public LocationService(LocationRepository locationRepository, MeterRegistry meterRegistry) {
        this.locationRepository = locationRepository;
        this.locationCreationCounter = Counter.builder("locations_created_total")
                .description("Total number of locations created or updated")
                .register(meterRegistry);
        this.locationDeletionCounter = Counter.builder("locations_deleted_total")
                .description("Total number of locations deleted")
                .register(meterRegistry);
    }

    @Transactional(readOnly = true)
    public List<Location> findAllLocations() {
        return locationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Location> findAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Location> findLocationById(Long id) {
        return locationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Location> findLocationByName(String name) {
        return locationRepository.findByName(name);
    }

    @Transactional
    public Location saveLocation(Location location) {
        Location savedLocation = locationRepository.save(location);
        locationCreationCounter.increment();
        return savedLocation;
    }

    @Transactional
    public Location updateLocation(Long id, Location locationDetails) {
        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Locația nu a fost găsită cu id: " + id));

        existingLocation.setName(locationDetails.getName());
        existingLocation.setAddress(locationDetails.getAddress());
        existingLocation.setDescription(locationDetails.getDescription());

        return locationRepository.save(existingLocation);
    }

    @Transactional
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new IllegalArgumentException("Locația nu a fost găsită cu id: " + id);
        }
        locationRepository.deleteById(id);
        locationDeletionCounter.increment();
    }
}
