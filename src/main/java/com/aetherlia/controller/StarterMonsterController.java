package com.aetherlia.controller;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.service.InventoryService;
import com.aetherlia.service.MonsterService;
import com.aetherlia.service.PlayerMonsterService;
import com.aetherlia.service.TeamService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StarterMonsterController {

	private final MonsterService monsterService;
	private final PlayerMonsterService playerMonsterService;
	private final TeamService teamService;
	private final InventoryService inventoryService;

	public StarterMonsterController(MonsterService monsterService, PlayerMonsterService playerMonsterService,
			TeamService teamService, InventoryService inventoryService) {

		this.monsterService = monsterService;
		this.playerMonsterService = playerMonsterService;
		this.teamService = teamService;
		this.inventoryService = inventoryService;
	}

	@GetMapping("/starter-monsters")
	public String showStarterMonsters(Model model) {

		List<Monster> monsters = monsterService.getAllMonsters();

		model.addAttribute("monsters", monsters);

		return "starter-monsters";
	}

	@PostMapping("/choose-starter")
	public String chooseStarter(@RequestParam Long monsterId, Authentication authentication) {

		String username = authentication.getName();

		// 1. Tạo PlayerMonster
		PlayerMonster playerMonster = playerMonsterService.addStarterMonster(username, monsterId);

		// 2. Tự động thêm vào Team
		teamService.addToTeam(username, playerMonster.getId());

		inventoryService.addItem(username, "BASIC_ORB", 5);

		// 3. Chuyển đến Monster của tôi
		return "redirect:/my-monsters";
	}
}