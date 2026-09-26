package com.aetherlia.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerPosition;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;
import com.aetherlia.repository.MonsterRepository;
import com.aetherlia.repository.PlayerPositionRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;

import jakarta.transaction.Transactional;

@Service
public class WildEncounterService {

    private final UserRepository userRepository;
    private final PlayerPositionRepository playerPositionRepository;
    private final MonsterRepository monsterRepository;
    private final WildEncounterRepository wildEncounterRepository;
    private final StatService statService;

    public WildEncounterService(
            UserRepository userRepository,
            PlayerPositionRepository playerPositionRepository,
            MonsterRepository monsterRepository,
            WildEncounterRepository wildEncounterRepository,
            StatService statService) {

        this.userRepository = userRepository;
        this.playerPositionRepository = playerPositionRepository;
        this.monsterRepository = monsterRepository;
        this.wildEncounterRepository = wildEncounterRepository;
        this.statService = statService;
    }

    @Transactional
    public WildEncounter createEncounter(String username) {

        /*
         * =====================================================
         * 1. TÌM USER
         * =====================================================
         */
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy User: " + username
            );
        }

        /*
         * =====================================================
         * 2. KIỂM TRA ENCOUNTER ĐANG ACTIVE
         * =====================================================
         *
         * Nếu RandomEncounterService vừa tạo Monster,
         * thì /explore phải dùng lại Monster đó.
         */
        Optional<WildEncounter> activeOptional =
                wildEncounterRepository.findByUserAndActiveTrue(user);

        if (activeOptional.isPresent()) {
            return activeOptional.get();
        }

        /*
         * =====================================================
         * 3. LẤY VỊ TRÍ NGƯỜI CHƠI
         * =====================================================
         */
        PlayerPosition position =
                playerPositionRepository.findByUser(user);

        if (position == null) {
            throw new RuntimeException(
                    "Không tìm thấy vị trí người chơi."
            );
        }

        /*
         * =====================================================
         * 4. PHẢI Ở ROUTE CỎ CAO
         * =====================================================
         */
        if (position.getLocationCode() == null
                || !position.getLocationCode().equals("route_grass")) {

            throw new RuntimeException(
                    "Anh phải đi tới Route Cỏ Cao trước."
            );
        }

        /*
         * =====================================================
         * 5. LẤY DANH SÁCH MONSTER
         * =====================================================
         */
        List<Monster> monsters =
                monsterRepository.findAll();

        if (monsters == null || monsters.isEmpty()) {
            throw new RuntimeException(
                    "Chưa có Monster trong database."
            );
        }

        /*
         * =====================================================
         * 6. CHỌN MONSTER NGẪU NHIÊN
         * =====================================================
         */
        Monster monster =
                monsters.get(
                        ThreadLocalRandom.current().nextInt(
                                monsters.size()
                        )
                );

        /*
         * =====================================================
         * 7. RANDOM LEVEL 2 -> 5
         * =====================================================
         */
        int level =
                ThreadLocalRandom.current().nextInt(
                        2,
                        6
                );

        /*
         * =====================================================
         * 8. TẠO ENCOUNTER
         * =====================================================
         */
        WildEncounter encounter =
                new WildEncounter();

        encounter.setUser(user);
        encounter.setMonster(monster);
        encounter.setLevel(level);

        /*
         * =====================================================
         * 9. TÍNH HP
         * =====================================================
         */
        int maxHp =
                statService.calculateMaxHp(
                        monster.getBaseHp(),
                        level
                );

        encounter.setCurrentHp(maxHp);

        /*
         * =====================================================
         * 10. ACTIVE
         * =====================================================
         */
        encounter.setActive(true);

        /*
         * =====================================================
         * 11. LƯU DATABASE
         * =====================================================
         */
        return wildEncounterRepository.save(encounter);
    }
}