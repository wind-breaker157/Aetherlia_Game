package com.aetherlia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.aetherlia.entity.PlayerCharacter;
import com.aetherlia.entity.PlayerPosition;
import com.aetherlia.service.PlayerCharacterService;
import com.aetherlia.service.PlayerPositionService;

@Controller
public class CharacterController {

    private final PlayerCharacterService playerCharacterService;
    private final PlayerPositionService playerPositionService;

    public CharacterController(
            PlayerCharacterService playerCharacterService,
            PlayerPositionService playerPositionService) {

        this.playerCharacterService = playerCharacterService;
        this.playerPositionService = playerPositionService;
    }

    @GetMapping("/character")
    public String character(
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        /*
         * Lấy hoặc tạo Character.
         */
        PlayerCharacter character =
                playerCharacterService.getOrCreateCharacter(
                        username
                );

        /*
         * Lấy vị trí hiện tại.
         */
        PlayerPosition position =
                playerPositionService.getOrCreatePosition(
                        username
                );

        /*
         * Đưa dữ liệu sang Thymeleaf.
         */
        model.addAttribute(
                "character",
                character
        );

        model.addAttribute(
                "position",
                position
        );

        return "character";
    }
}