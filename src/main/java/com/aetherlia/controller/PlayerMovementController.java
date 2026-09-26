package com.aetherlia.controller;

import com.aetherlia.entity.PlayerCharacter;
import com.aetherlia.game.MovementResult;

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

		/*
		 * Backend xử lý Movement + Collision.
		 */
		MovementResult result = playerPositionService.moveCharacter(username, direction);

		/*
		 * Lưu hướng nhìn.
		 */
		playerCharacterService.updateFacing(username, direction);

		/*
		 * Response về Browser.
		 */
		Map<String, Object> response = new HashMap<>();

		response.put("x", result.getX());

		response.put("y", result.getY());

		response.put("facing", result.getFacing());

		response.put("blocked", result.isBlocked());

		return response;
	}
}