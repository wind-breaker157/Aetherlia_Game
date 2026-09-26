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

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            PlayerMonsterRepository playerMonsterRepository,
            UserRepository userRepository) {

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

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        Team team =
                teamRepository.findByUser(user);

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

    public List<TeamMember> getTeamMembers(
            String username) {

        Team team =
                getOrCreateTeam(username);

        return teamMemberRepository
                .findByTeamOrderByPositionAsc(team);
    }

    // =====================================================
    // 3. LẤY TOÀN BỘ MONSTER NGƯỜI CHƠI SỞ HỮU
    // =====================================================

    public List<PlayerMonster> getMyMonsters(
            String username) {

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        return playerMonsterRepository.findByUser(user);
    }

    // =====================================================
    // 4. THÊM MONSTER VÀO TEAM
    // =====================================================

    @Transactional
    public void addToTeam(
            String username,
            Long playerMonsterId) {

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        Team team =
                getOrCreateTeam(username);

        PlayerMonster playerMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                playerMonsterId,
                                user
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster không thuộc tài khoản."
                                )
                        );

        boolean alreadyInTeam =
                teamMemberRepository
                        .existsByTeamAndPlayerMonster(
                                team,
                                playerMonster
                        );

        if (alreadyInTeam) {
            return;
        }

        List<TeamMember> members =
                teamMemberRepository
                        .findByTeamOrderByPositionAsc(team);

        if (members.size() >= 6) {
            throw new RuntimeException(
                    "Team chỉ được tối đa 6 Monster."
            );
        }

        TeamMember teamMember =
                new TeamMember();

        teamMember.setTeam(team);

        teamMember.setPlayerMonster(
                playerMonster
        );

        teamMember.setPosition(
                members.size() + 1
        );

        teamMemberRepository.save(
                teamMember
        );
    }

    // =====================================================
    // 5. XÓA MONSTER KHỎI TEAM
    // =====================================================

    @Transactional
    public void removeFromTeam(
            String username,
            Long playerMonsterId) {

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        Team team =
                getOrCreateTeam(username);

        PlayerMonster playerMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                playerMonsterId,
                                user
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster không thuộc tài khoản."
                                )
                        );

        boolean exists =
                teamMemberRepository
                        .existsByTeamAndPlayerMonster(
                                team,
                                playerMonster
                        );

        if (!exists) {
            return;
        }

        teamMemberRepository
                .deleteByTeamAndPlayerMonster(
                        team,
                        playerMonster
                );

        List<TeamMember> members =
                teamMemberRepository
                        .findByTeamOrderByPositionAsc(team);

        int position = 1;

        for (TeamMember member : members) {

            member.setPosition(position);

            position++;
        }

        teamMemberRepository.saveAll(
                members
        );
    }

    // =====================================================
    // 6. ĐỔI VỊ TRÍ MONSTER
    // direction:
    // UP   -> lên 1 Slot
    // DOWN -> xuống 1 Slot
    // =====================================================

    @Transactional
    public void moveMonster(
            String username,
            Long playerMonsterId,
            String direction) {

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        Team team =
                getOrCreateTeam(username);

        PlayerMonster playerMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                playerMonsterId,
                                user
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster không thuộc tài khoản."
                                )
                        );

        List<TeamMember> members =
                teamMemberRepository
                        .findByTeamOrderByPositionAsc(team);

        int currentIndex = -1;

        for (int i = 0; i < members.size(); i++) {

            TeamMember member =
                    members.get(i);

            if (member.getPlayerMonster()
                    != null
                    && member.getPlayerMonster()
                            .getId()
                            .equals(playerMonsterId)) {

                currentIndex = i;

                break;
            }
        }

        if (currentIndex == -1) {
            throw new RuntimeException(
                    "Monster không nằm trong Team."
            );
        }

        int targetIndex = currentIndex;

        if ("UP".equalsIgnoreCase(direction)) {

            if (currentIndex > 0) {
                targetIndex = currentIndex - 1;
            }

        } else if ("DOWN".equalsIgnoreCase(direction)) {

            if (currentIndex < members.size() - 1) {
                targetIndex = currentIndex + 1;
            }

        } else {

            throw new RuntimeException(
                    "Hướng di chuyển Team không hợp lệ."
            );
        }

        /*
         * Đã ở đầu/cuối -> không cần làm gì.
         */
        if (targetIndex == currentIndex) {
            return;
        }

        /*
         * Đổi vị trí.
         */
        TeamMember currentMember =
                members.get(currentIndex);

        TeamMember targetMember =
                members.get(targetIndex);

        int currentPosition =
                currentMember.getPosition();

        int targetPosition =
                targetMember.getPosition();

        currentMember.setPosition(
                targetPosition
        );

        targetMember.setPosition(
                currentPosition
        );

        teamMemberRepository.save(
                currentMember
        );

        teamMemberRepository.save(
                targetMember
        );
    }
}