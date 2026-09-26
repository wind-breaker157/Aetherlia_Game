package com.aetherlia.service;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aetherlia.battle.CaptureResult;
import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;
import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;

@Service
public class CaptureService {

    private final WildEncounterRepository wildEncounterRepository;
    private final PlayerMonsterRepository playerMonsterRepository;
    private final UserRepository userRepository;
    private final StatService statService;
    private final InventoryService inventoryService;
    private final TeamService teamService;

    private final Random random = new Random();

    public CaptureService(
            WildEncounterRepository wildEncounterRepository,
            PlayerMonsterRepository playerMonsterRepository,
            UserRepository userRepository,
            InventoryService inventoryService,
            TeamService teamService,
            StatService statService) {

        this.wildEncounterRepository = wildEncounterRepository;
        this.playerMonsterRepository = playerMonsterRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.teamService = teamService;
        this.statService = statService;
    }

    // =====================================================
    // CAPTURE MONSTER
    // =====================================================

    @Transactional
    public CaptureResult capture(
            String username,
            Long wildEncounterId,
            String orbType) {

        /*
         * =================================================
         * 1. TÌM USER
         * =================================================
         */

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        /*
         * =================================================
         * 2. TÌM WILD ENCOUNTER
         * =================================================
         */

        WildEncounter encounter =
                wildEncounterRepository
                        .findById(wildEncounterId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy Monster hoang dã."
                                )
                        );

        /*
         * =================================================
         * 3. KIỂM TRA ENCOUNTER THUỘC USER
         * =================================================
         */

        if (encounter.getUser() == null
                || encounter.getUser().getId() == null
                || !encounter.getUser()
                        .getId()
                        .equals(user.getId())) {

            throw new RuntimeException(
                    "Battle không thuộc tài khoản."
            );
        }

        /*
         * =================================================
         * 4. KIỂM TRA ENCOUNTER ACTIVE
         * =================================================
         */

        if (!encounter.isActive()) {

            return new CaptureResult(
                    "Battle đã kết thúc.",
                    false,
                    0
            );
        }

        Monster monster =
                encounter.getMonster();

        /*
         * =================================================
         * 5. KHÔNG CHO BẮT LEGENDARY THƯỜNG
         * =================================================
         */

        if ("LEGENDARY".equalsIgnoreCase(
                monster.getRarity())) {

            return new CaptureResult(
                    "Monster Legendary này chỉ có thể "
                    + "thu phục thông qua Quest Legendary.",
                    false,
                    0
            );
        }

        /*
         * =================================================
         * 6. KIỂM TRA ORB HỢP LỆ
         * =================================================
         */

        double orbMultiplier =
                getOrbMultiplier(orbType);

        if (orbMultiplier <= 0) {

            return new CaptureResult(
                    "Loại Capture Orb không hợp lệ.",
                    false,
                    0
            );
        }

        /*
         * =================================================
         * 7. TRỪ ORB
         * =================================================
         */

        boolean consumed =
                inventoryService.consumeItem(
                        username,
                        orbType,
                        1
                );

        if (!consumed) {

            return new CaptureResult(
                    "Anh không còn Capture Orb phù hợp.",
                    false,
                    0
            );
        }

        /*
         * =================================================
         * 8. TÌM LEVEL CAO NHẤT CỦA TEAM
         * =================================================
         */

        List<TeamMember> members =
                teamService.getTeamMembers(username);

        int bestLevel = 1;

        for (TeamMember member : members) {

            if (member.getPlayerMonster() != null) {

                bestLevel =
                        Math.max(
                                bestLevel,
                                member.getPlayerMonster()
                                        .getLevel()
                        );
            }
        }

        /*
         * =================================================
         * 9. MAX HP THỰC TẾ
         * =================================================
         *
         * Phải dùng HP theo Level của Monster hoang dã,
         * không dùng baseHp trực tiếp.
         */

        double maxHp =
                statService.calculateMaxHp(
                        monster.getBaseHp(),
                        encounter.getLevel()
                );

        double currentHp =
                encounter.getCurrentHp();

        /*
         * Bảo vệ dữ liệu.
         */
        currentHp =
                Math.max(
                        0,
                        Math.min(
                                currentHp,
                                maxHp
                        )
                );

        /*
         * =================================================
         * 10. HP FACTOR
         * =================================================
         *
         * ((3 * maxHP - 2 * curHP) / (3 * maxHP))
         */

        double hpFactor =
                (
                        (3 * maxHp)
                        - (2 * currentHp)
                )
                / (3 * maxHp);

        /*
         * Đảm bảo 0 -> 1.
         */
        hpFactor =
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                hpFactor
                        )
                );

        /*
         * =================================================
         * 11. BASE CATCH THEO RARITY
         * =================================================
         */

        double baseCatch =
                getBaseCatch(
                        monster.getRarity()
                );

        /*
         * =================================================
         * 12. STATUS
         * =================================================
         *
         * Hiện tại project chưa có Status System.
         * Vì vậy dùng 1.0.
         */

        double statusMultiplier = 1.0;

        /*
         * =================================================
         * 13. LEVEL MODIFIER
         * =================================================
         *
         * max(
         *     0.5,
         *     1 - ((wildLevel - bestLevel) * 0.02)
         * )
         */

        double levelModifier =
                1.0
                - (
                        (
                                encounter.getLevel()
                                - bestLevel
                        )
                        * 0.02
                );

        levelModifier =
                Math.max(
                        0.5,
                        levelModifier
                );

        /*
         * =================================================
         * 14. TÍNH TỶ LỆ BẮT
         * =================================================
         */

        double chance =
                hpFactor
                * baseCatch
                * orbMultiplier
                * statusMultiplier
                * levelModifier;

        /*
         * Giới hạn 0 -> 100%.
         */

        chance =
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                chance
                        )
                );

        int percent =
                (int) Math.round(
                        chance * 100
                );

        /*
         * =================================================
         * 15. RANDOM CAPTURE
         * =================================================
         */

        boolean success =
                random.nextDouble() < chance;

        /*
         * =================================================
         * 16. CAPTURE THẤT BẠI
         * =================================================
         */

        if (!success) {

            /*
             * Monster có thể rung 1 -> 3 lần.
             */
            int shakes =
                    1 + random.nextInt(3);

            return new CaptureResult(
                    "Capture Orb rung "
                    + shakes
                    + " lần nhưng "
                    + monster.getName()
                    + " đã thoát! "
                    + "Tỷ lệ bắt khoảng "
                    + percent
                    + "%.",
                    false,
                    shakes
            );
        }

        /*
         * =================================================
         * 17. TẠO PLAYER MONSTER
         * =================================================
         */

        PlayerMonster playerMonster =
                new PlayerMonster();

        playerMonster.setUser(user);

        playerMonster.setMonster(monster);

        playerMonster.setLevel(
                encounter.getLevel()
        );

        playerMonster.setExp(0);

        /*
         * Monster bắt được bắt đầu với HP đầy.
         */

        int maxHpPlayerMonster =
                statService.calculateMaxHp(
                        monster.getBaseHp(),
                        encounter.getLevel()
                );

        playerMonster.setCurrentHp(
                maxHpPlayerMonster
        );

        playerMonster =
                playerMonsterRepository.save(
                        playerMonster
                );

        /*
         * =================================================
         * 18. KẾT THÚC WILD ENCOUNTER
         * =================================================
         */

        encounter.setCurrentHp(0);

        encounter.setActive(false);

        wildEncounterRepository.save(
                encounter
        );

        /*
         * =================================================
         * 19. KIỂM TRA TEAM
         * =================================================
         */

        List<TeamMember> currentTeam =
                teamService.getTeamMembers(username);

        /*
         * Team còn chỗ -> tự thêm Monster.
         */

        if (currentTeam.size() < 6) {

            teamService.addToTeam(
                    username,
                    playerMonster.getId()
            );

            return new CaptureResult(
                    "🎉 Thu phục thành công "
                    + monster.getName()
                    + " Lv."
                    + encounter.getLevel()
                    + "! "
                    + "Monster đã được thêm vào Team. "
                    + "Tỷ lệ bắt: "
                    + percent
                    + "%.",
                    true,
                    3
            );
        }

        /*
         * =================================================
         * 20. TEAM ĐẦY
         * =================================================
         *
         * PlayerMonster vẫn được lưu,
         * tức là Monster nằm trong collection.
         */

        return new CaptureResult(
                "🎉 Thu phục thành công "
                + monster.getName()
                + " Lv."
                + encounter.getLevel()
                + "! "
                + "Team đã đủ 6, Monster được lưu vào kho. "
                + "Tỷ lệ bắt: "
                + percent
                + "%.",
                true,
                3
        );
    }

    // =====================================================
    // BASE CATCH
    // =====================================================

    private double getBaseCatch(
            String rarity) {

        if (rarity == null) {
            return 1.0;
        }

        switch (rarity.toUpperCase()) {

        case "COMMON":
            return 1.0;

        case "UNCOMMON":
            return 0.7;

        case "RARE":
            return 0.4;

        case "EPIC":
            return 0.2;

        case "LEGENDARY":
            return 0.0;

        default:
            return 1.0;
        }
    }

    // =====================================================
    // ORB MULTIPLIER
    // =====================================================

    private double getOrbMultiplier(
            String orbType) {

        if (orbType == null) {
            return 0.0;
        }

        switch (orbType.toUpperCase()) {

        case "BASIC_ORB":
            return 1.0;

        case "GREAT_ORB":
            return 1.5;

        case "ULTRA_ORB":
            return 2.0;

        case "MASTER_ORB":
            return 100.0;

        default:
            return 0.0;
        }
    }
}