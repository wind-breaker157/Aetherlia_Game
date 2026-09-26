package com.aetherlia.service;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.User;

import com.aetherlia.repository.MonsterRepository;
import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerMonsterService {

	private final PlayerMonsterRepository playerMonsterRepository;
	private final UserRepository userRepository;
	private final MonsterRepository monsterRepository;
	private final StatService statService;

	public PlayerMonsterService(PlayerMonsterRepository playerMonsterRepository, UserRepository userRepository,
			MonsterRepository monsterRepository, StatService statService) {

		this.playerMonsterRepository = playerMonsterRepository;
		this.userRepository = userRepository;
		this.monsterRepository = monsterRepository;
		this.statService = statService;
	}

	// Lấy toàn bộ Monster của người chơi
	public List<PlayerMonster> getMyMonsters(String username) {

		User user = userRepository.findByUsername(username);

		return playerMonsterRepository.findByUser(user);
	}

	// Thêm Monster khởi đầu
	public PlayerMonster addStarterMonster(String username, Long monsterId) {

		User user = userRepository.findByUsername(username);

		// Người chơi chỉ được chọn Monster khởi đầu một lần
		if (playerMonsterRepository.existsByUser(user)) {

			throw new RuntimeException("Người chơi đã có Monster khởi đầu.");
		}

		// Tìm Monster
		Monster monster = monsterRepository.findById(monsterId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Monster."));

		// Tạo PlayerMonster
		PlayerMonster playerMonster = new PlayerMonster();

		playerMonster.setUser(user);

		playerMonster.setMonster(monster);

		playerMonster.setLevel(1);

		playerMonster.setExp(0);

		int maxHp = statService.calculateMaxHp(monster.getBaseHp(), 1);

		playerMonster.setCurrentHp(maxHp);

		// Lưu vào database
		return playerMonsterRepository.save(playerMonster);
	}
}