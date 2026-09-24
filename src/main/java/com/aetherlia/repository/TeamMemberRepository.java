package com.aetherlia.repository;

import com.aetherlia.entity.Team;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.entity.PlayerMonster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

	List<TeamMember> findByTeamOrderByPositionAsc(Team team);

	boolean existsByTeamAndPlayerMonster(Team team, PlayerMonster playerMonster);

	void deleteByTeamAndPlayerMonster(Team team, PlayerMonster playerMonster);
}