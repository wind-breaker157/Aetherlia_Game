package com.aetherlia.service;

import com.aetherlia.entity.Monster;
import com.aetherlia.repository.MonsterRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonsterService {

    private final MonsterRepository monsterRepository;

    public MonsterService(MonsterRepository monsterRepository) {
        this.monsterRepository = monsterRepository;
    }

    public List<Monster> getAllMonsters() {
        return monsterRepository.findAll();
    }
}