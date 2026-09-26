package com.aetherlia.repository;

import com.aetherlia.entity.PlayerCharacter;
import com.aetherlia.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerCharacterRepository
        extends JpaRepository<PlayerCharacter, Long> {

    PlayerCharacter findByUser(User user);
}