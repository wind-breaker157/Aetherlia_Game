package com.aetherlia.controller;

import com.aetherlia.entity.PlayerCharacter;
import com.aetherlia.entity.PlayerPosition;

import com.aetherlia.service.PlayerCharacterService;
import com.aetherlia.service.PlayerPositionService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GameController {

    private final PlayerCharacterService playerCharacterService;
    private final PlayerPositionService playerPositionService;

    public GameController(
            PlayerCharacterService playerCharacterService,
            PlayerPositionService playerPositionService) {

        this.playerCharacterService =
                playerCharacterService;

        this.playerPositionService =
                playerPositionService;
    }

    @GetMapping("/game")
    public String game(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();

        PlayerCharacter character =
                playerCharacterService
                        .getOrCreateCharacter(username);

        PlayerPosition position =
                playerPositionService
                        .getOrCreatePosition(username);

        model.addAttribute(
                "character",
                character
        );

        model.addAttribute(
                "position",
                position
        );

        return "game";
    }
}