package com.example.demo.controller;

import com.example.demo.domain.Location;
import com.example.demo.service.LocationService;
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

import java.util.Optional;

@Controller
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // Listarea locațiilor cu paginare și sortare
    @GetMapping
    public String listLocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Location> locationsPage = locationService.findAllLocations(pageable);

        model.addAttribute("locationsPage", locationsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", locationsPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "locations/list"; // Va căuta src/main/resources/templates/locations/list.html
    }

    // Afișează formularul pentru a adăuga o locație nouă
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("location", new Location());
        return "locations/form"; // Va căuta src/main/resources/templates/locations/form.html
    }

    // Procesează adăugarea/editarea unei locații
    @PostMapping
    public String saveLocation(@Valid @ModelAttribute("location") Location location,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "locations/form";
        }

        // Verifică unicitatea numelui la crearea unei locații noi
        if (location.getId() == null || locationService.findLocationById(location.getId()).isEmpty()) {
            Optional<Location> existingLocationByName = locationService.findLocationByName(location.getName());
            if (existingLocationByName.isPresent() && !existingLocationByName.get().getId().equals(location.getId())) {
                bindingResult.rejectValue("name", "error.location", "O locație cu acest nume există deja.");
                return "locations/form";
            }
        }

        locationService.saveLocation(location);
        redirectAttributes.addFlashAttribute("message", "Locație salvată cu succes!");
        return "redirect:/locations";
    }

    // Afișează formularul pentru a edita o locație existentă
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Location> location = locationService.findLocationById(id);
        if (location.isPresent()) {
            model.addAttribute("location", location.get());
            return "locations/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Locația nu a fost găsită!");
            return "redirect:/locations";
        }
    }

    // Șterge o locație
    @GetMapping("/delete/{id}")
    public String deleteLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            locationService.deleteLocation(id);
            redirectAttributes.addFlashAttribute("message", "Locație ștearsă cu succes!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/locations";
    }
}
