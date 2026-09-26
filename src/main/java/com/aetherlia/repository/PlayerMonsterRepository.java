package com.aetherlia.repository;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerMonsterRepository
        extends JpaRepository<PlayerMonster, Long> {

    List<PlayerMonster> findByUser(User user);

    boolean existsByUser(User user);

    Optional<PlayerMonster> findByIdAndUser(Long id, User user);
}