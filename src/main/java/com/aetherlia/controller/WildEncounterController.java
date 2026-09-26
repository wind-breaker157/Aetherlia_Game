package com.aetherlia.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.aetherlia.entity.WildEncounter;
import com.aetherlia.service.WildEncounterService;

@Controller
public class WildEncounterController {

    private final WildEncounterService wildEncounterService;

    public WildEncounterController(
            WildEncounterService wildEncounterService) {

        this.wildEncounterService = wildEncounterService;
    }

    @GetMapping("/explore")
    public String explore(
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        /*
         * createEncounter() sẽ:
         *
         * 1. Kiểm tra xem đã có WildEncounter active hay chưa.
         * 2. Nếu có -> trả lại encounter cũ.
         * 3. Nếu chưa có -> kiểm tra Route Cỏ Cao.
         * 4. Nếu hợp lệ -> tạo Monster mới.
         */
        WildEncounter encounter =
                wildEncounterService.createEncounter(username);

        model.addAttribute("encounter", encounter);

        return "wild-encounter";
    }
}