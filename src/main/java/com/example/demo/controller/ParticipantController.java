package com.example.demo.controller;

import com.example.demo.domain.Participant;
import com.example.demo.service.ParticipantService;
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
@RequestMapping("/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    @Autowired
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping
    public String listParticipants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Participant> participantsPage = participantService.findAllParticipants(pageable);

        model.addAttribute("participantsPage", participantsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", participantsPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "participants/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("participant", new Participant());
        return "participants/form";
    }

    @PostMapping
    public String saveParticipant(@Valid @ModelAttribute("participant") Participant participant,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "participants/form";
        }

        if (participant.getId() == null || participantService.findParticipantById(participant.getId()).isEmpty()) {
            Optional<Participant> existingEmail = participantService.findParticipantByName(participant.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getId().equals(participant.getId())) {
                bindingResult.rejectValue("email", "error.participant", "Acest email este deja înregistrat.");
                return "participants/form";
            }
        }

        participantService.saveParticipant(participant);
        redirectAttributes.addFlashAttribute("message", "Participant salvat cu succes!");
        return "redirect:/participants";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Participant> participant = participantService.findParticipantById(id);
        if (participant.isPresent()) {
            model.addAttribute("participant", participant.get());
            return "participants/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Participantul nu a fost găsit!");
            return "redirect:/participants";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteParticipant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            participantService.deleteParticipant(id);
            redirectAttributes.addFlashAttribute("message", "Participant șters cu succes!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/participants";
    }
}
