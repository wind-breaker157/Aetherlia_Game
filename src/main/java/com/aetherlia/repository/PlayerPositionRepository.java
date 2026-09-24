package com.aetherlia.repository;

import com.aetherlia.entity.PlayerPosition;
import com.aetherlia.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerPositionRepository
        extends JpaRepository<PlayerPosition, Long> {

    PlayerPosition findByUser(User user);
}