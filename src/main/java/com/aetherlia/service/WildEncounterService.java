package com.aetherlia.service;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerPosition;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;

import com.aetherlia.repository.MonsterRepository;
import com.aetherlia.repository.PlayerPositionRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class WildEncounterService {

	private final WildEncounterRepository wildEncounterRepository;
	private final MonsterRepository monsterRepository;
	private final UserRepository userRepository;
	private final PlayerPositionRepository playerPositionRepository;
	private final StatService statService;
	private final Random random = new Random();

	public WildEncounterService(WildEncounterRepository wildEncounterRepository, MonsterRepository monsterRepository,
			UserRepository userRepository, PlayerPositionRepository playerPositionRepository, StatService statService) {

		this.wildEncounterRepository = wildEncounterRepository;

		this.monsterRepository = monsterRepository;

		this.userRepository = userRepository;

		this.playerPositionRepository = playerPositionRepository;

		this.statService = statService;
	}

	// ==========================================
	// TẠO MONSTER HOANG DÃ
	// ==========================================

	@Transactional
	public WildEncounter createEncounter(String username) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		// ==========================================
		// Kiểm tra vị trí người chơi
		// ==========================================

		PlayerPosition position = playerPositionRepository.findByUser(user);

		if (position == null) {
			throw new RuntimeException("Chưa xác định vị trí người chơi.");
		}

		// Chỉ gặp Monster ở Route Cỏ Cao
		if (!"route_grass".equals(position.getLocationCode())) {

			throw new RuntimeException("Anh phải đi tới Route Cỏ Cao trước.");
		}

		// ==========================================
		// Kiểm tra Encounter hiện tại
		// ==========================================

		WildEncounter oldEncounter = wildEncounterRepository.findByUserAndActiveTrue(user).orElse(null);

		if (oldEncounter != null) {
			return oldEncounter;
		}

		// ==========================================
		// Lấy Monster
		// ==========================================

		List<Monster> monsters = monsterRepository.findAll();

		if (monsters.isEmpty()) {
			throw new RuntimeException("Chưa có Monster trong database.");
		}

		// Chọn Monster ngẫu nhiên
		Monster monster = monsters.get(random.nextInt(monsters.size()));

		// Level hoang dã: 2 -> 5
		int level = random.nextInt(4) + 2;

		// ==========================================
		// Tạo Encounter
		// ==========================================

		WildEncounter encounter = new WildEncounter();

		encounter.setUser(user);

		encounter.setMonster(monster);

		encounter.setLevel(level);

		int maxHp = statService.calculateMaxHp(monster.getBaseHp(), level);

		encounter.setCurrentHp(maxHp);

		encounter.setActive(true);

		return wildEncounterRepository.save(encounter);
	}

	// ==========================================
	// LẤY ENCOUNTER ĐANG HOẠT ĐỘNG
	// ==========================================

	public WildEncounter getActiveEncounter(String username) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		return wildEncounterRepository.findByUserAndActiveTrue(user).orElse(null);
	}
}