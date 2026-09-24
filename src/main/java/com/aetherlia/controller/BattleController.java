package com.aetherlia.controller;

import com.aetherlia.battle.BattleResult;
import com.aetherlia.battle.BattleSkill;
import com.aetherlia.battle.BattleStartData;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.WildEncounter;

import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.WildEncounterRepository;

import com.aetherlia.service.BattleService;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.aetherlia.battle.CaptureResult;
import com.aetherlia.service.CaptureService;
import com.aetherlia.service.StatService;

import java.util.List;

@Controller
public class BattleController {

	private final BattleService battleService;
	private final PlayerMonsterRepository playerMonsterRepository;
	private final WildEncounterRepository wildEncounterRepository;
	private final CaptureService captureService;
	private final StatService statService;

	public BattleController(BattleService battleService, PlayerMonsterRepository playerMonsterRepository,
			WildEncounterRepository wildEncounterRepository, CaptureService captureService, StatService statService) {

		this.battleService = battleService;

		this.playerMonsterRepository = playerMonsterRepository;

		this.wildEncounterRepository = wildEncounterRepository;

		this.captureService = captureService;

		this.statService = statService;
	}

	// ==========================================
	// MỞ BATTLE
	// ==========================================

	@GetMapping("/battle")
	public String battle(Authentication authentication, HttpSession session, Model model) {

		String username = authentication.getName();

		Long playerMonsterId = (Long) session.getAttribute("battlePlayerMonsterId");

		Long wildEncounterId = (Long) session.getAttribute("battleWildEncounterId");

		// Nếu chưa có Battle Session
		if (playerMonsterId == null || wildEncounterId == null) {

			BattleStartData start = battleService.prepareBattle(username);

			playerMonsterId = start.getPlayerMonsterId();

			wildEncounterId = start.getWildEncounterId();

			session.setAttribute("battlePlayerMonsterId", playerMonsterId);

			session.setAttribute("battleWildEncounterId", wildEncounterId);
		}

		PlayerMonster playerMonster = playerMonsterRepository.findById(playerMonsterId).orElseThrow();

		WildEncounter encounter = wildEncounterRepository.findById(wildEncounterId).orElseThrow();
		int playerMaxHp = statService.calculatePlayerMaxHp(playerMonster);

		int wildMaxHp = statService.calculateWildMaxHp(encounter.getMonster(), encounter.getLevel());

		model.addAttribute("playerMaxHp", playerMaxHp);

		model.addAttribute("wildMaxHp", wildMaxHp);

		List<BattleSkill> skills = battleService.getAvailableSkills();

		String message = (String) session.getAttribute("battleMessage");

		session.removeAttribute("battleMessage");

		Boolean victory = (Boolean) session.getAttribute("battleVictory");

		Boolean defeat = (Boolean) session.getAttribute("battleDefeat");

		String captureMessage = (String) session.getAttribute("battleCaptureMessage");

		Boolean captureSuccess = (Boolean) session.getAttribute("battleCaptureSuccess");

		session.removeAttribute("battleCaptureMessage");

		session.removeAttribute("battleCaptureSuccess");

		if (captureSuccess == null) {
			captureSuccess = false;
		}

		if (victory == null) {
			victory = false;
		}

		if (defeat == null) {
			defeat = false;
		}

		model.addAttribute("playerMonster", playerMonster);

		model.addAttribute("encounter", encounter);

		model.addAttribute("skills", skills);

		model.addAttribute("message", message);

		model.addAttribute("victory", victory);

		model.addAttribute("defeat", defeat);

		model.addAttribute("captureMessage", captureMessage);

		model.addAttribute("captureSuccess", captureSuccess);

		return "battle";
	}

	// ==========================================
	// PLAYER DÙNG SKILL
	// ==========================================

	@PostMapping("/battle/attack")
	public String attack(@RequestParam String skillId, Authentication authentication, HttpSession session) {

		String username = authentication.getName();

		Long playerMonsterId = (Long) session.getAttribute("battlePlayerMonsterId");

		Long wildEncounterId = (Long) session.getAttribute("battleWildEncounterId");

		if (playerMonsterId == null || wildEncounterId == null) {

			return "redirect:/battle";
		}

		BattleResult result = battleService.playerAttack(username, playerMonsterId, wildEncounterId, skillId);

		session.setAttribute("battleMessage", result.getMessage());

		session.setAttribute("battleVictory", result.isVictory());

		session.setAttribute("battleDefeat", result.isDefeat());

		return "redirect:/battle";
	}

	@PostMapping("/battle/capture")
	public String capture(Authentication authentication, HttpSession session) {

		String username = authentication.getName();

		Long wildEncounterId = (Long) session.getAttribute("battleWildEncounterId");

		if (wildEncounterId == null) {
			return "redirect:/battle";
		}

		CaptureResult result = captureService.capture(username, wildEncounterId, "BASIC_ORB");

		session.setAttribute("battleCaptureMessage", result.getMessage());

		session.setAttribute("battleCaptureSuccess", result.isSuccess());

		return "redirect:/battle";
	}

	@GetMapping("/battle/leave")
	public String leaveBattle(HttpSession session) {

		// Xóa thông tin Battle cũ
		session.removeAttribute("battlePlayerMonsterId");
		session.removeAttribute("battleWildEncounterId");

		// Xóa kết quả Battle cũ
		session.removeAttribute("battleMessage");
		session.removeAttribute("battleVictory");
		session.removeAttribute("battleDefeat");

		// Xóa kết quả Capture cũ
		session.removeAttribute("battleCaptureMessage");
		session.removeAttribute("battleCaptureSuccess");

		return "redirect:/map/1";
	}
}