package com.aetherlia.controller;

import com.aetherlia.entity.PlayerCharacter;
import com.aetherlia.entity.PlayerPosition;

import com.aetherlia.service.PlayerCharacterService;
import com.aetherlia.service.PlayerPositionService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PlayerMovementController {

	private final PlayerPositionService playerPositionService;
	private final PlayerCharacterService playerCharacterService;

	public PlayerMovementController(PlayerPositionService playerPositionService,
			PlayerCharacterService playerCharacterService) {

		this.playerPositionService = playerPositionService;

		this.playerCharacterService = playerCharacterService;
	}

	@PostMapping("/player/move")
	@ResponseBody
	public Map<String, Object> move(@RequestParam String direction, Authentication authentication) {

		String username = authentication.getName();

		PlayerPosition position = playerPositionService.moveCharacter(username, direction);

		playerCharacterService.updateFacing(username, direction);

		PlayerCharacter character = playerCharacterService.getOrCreateCharacter(username);

		Map<String, Object> response = new HashMap<>();

		response.put("x", position.getX());

		response.put("y", position.getY());

		response.put("facing", character.getFacing());

		return response;
	}
}