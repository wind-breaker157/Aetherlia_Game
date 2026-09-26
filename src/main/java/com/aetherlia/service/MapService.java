package com.aetherlia.service;

import com.aetherlia.entity.MapArea;
import com.aetherlia.entity.PlayerMap;
import com.aetherlia.entity.User;

import com.aetherlia.repository.MapAreaRepository;
import com.aetherlia.repository.PlayerMapRepository;
import com.aetherlia.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MapService {

	private final MapAreaRepository mapAreaRepository;
	private final PlayerMapRepository playerMapRepository;
	private final UserRepository userRepository;

	public MapService(MapAreaRepository mapAreaRepository, PlayerMapRepository playerMapRepository,
			UserRepository userRepository) {

		this.mapAreaRepository = mapAreaRepository;
		this.playerMapRepository = playerMapRepository;
		this.userRepository = userRepository;
	}

	// =====================================================
	// Lấy Map của người chơi
	// Nếu người chơi chưa có dữ liệu Map → khởi tạo
	// =====================================================

	@Transactional
	public List<PlayerMap> getPlayerMaps(String username) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		List<MapArea> mapAreas = mapAreaRepository.findAllByOrderByMapNumberAsc();

		List<PlayerMap> playerMaps = playerMapRepository.findByUserOrderByMapAreaMapNumberAsc(user);

		// Nếu chưa có dữ liệu Map cho User
		if (playerMaps.isEmpty()) {

			for (MapArea mapArea : mapAreas) {

				PlayerMap playerMap = new PlayerMap();

				playerMap.setUser(user);

				playerMap.setMapArea(mapArea);

				// Map 1 mở sẵn
				playerMap.setUnlocked(mapArea.isUnlockedByDefault());

				playerMapRepository.save(playerMap);
			}

			// Lấy lại dữ liệu
			playerMaps = playerMapRepository.findByUserOrderByMapAreaMapNumberAsc(user);
		}

		return playerMaps;
	}

	public MapArea getMapDetail(int mapNumber) {

		return mapAreaRepository.findByMapNumber(mapNumber)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Map."));
	}
}