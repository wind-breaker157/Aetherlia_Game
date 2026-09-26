package com.aetherlia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aetherlia.game.EncounterCheckResult;
import com.aetherlia.service.RandomEncounterService;


@RestController
public class RandomEncounterController {

    private final RandomEncounterService randomEncounterService;


    public RandomEncounterController(
            RandomEncounterService randomEncounterService) {

        this.randomEncounterService =
                randomEncounterService;
    }


    @PostMapping("/encounter/check")
    public EncounterCheckResult checkEncounter(
            Authentication authentication) {

        String username =
                authentication.getName();


        return randomEncounterService
                .checkEncounter(username);
    }
}