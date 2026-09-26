package com.aetherlia.controller;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.service.PlayerMonsterService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PlayerMonsterController {

    private final PlayerMonsterService playerMonsterService;

    public PlayerMonsterController(
            PlayerMonsterService playerMonsterService) {

        this.playerMonsterService = playerMonsterService;
    }

    @GetMapping("/my-monsters")
    public String myMonsters(
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        List<PlayerMonster> playerMonsters =
                playerMonsterService.getMyMonsters(username);

        model.addAttribute("playerMonsters", playerMonsters);

        return "my-monsters";
    }
}