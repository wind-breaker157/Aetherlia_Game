package com.aetherlia.repository;

import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WildEncounterRepository
        extends JpaRepository<WildEncounter, Long> {

    Optional<WildEncounter> findByUserAndActiveTrue(
            User user
    );
}