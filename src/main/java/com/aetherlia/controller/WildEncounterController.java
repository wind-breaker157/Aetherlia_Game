package com.aetherlia.controller;

import com.aetherlia.entity.WildEncounter;
import com.aetherlia.service.WildEncounterService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WildEncounterController {

    private final WildEncounterService wildEncounterService;

    public WildEncounterController(
            WildEncounterService wildEncounterService) {

        this.wildEncounterService =
                wildEncounterService;
    }


    @GetMapping("/explore")
    public String explore(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();

        WildEncounter encounter =
                wildEncounterService
                        .createEncounter(username);

        model.addAttribute(
                "encounter",
                encounter
        );

        return "wild-encounter";
    }
}