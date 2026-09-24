package com.aetherlia.service;

import com.aetherlia.entity.MapArea;
import com.aetherlia.entity.PlayerPosition;
import com.aetherlia.entity.User;

import com.aetherlia.repository.MapAreaRepository;
import com.aetherlia.repository.PlayerPositionRepository;
import com.aetherlia.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerPositionService {

	private final PlayerPositionRepository playerPositionRepository;
	private final UserRepository userRepository;
	private final MapAreaRepository mapAreaRepository;

	public PlayerPositionService(PlayerPositionRepository playerPositionRepository, UserRepository userRepository,
			MapAreaRepository mapAreaRepository) {

		this.playerPositionRepository = playerPositionRepository;
		this.userRepository = userRepository;
		this.mapAreaRepository = mapAreaRepository;
	}

	// ==========================================
	// Lấy vị trí hiện tại
	// Nếu chưa có thì đặt tại Map 1
	// ==========================================

	@Transactional
	public PlayerPosition getOrCreatePosition(String username) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		PlayerPosition position = playerPositionRepository.findByUser(user);

		if (position == null) {

			MapArea map = mapAreaRepository.findByMapNumber(1)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy Map 1."));

			position = new PlayerPosition();

			position.setUser(user);

			position.setMapArea(map);

			position.setLocationCode("village");

			/*
			 * Vị trí bắt đầu: chính giữa World.
			 */
			position.setX(450);
			position.setY(250);

			position = playerPositionRepository.save(position);

		} else {

			/*
			 * Những PlayerPosition cũ được tạo trước khi có x/y sẽ có 0,0.
			 *
			 * Đưa về chính giữa World.
			 */
			if (position.getX() == 0 && position.getY() == 0) {

				position.setX(450);
				position.setY(250);

				position = playerPositionRepository.save(position);
			}
		}

		return position;
	}

	// ==========================================
	// Di chuyển trong Map
	// ==========================================

	@Transactional
	public void move(String username, int mapNumber, String locationCode) {

		PlayerPosition position = getOrCreatePosition(username);

		MapArea mapArea = mapAreaRepository.findByMapNumber(mapNumber)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy Map."));

		// Hiện tại chỉ cho di chuyển trong Map hiện tại
		if (position.getMapArea().getMapNumber() != mapNumber) {

			throw new RuntimeException("Anh chưa ở khu vực này.");
		}

		position.setLocationCode(locationCode);

		playerPositionRepository.save(position);
	}

	@Transactional
	public PlayerPosition moveCharacter(String username, String direction) {

		PlayerPosition position = getOrCreatePosition(username);

		final int STEP = 32;

		/*
		 * World:
		 *
		 * 0 ---------------- 900
		 *
		 * 0 | | 500
		 */

		int x = position.getX();
		int y = position.getY();

		switch (direction.toUpperCase()) {

		case "UP":
			y -= STEP;
			break;

		case "DOWN":
			y += STEP;
			break;

		case "LEFT":
			x -= STEP;
			break;

		case "RIGHT":
			x += STEP;
			break;

		default:
			throw new RuntimeException("Hướng di chuyển không hợp lệ.");
		}

		/*
		 * Không cho đi ra ngoài World.
		 */
		x = Math.max(32, Math.min(868, x));

		y = Math.max(32, Math.min(468, y));

		position.setX(x);
		position.setY(y);

		return playerPositionRepository.save(position);
	}
}