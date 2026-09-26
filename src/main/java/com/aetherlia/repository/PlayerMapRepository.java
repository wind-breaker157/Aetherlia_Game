package com.aetherlia.repository;

import com.aetherlia.entity.PlayerMap;
import com.aetherlia.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerMapRepository
        extends JpaRepository<PlayerMap, Long> {

    List<PlayerMap> findByUserOrderByMapAreaMapNumberAsc(
            User user
    );

    boolean existsByUserAndMapArea(
            User user,
            com.aetherlia.entity.MapArea mapArea
    );
}