package com.aetherlia.controller;

import com.aetherlia.entity.Monster;
import com.aetherlia.service.MonsterService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MonsterController {

    private final MonsterService monsterService;

    public MonsterController(MonsterService monsterService) {
        this.monsterService = monsterService;
    }

    @GetMapping("/monsters")
    public String monsters(Model model) {

        List<Monster> monsters = monsterService.getAllMonsters();

        model.addAttribute("monsters", monsters);

        return "monsters";
    }
}