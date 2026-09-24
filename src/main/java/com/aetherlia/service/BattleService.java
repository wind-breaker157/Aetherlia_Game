package com.aetherlia.service;

import com.aetherlia.battle.BattleResult;
import com.aetherlia.battle.BattleSkill;
import com.aetherlia.battle.BattleStartData;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;

import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BattleService {

	private final PlayerMonsterRepository playerMonsterRepository;
	private final WildEncounterRepository wildEncounterRepository;
	private final UserRepository userRepository;
	private final TeamService teamService;
	private final ProgressionService progressionService;
	private final StatService statService;

	private final Random random = new Random();

	public BattleService(PlayerMonsterRepository playerMonsterRepository,
			WildEncounterRepository wildEncounterRepository, UserRepository userRepository, TeamService teamService,
			ProgressionService progressionService, StatService statService) {

		this.playerMonsterRepository = playerMonsterRepository;

		this.wildEncounterRepository = wildEncounterRepository;

		this.userRepository = userRepository;

		this.teamService = teamService;

		this.progressionService = progressionService;

		this.statService = statService;
	}

	// =====================================================
	// BẮT ĐẦU BATTLE
	// =====================================================

	public BattleStartData prepareBattle(String username) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		// Lấy Encounter đang hoạt động
		WildEncounter encounter = wildEncounterRepository.findByUserAndActiveTrue(user)
				.orElseThrow(() -> new RuntimeException("Không có Monster hoang dã."));

		// Lấy Team
		List<TeamMember> members = teamService.getTeamMembers(username);

		if (members.isEmpty()) {
			throw new RuntimeException("Team chưa có Monster.");
		}

		// Monster ở vị trí 1 chiến đấu trước
		PlayerMonster playerMonster = members.get(0).getPlayerMonster();
		int maxHp = statService.calculatePlayerMaxHp(playerMonster);

		/*
		 * Nếu Monster đã bị hạ từ Battle trước thì hồi đầy HP khi bắt đầu Battle mới.
		 */
		if (playerMonster.getCurrentHp() <= 0) {

			playerMonster.setCurrentHp(maxHp);

			playerMonsterRepository.save(playerMonster);
		}

		return new BattleStartData(playerMonster.getId(), encounter.getId());
	}

	// =====================================================
	// LẤY 4 SKILL
	// =====================================================

	public List<BattleSkill> getAvailableSkills() {

		List<BattleSkill> skills = new ArrayList<>();

		skills.add(new BattleSkill("basic_attack", "Basic Attack", "NORMAL", 40, 100));

		skills.add(new BattleSkill("fire_burst", "Fire Burst", "FIRE", 40, 100));

		skills.add(new BattleSkill("water_shot", "Water Shot", "WATER", 40, 100));

		skills.add(new BattleSkill("nature_strike", "Nature Strike", "GRASS", 40, 100));

		return skills;
	}

	// =====================================================
	// TÌM SKILL
	// =====================================================

	private BattleSkill findSkill(String skillId) {

		for (BattleSkill skill : getAvailableSkills()) {

			if (skill.getId().equals(skillId)) {
				return skill;
			}
		}

		throw new RuntimeException("Không tìm thấy Skill.");
	}

	// =====================================================
	// PLAYER TẤN CÔNG
	// =====================================================

	@Transactional
	public BattleResult playerAttack(String username, Long playerMonsterId, Long wildEncounterId, String skillId) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		// Kiểm tra Monster thuộc User
		PlayerMonster playerMonster = playerMonsterRepository.findByIdAndUser(playerMonsterId, user)
				.orElseThrow(() -> new RuntimeException("Monster không thuộc tài khoản."));

		// Lấy Encounter
		WildEncounter encounter = wildEncounterRepository.findById(wildEncounterId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Battle."));

		// Kiểm tra Encounter thuộc User
		if (!encounter.getUser().getId().equals(user.getId())) {

			throw new RuntimeException("Battle không thuộc tài khoản.");
		}

		if (!encounter.isActive()) {

			return new BattleResult("Battle đã kết thúc.", false, false);
		}

		BattleSkill skill = findSkill(skillId);

		// =================================================
		// PLAYER GÂY DAMAGE
		// =================================================

		int playerDamage = calculateDamage(playerMonster.getLevel(), playerMonster.getMonster().getBaseAttack(),
				encounter.getMonster().getBaseDefense(), skill.getPower(), skill.getType(),
				playerMonster.getMonster().getType(), encounter.getMonster().getType());

		int newEnemyHp = Math.max(0, encounter.getCurrentHp() - playerDamage);

		encounter.setCurrentHp(newEnemyHp);

		// =================================================
		// ENEMY BỊ HẠ
		// =================================================

		if (newEnemyHp <= 0) {

			encounter.setActive(false);

			wildEncounterRepository.save(encounter);

			String expMessage = progressionService.grantWildExp(playerMonster, encounter);

			playerMonsterRepository.save(playerMonster);

			return new BattleResult(
					"Anh đã đánh bại Monster hoang dã! " + "Damage: " + playerDamage + ". " + expMessage, true, false);
		}

		// =================================================
		// ENEMY PHẢN CÔNG
		// =================================================

		int enemyDamage = calculateDamage(encounter.getLevel(), encounter.getMonster().getBaseAttack(),
				playerMonster.getMonster().getBaseDefense(), 35, encounter.getMonster().getType(),
				encounter.getMonster().getType(), playerMonster.getMonster().getType());

		int newPlayerHp = Math.max(0, playerMonster.getCurrentHp() - enemyDamage);

		playerMonster.setCurrentHp(newPlayerHp);

		// =================================================
		// PLAYER BỊ HẠ
		// =================================================

		if (newPlayerHp <= 0) {

			encounter.setActive(false);

			wildEncounterRepository.save(encounter);

			playerMonsterRepository.save(playerMonster);

			return new BattleResult("Monster của anh đã bị hạ. Damage nhận: " + enemyDamage, false, true);
		}

		playerMonsterRepository.save(playerMonster);

		wildEncounterRepository.save(encounter);

		return new BattleResult("Anh gây " + playerDamage + " damage. " + encounter.getMonster().getName()
				+ " phản công gây " + enemyDamage + " damage.", false, false);
	}

	// =====================================================
	// DAMAGE FORMULA
	// =====================================================

	private int calculateDamage(int level, int attack, int defense, int power, String attackType,
			String attackerMonsterType, String defenderType) {

		double base = (((2.0 * level / 5.0) + 2) * power * (attack / (double) defense) / 50.0) + 2;

		/*
		 * STAB:
		 *
		 * Skill Type == Type của Monster tấn công
		 */
		double stab = 1.0;

		if (normalizeType(attackType).equals(normalizeType(attackerMonsterType))) {

			stab = 1.5;
		}

		double typeMultiplier = getTypeMultiplier(attackType, defenderType);

		double critical = 1.0;

		if (random.nextInt(100) < 6) {

			critical = 1.5;
		}

		double randomMultiplier = 0.85 + (random.nextDouble() * 0.15);

		double damage = base * stab * typeMultiplier * critical * randomMultiplier;

		return Math.max(1, (int) damage);
	}

	// =====================================================
	// TYPE
	// =====================================================

	private String normalizeType(String type) {

		if (type == null) {
			return "NORMAL";
		}

		if (type.equalsIgnoreCase("GRASS")) {
			return "NATURE";
		}

		return type.toUpperCase();
	}

	private double getTypeMultiplier(String attackType, String defenderType) {

		String attack = normalizeType(attackType);

		String defender = normalizeType(defenderType);

		// Fire > Nature
		if (attack.equals("FIRE") && defender.equals("NATURE")) {

			return 2.0;
		}

		// Water > Fire
		if (attack.equals("WATER") && defender.equals("FIRE")) {

			return 2.0;
		}

		// Nature > Water
		if (attack.equals("NATURE") && defender.equals("WATER")) {

			return 2.0;
		}

		// Fire < Water
		if (attack.equals("FIRE") && defender.equals("WATER")) {

			return 0.5;
		}

		// Water < Nature
		if (attack.equals("WATER") && defender.equals("NATURE")) {

			return 0.5;
		}

		// Nature < Fire
		if (attack.equals("NATURE") && defender.equals("FIRE")) {

			return 0.5;
		}

		return 1.0;
	}
}