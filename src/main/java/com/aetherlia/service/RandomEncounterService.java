package com.aetherlia.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerPosition;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;
import com.aetherlia.game.EncounterCheckResult;
import com.aetherlia.repository.MonsterRepository;
import com.aetherlia.repository.PlayerPositionRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;

import jakarta.transaction.Transactional;

@Service
public class RandomEncounterService {

    /*
     * Xác suất gặp Monster mỗi bước trong cỏ.
     *
     * 4 = 4%
     */
    private static final int ENCOUNTER_CHANCE_PERCENT = 4;

    /*
     * Level Monster hoang dã.
     */
    private static final int MIN_WILD_LEVEL = 2;
    private static final int MAX_WILD_LEVEL = 5;

    private final UserRepository userRepository;
    private final PlayerPositionRepository playerPositionRepository;
    private final MonsterRepository monsterRepository;
    private final WildEncounterRepository wildEncounterRepository;
    private final StatService statService;

    public RandomEncounterService(
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

    /*
     * =========================================================
     * CHECK RANDOM ENCOUNTER
     * =========================================================
     */
    @Transactional
    public EncounterCheckResult checkEncounter(String username) {

        /*
         * Tìm User.
         */
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy User: " + username
            );
        }

        /*
         * Lấy vị trí người chơi.
         */
        PlayerPosition position =
                playerPositionRepository.findByUser(user);

        if (position == null) {
            return new EncounterCheckResult(
                    false,
                    null,
                    null,
                    0
            );
        }

        /*
         * Chỉ random khi đang ở route_grass.
         */
        if (position.getLocationCode() == null
                || !position.getLocationCode().equals("route_grass")) {

            return new EncounterCheckResult(
                    false,
                    null,
                    null,
                    0
            );
        }

        /*
         * Kiểm tra xem người chơi có encounter đang hoạt động hay không.
         *
         * Repository trả về Optional<WildEncounter>
         */
        Optional<WildEncounter> activeEncounterOptional =
                wildEncounterRepository.findByUserAndActiveTrue(user);

        /*
         * Nếu có encounter cũ thì dùng lại encounter đó.
         */
        if (activeEncounterOptional.isPresent()) {

            WildEncounter activeEncounter =
                    activeEncounterOptional.get();

            return new EncounterCheckResult(
                    true,
                    activeEncounter.getId(),
                    activeEncounter.getMonster().getName(),
                    activeEncounter.getLevel()
            );
        }

        /*
         * Random số từ 1 -> 100.
         */
        int random =
                ThreadLocalRandom.current().nextInt(1, 101);

        /*
         * Chưa gặp.
         */
        if (random > ENCOUNTER_CHANCE_PERCENT) {
            return new EncounterCheckResult(
                    false,
                    null,
                    null,
                    0
            );
        }

        /*
         * Lấy danh sách Monster.
         */
        List<Monster> monsters =
                monsterRepository.findAll();

        if (monsters == null || monsters.isEmpty()) {
            return new EncounterCheckResult(
                    false,
                    null,
                    null,
                    0
            );
        }

        /*
         * Chọn Monster ngẫu nhiên.
         */
        Monster monster =
                monsters.get(
                        ThreadLocalRandom.current().nextInt(
                                monsters.size()
                        )
                );

        /*
         * Random level 2 -> 5.
         */
        int level =
                ThreadLocalRandom.current().nextInt(
                        MIN_WILD_LEVEL,
                        MAX_WILD_LEVEL + 1
                );

        /*
         * Tạo WildEncounter.
         */
        WildEncounter encounter =
                new WildEncounter();

        encounter.setUser(user);
        encounter.setMonster(monster);
        encounter.setLevel(level);

        /*
         * Tính HP tối đa của Monster.
         */
        int maxHp =
                statService.calculateMaxHp(
                        monster.getBaseHp(),
                        level
                );

        encounter.setCurrentHp(maxHp);

        /*
         * Đánh dấu encounter đang hoạt động.
         */
        encounter.setActive(true);

        /*
         * Lưu database.
         */
        encounter =
                wildEncounterRepository.save(encounter);

        /*
         * Trả kết quả về Controller.
         */
        return new EncounterCheckResult(
                true,
                encounter.getId(),
                monster.getName(),
                level
        );
    }
}