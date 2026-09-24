package com.aetherlia.repository;

import com.aetherlia.entity.Team;
import com.aetherlia.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Team findByUser(User user);

}