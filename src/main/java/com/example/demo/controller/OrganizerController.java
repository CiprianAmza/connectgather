package com.example.demo.controller;

import com.example.demo.domain.Organizer;
import com.example.demo.service.OrganizerService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/organizers")
public class OrganizerController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizerController.class);

    private final OrganizerService organizerService;

    @Autowired
    public OrganizerController(OrganizerService organizerService) {
        this.organizerService = organizerService;
    }

    @GetMapping
    public String listOrganizers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        logger.info("Request to list organizers. Page: {}, Size: {}, SortBy: {}, SortDir: {}", page, size, sortBy, sortDir);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Organizer> organizersPage = organizerService.findAllOrganizers(pageable);

        model.addAttribute("organizersPage", organizersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", organizersPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "organizers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        logger.info("Displaying create new organizer form.");
        model.addAttribute("organizer", new Organizer());
        return "organizers/form";
    }

    @PostMapping
    public String saveOrganizer(@Valid @ModelAttribute("organizer") Organizer organizer,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        logger.info("Attempting to save organizer: {}", organizer.getName());

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred for organizer {}: {}", organizer.getName(), bindingResult.getAllErrors());
            return "organizers/form";
        }

        // Verificăm dacă email-ul există deja pentru un alt organizator (doar la creare sau update)
        if (organizer.getId() == null || organizerService.findOrganizerById(organizer.getId()).isEmpty()) {
            Optional<Organizer> existingEmail = organizerService.findOrganizerByEmail(organizer.getEmail());
            if (existingEmail.isPresent() && (organizer.getId() == null || !existingEmail.get().getId().equals(organizer.getId()))) {
                bindingResult.rejectValue("email", "error.organizer", "Acest email este deja înregistrat pentru un alt organizator.");
                logger.warn("Attempted to save organizer with duplicate email: {}", organizer.getEmail());
                return "organizers/form";
            }
        }

        organizerService.saveOrganizer(organizer);
        redirectAttributes.addFlashAttribute("message", "Organizator salvat cu succes!");
        logger.info("Organizer {} saved successfully, redirecting to /organizers.", organizer.getName());
        return "redirect:/organizers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        logger.info("Displaying edit form for organizer ID: {}", id);
        Optional<Organizer> organizer = organizerService.findOrganizerById(id);
        if (organizer.isPresent()) {
            model.addAttribute("organizer", organizer.get());
            return "organizers/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Organizatorul nu a fost găsit!");
            logger.warn("Attempted to edit non-existent organizer with ID: {}", id);
            return "redirect:/organizers";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteOrganizer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Request to delete organizer with ID: {}", id);
        try {
            organizerService.deleteOrganizer(id);
            redirectAttributes.addFlashAttribute("message", "Organizator șters cu succes!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            logger.error("Error deleting organizer with ID {}: {}", id, e.getMessage());
        }
        return "redirect:/organizers";
    }
}
