package com.aetherlia.service;

import com.aetherlia.battle.CaptureResult;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;

import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class CaptureService {

	private final WildEncounterRepository wildEncounterRepository;
	private final PlayerMonsterRepository playerMonsterRepository;
	private final UserRepository userRepository;
	private final StatService statService;
	private final InventoryService inventoryService;
	private final TeamService teamService;

	private final Random random = new Random();

	public CaptureService(WildEncounterRepository wildEncounterRepository,
			PlayerMonsterRepository playerMonsterRepository, UserRepository userRepository,
			InventoryService inventoryService, TeamService teamService, StatService statService) {

		this.wildEncounterRepository = wildEncounterRepository;

		this.playerMonsterRepository = playerMonsterRepository;

		this.userRepository = userRepository;

		this.inventoryService = inventoryService;

		this.teamService = teamService;

		this.statService = statService;
	}

	@Transactional
	public CaptureResult capture(String username, Long wildEncounterId, String orbType) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		WildEncounter encounter = wildEncounterRepository.findById(wildEncounterId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Monster hoang dã."));

		if (!encounter.getUser().getId().equals(user.getId())) {

			throw new RuntimeException("Battle không thuộc tài khoản.");
		}

		if (!encounter.isActive()) {

			return new CaptureResult("Battle đã kết thúc.", false, 0);
		}

		Monster monster = encounter.getMonster();

		if ("LEGENDARY".equalsIgnoreCase(monster.getRarity())) {

			return new CaptureResult("Monster Legendary này chỉ có thể thu phục thông qua Quest Legendary.", false, 0);
		}

		/*
		 * Kiểm tra Orb
		 */
		boolean consumed = inventoryService.consumeItem(username, orbType, 1);

		if (!consumed) {

			return new CaptureResult("Anh không còn Capture Orb phù hợp.", false, 0);
		}

		/*
		 * 1. Tìm level cao nhất của Team
		 */
		List<TeamMember> members = teamService.getTeamMembers(username);

		int bestLevel = 1;

		for (TeamMember member : members) {

			if (member.getPlayerMonster() != null) {

				bestLevel = Math.max(bestLevel, member.getPlayerMonster().getLevel());
			}
		}

		/*
		 * 2. maxHP / currentHP
		 */
		double maxHp = monster.getBaseHp();

		double currentHp = encounter.getCurrentHp();

		/*
		 * 3. HP factor
		 *
		 * ((3*maxHP - 2*curHP) / (3*maxHP))
		 */
		double hpFactor = ((3 * maxHp) - (2 * currentHp)) / (3 * maxHp);

		/*
		 * 4. Base catch theo rarity
		 */
		double baseCatch = getBaseCatch(monster.getRarity());

		/*
		 * 5. Basic Orb
		 */
		double orbMultiplier = 1.0;

		/*
		 * 6. Chưa có status system
		 */
		double statusMultiplier = 1.0;

		/*
		 * 7. Level modifier
		 */
		double levelModifier = 1.0 - ((encounter.getLevel() - bestLevel) * 0.02);

		levelModifier = Math.max(0.5, levelModifier);

		/*
		 * 8. Tính xác suất
		 */
		double chance = hpFactor * baseCatch * orbMultiplier * statusMultiplier * levelModifier;

		/*
		 * Giới hạn probability
		 */
		chance = Math.max(0.0, Math.min(1.0, chance));

		/*
		 * 9. Cơ chế 3 lần lắc
		 */
		boolean success = random.nextDouble() < chance;

		if (!success) {

			int shakes = 1 + random.nextInt(3);

			wildEncounterRepository.save(encounter);

			int percent = (int) Math.round(chance * 100);

			return new CaptureResult("Capture Orb rung " + shakes + " lần nhưng Monster đã thoát! "
					+ "Tỷ lệ bắt khoảng " + percent + "%.", false, shakes);
		}

		/*
		 * 10. Tạo PlayerMonster mới
		 */
		PlayerMonster playerMonster = new PlayerMonster();

		playerMonster.setUser(user);

		playerMonster.setMonster(monster);

		playerMonster.setLevel(encounter.getLevel());

		playerMonster.setExp(0);

		int maxHp1 =
		        statService.calculateMaxHp(
		                monster.getBaseHp(),
		                encounter.getLevel()
		        );

		playerMonster.setCurrentHp(maxHp1);

		playerMonster = playerMonsterRepository.save(playerMonster);

		/*
		 * 11. Kết thúc encounter
		 */
		encounter.setActive(false);

		wildEncounterRepository.save(encounter);

		/*
		 * 12. Nếu Team còn chỗ -> tự thêm
		 */
		List<TeamMember> currentTeam = teamService.getTeamMembers(username);

		if (currentTeam.size() < 6) {

			teamService.addToTeam(username, playerMonster.getId());

			return new CaptureResult("🎉 Thu phục thành công " + monster.getName() + " Lv." + encounter.getLevel()
					+ "! Monster đã được thêm vào Team.", true, 3);
		}

		/*
		 * Team đầy -> lưu vào collection/box
		 *
		 * Hiện tại player_monsters chính là collection.
		 */
		return new CaptureResult("🎉 Thu phục thành công " + monster.getName() + " Lv." + encounter.getLevel()
				+ "! Team đã đủ 6, Monster được lưu vào kho.", true, 3);
	}

	private double getBaseCatch(String rarity) {

		if (rarity == null) {
			return 1.0;
		}

		switch (rarity.toUpperCase()) {

		case "COMMON":
			return 1.0;

		case "UNCOMMON":
			return 0.7;

		case "RARE":
			return 0.4;

		case "EPIC":
			return 0.2;

		case "LEGENDARY":
			return 0.0;

		default:
			return 1.0;
		}
	}
}