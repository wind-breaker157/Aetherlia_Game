package com.aetherlia.service;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.Team;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.entity.User;

import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.TeamMemberRepository;
import com.aetherlia.repository.TeamRepository;
import com.aetherlia.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final PlayerMonsterRepository playerMonsterRepository;
	private final UserRepository userRepository;

	public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
			PlayerMonsterRepository playerMonsterRepository, UserRepository userRepository) {

		this.teamRepository = teamRepository;
		this.teamMemberRepository = teamMemberRepository;
		this.playerMonsterRepository = playerMonsterRepository;
		this.userRepository = userRepository;
	}

	// =====================================================
	// 1. LẤY TEAM CỦA NGƯỜI CHƠI
	// Nếu chưa có Team thì tạo mới
	// =====================================================

	public Team getOrCreateTeam(String username) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		Team team = teamRepository.findByUser(user);

		if (team == null) {

			team = new Team();

			team.setUser(user);

			team = teamRepository.save(team);
		}

		return team;
	}

	// =====================================================
	// 2. LẤY DANH SÁCH MONSTER TRONG TEAM
	// =====================================================

	public List<TeamMember> getTeamMembers(String username) {

		Team team = getOrCreateTeam(username);

		return teamMemberRepository.findByTeamOrderByPositionAsc(team);
	}

	// =====================================================
	// 3. LẤY TOÀN BỘ MONSTER NGƯỜI CHƠI ĐANG SỞ HỮU
	// =====================================================

	public List<PlayerMonster> getMyMonsters(String username) {

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		return playerMonsterRepository.findByUser(user);
	}

	// =====================================================
	// 4. THÊM MONSTER VÀO TEAM
	// =====================================================

	@Transactional
	public void addToTeam(String username, Long playerMonsterId) {

		// -------------------------------------------------
		// Tìm User
		// -------------------------------------------------

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		// -------------------------------------------------
		// Lấy Team
		// -------------------------------------------------

		Team team = getOrCreateTeam(username);

		// -------------------------------------------------
		// Tìm PlayerMonster
		// Đồng thời kiểm tra Monster thuộc User hiện tại
		// -------------------------------------------------

		PlayerMonster playerMonster = playerMonsterRepository.findByIdAndUser(playerMonsterId, user)
				.orElseThrow(() -> new RuntimeException("Monster không thuộc tài khoản."));

		// -------------------------------------------------
		// Kiểm tra Monster đã ở Team chưa
		// -------------------------------------------------

		boolean alreadyInTeam = teamMemberRepository.existsByTeamAndPlayerMonster(team, playerMonster);

		if (alreadyInTeam) {

			// Không thêm trùng
			return;
		}

		// -------------------------------------------------
		// Lấy danh sách hiện tại
		// -------------------------------------------------

		List<TeamMember> members = teamMemberRepository.findByTeamOrderByPositionAsc(team);

		// -------------------------------------------------
		// Team tối đa 6 Monster
		// -------------------------------------------------

		if (members.size() >= 6) {

			throw new RuntimeException("Team chỉ được tối đa 6 Monster.");
		}

		// -------------------------------------------------
		// Tạo TeamMember
		// -------------------------------------------------

		TeamMember teamMember = new TeamMember();

		teamMember.setTeam(team);

		teamMember.setPlayerMonster(playerMonster);

		// -------------------------------------------------
		// Đặt vị trí tiếp theo
		// -------------------------------------------------

		teamMember.setPosition(members.size() + 1);

		// -------------------------------------------------
		// Lưu
		// -------------------------------------------------

		teamMemberRepository.save(teamMember);
	}

	// =====================================================
	// 5. XÓA MONSTER KHỎI TEAM
	// =====================================================

	@Transactional
	public void removeFromTeam(String username, Long playerMonsterId) {

		// -------------------------------------------------
		// Tìm User
		// -------------------------------------------------

		User user = userRepository.findByUsername(username);

		if (user == null) {
			throw new RuntimeException("Không tìm thấy người chơi.");
		}

		// -------------------------------------------------
		// Lấy Team
		// -------------------------------------------------

		Team team = getOrCreateTeam(username);

		// -------------------------------------------------
		// Tìm PlayerMonster
		// Đảm bảo Monster thuộc User hiện tại
		// -------------------------------------------------

		PlayerMonster playerMonster = playerMonsterRepository.findByIdAndUser(playerMonsterId, user)
				.orElseThrow(() -> new RuntimeException("Monster không thuộc tài khoản."));

		// -------------------------------------------------
		// Kiểm tra Monster có trong Team hay không
		// -------------------------------------------------

		boolean exists = teamMemberRepository.existsByTeamAndPlayerMonster(team, playerMonster);

		if (!exists) {

			// Không có thì không cần xóa
			return;
		}

		// -------------------------------------------------
		// Xóa Monster khỏi Team
		// -------------------------------------------------

		teamMemberRepository.deleteByTeamAndPlayerMonster(team, playerMonster);

		// -------------------------------------------------
		// Lấy lại danh sách sau khi xóa
		// -------------------------------------------------

		List<TeamMember> members = teamMemberRepository.findByTeamOrderByPositionAsc(team);

		// -------------------------------------------------
		// Đánh lại vị trí
		// -------------------------------------------------

		int position = 1;

		for (TeamMember member : members) {

			member.setPosition(position);

			position++;
		}

		// -------------------------------------------------
		// Lưu lại vị trí
		// -------------------------------------------------

		teamMemberRepository.saveAll(members);
	}
}