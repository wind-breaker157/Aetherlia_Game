package com.aetherlia.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aetherlia.battle.BattleResult;
import com.aetherlia.battle.BattleSkill;
import com.aetherlia.battle.BattleStartData;
import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;
import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;

@Service
public class BattleService {

    private final PlayerMonsterRepository playerMonsterRepository;
    private final WildEncounterRepository wildEncounterRepository;
    private final UserRepository userRepository;
    private final TeamService teamService;
    private final ProgressionService progressionService;
    private final StatService statService;
    private final InventoryService inventoryService;

    private final Random random = new Random();

    public BattleService(
            PlayerMonsterRepository playerMonsterRepository,
            WildEncounterRepository wildEncounterRepository,
            UserRepository userRepository,
            TeamService teamService,
            ProgressionService progressionService,
            StatService statService,
            InventoryService inventoryService) {

        this.playerMonsterRepository = playerMonsterRepository;
        this.wildEncounterRepository = wildEncounterRepository;
        this.userRepository = userRepository;
        this.teamService = teamService;
        this.progressionService = progressionService;
        this.statService = statService;
        this.inventoryService = inventoryService;
    }

    // =====================================================
    // ATTACK RESULT
    // =====================================================

    private static class AttackOutcome {

        private final int damage;
        private final boolean critical;
        private final double typeMultiplier;

        public AttackOutcome(
                int damage,
                boolean critical,
                double typeMultiplier) {

            this.damage = damage;
            this.critical = critical;
            this.typeMultiplier = typeMultiplier;
        }

        public int getDamage() {
            return damage;
        }

        public boolean isCritical() {
            return critical;
        }

        public double getTypeMultiplier() {
            return typeMultiplier;
        }
    }

    // =====================================================
    // BATTLE START
    // =====================================================

    public BattleStartData prepareBattle(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi.");
        }

        WildEncounter encounter =
                wildEncounterRepository
                        .findByUserAndActiveTrue(user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không có Monster hoang dã."));

        List<TeamMember> members =
                teamService.getTeamMembers(username);

        if (members.isEmpty()) {
            throw new RuntimeException(
                    "Team chưa có Monster.");
        }

        PlayerMonster playerMonster =
                members.get(0).getPlayerMonster();

        int maxHp =
                statService.calculatePlayerMaxHp(
                        playerMonster);

        if (playerMonster.getCurrentHp() <= 0) {

            playerMonster.setCurrentHp(maxHp);

            playerMonsterRepository.save(
                    playerMonster);
        }

        return new BattleStartData(
                playerMonster.getId(),
                encounter.getId());
    }

    // =====================================================
    // SKILLS
    // =====================================================

    public List<BattleSkill> getAvailableSkills() {

        List<BattleSkill> skills =
                new ArrayList<>();

        skills.add(
                new BattleSkill(
                        "basic_attack",
                        "Basic Attack",
                        "NORMAL",
                        40,
                        100,
                        0));

        skills.add(
                new BattleSkill(
                        "fire_burst",
                        "Fire Burst",
                        "FIRE",
                        40,
                        100,
                        0));

        skills.add(
                new BattleSkill(
                        "water_shot",
                        "Water Shot",
                        "WATER",
                        40,
                        100,
                        0));

        skills.add(
                new BattleSkill(
                        "nature_strike",
                        "Nature Strike",
                        "GRASS",
                        40,
                        100,
                        0));

        return skills;
    }

    // =====================================================
    // FIND SKILL
    // =====================================================

    private BattleSkill findSkill(String skillId) {

        for (BattleSkill skill :
                getAvailableSkills()) {

            if (skill.getId()
                    .equals(skillId)) {

                return skill;
            }
        }

        throw new RuntimeException(
                "Không tìm thấy Skill.");
    }

    // =====================================================
    // PLAYER ATTACK
    // =====================================================

    @Transactional
    public BattleResult playerAttack(
            String username,
            Long playerMonsterId,
            Long wildEncounterId,
            String skillId) {

        User user =
                userRepository.findByUsername(
                        username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi.");
        }

        PlayerMonster playerMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                playerMonsterId,
                                user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster không thuộc tài khoản."));

        WildEncounter encounter =
                wildEncounterRepository
                        .findById(wildEncounterId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy Battle."));

        validateEncounterOwner(
                encounter,
                user);

        if (!encounter.isActive()) {

            return new BattleResult(
                    "Battle đã kết thúc.",
                    false,
                    false,
                    playerMonster.getId());
        }

        if (playerMonster.getCurrentHp() <= 0) {

            return new BattleResult(
                    "Monster của anh đã bị hạ.",
                    false,
                    true,
                    playerMonster.getId());
        }

        BattleSkill skill =
                findSkill(skillId);

        // =================================================
        // PLAYER ATTACK
        // =================================================

        AttackOutcome playerOutcome =
                calculateDamage(
                        playerMonster.getLevel(),
                        playerMonster.getMonster().getBaseAttack(),
                        encounter.getMonster().getBaseDefense(),
                        skill.getPower(),
                        skill.getType(),
                        playerMonster.getMonster().getType(),
                        encounter.getMonster().getType());

        int playerDamage =
                playerOutcome.getDamage();

        int newEnemyHp =
                Math.max(
                        0,
                        encounter.getCurrentHp()
                                - playerDamage);

        encounter.setCurrentHp(
                newEnemyHp);

        // =================================================
        // WILD DEFEATED
        // =================================================

        if (newEnemyHp <= 0) {

            encounter.setCurrentHp(0);
            encounter.setActive(false);

            wildEncounterRepository.save(
                    encounter);

            String expMessage =
                    progressionService
                            .grantWildExp(
                                    playerMonster,
                                    encounter);

            playerMonsterRepository.save(
                    playerMonster);

            BattleResult result =
                    new BattleResult(
                            "Anh đã đánh bại Monster hoang dã! "
                                    + "Damage: "
                                    + playerDamage
                                    + ". "
                                    + expMessage,
                            true,
                            false,
                            playerMonster.getId());

            applyPlayerMetadata(
                    result,
                    playerOutcome);

            return result;
        }

        // =================================================
        // WILD COUNTER ATTACK
        // =================================================

        AttackOutcome enemyOutcome =
                performWildAttack(
                        encounter,
                        playerMonster);

        int enemyDamage =
                enemyOutcome.getDamage();

        int oldPlayerHp =
                playerMonster.getCurrentHp();

        int newPlayerHp =
                Math.max(
                        0,
                        oldPlayerHp
                                - enemyDamage);

        playerMonster.setCurrentHp(
                newPlayerHp);

        // =================================================
        // PLAYER DEFEATED
        // =================================================

        if (newPlayerHp <= 0) {

            playerMonster.setCurrentHp(0);

            encounter.setActive(false);

            playerMonsterRepository.save(
                    playerMonster);

            wildEncounterRepository.save(
                    encounter);

            BattleResult result =
                    new BattleResult(
                            "Monster của anh đã bị hạ. "
                                    + "HP trước đòn cuối: "
                                    + oldPlayerHp
                                    + ". Damage nhận: "
                                    + enemyDamage
                                    + ". HP còn lại: 0.",
                            false,
                            true,
                            playerMonster.getId());

            applyPlayerMetadata(
                    result,
                    playerOutcome);

            applyEnemyMetadata(
                    result,
                    enemyOutcome);

            return result;
        }

        // =================================================
        // SAVE NORMAL BATTLE
        // =================================================

        playerMonsterRepository.save(
                playerMonster);

        wildEncounterRepository.save(
                encounter);

        BattleResult result =
                new BattleResult(
                        "Anh gây "
                                + playerDamage
                                + " damage. "
                                + encounter.getMonster().getName()
                                + " phản công gây "
                                + enemyDamage
                                + " damage. "
                                + "HP của anh còn "
                                + newPlayerHp
                                + ".",
                        false,
                        false,
                        playerMonster.getId());

        applyPlayerMetadata(
                result,
                playerOutcome);

        applyEnemyMetadata(
                result,
                enemyOutcome);

        return result;
    }

    // =====================================================
    // CAPTURE FAIL -> ENEMY TURN
    // =====================================================

    @Transactional
    public BattleResult enemyTurnAfterCapture(
            String username,
            Long playerMonsterId,
            Long wildEncounterId) {

        User user =
                userRepository.findByUsername(
                        username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi.");
        }

        PlayerMonster playerMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                playerMonsterId,
                                user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster không thuộc tài khoản."));

        WildEncounter encounter =
                wildEncounterRepository
                        .findById(wildEncounterId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy Battle."));

        validateEncounterOwner(
                encounter,
                user);

        // =================================================
        // BATTLE KHÔNG CÒN HOẠT ĐỘNG
        // =================================================

        if (!encounter.isActive()) {

            return new BattleResult(
                    "Battle đã kết thúc.",
                    false,
                    false,
                    playerMonster.getId());
        }

        // =================================================
        // PLAYER ĐÃ BỊ HẠ
        // =================================================

        if (playerMonster.getCurrentHp() <= 0) {

            return new BattleResult(
                    "Monster của anh đã bị hạ.",
                    false,
                    true,
                    playerMonster.getId());
        }

        // =================================================
        // WILD ATTACK
        // =================================================

        AttackOutcome enemyOutcome =
                performWildAttack(
                        encounter,
                        playerMonster);

        int enemyDamage =
                enemyOutcome.getDamage();

        int oldPlayerHp =
                playerMonster.getCurrentHp();

        int newPlayerHp =
                Math.max(
                        0,
                        oldPlayerHp
                                - enemyDamage);

        playerMonster.setCurrentHp(
                newPlayerHp);

        // =================================================
        // PLAYER DEFEATED
        // =================================================

        if (newPlayerHp <= 0) {

            playerMonster.setCurrentHp(0);

            encounter.setActive(false);

            playerMonsterRepository.save(
                    playerMonster);

            wildEncounterRepository.save(
                    encounter);

            BattleResult result =
                    new BattleResult(
                            encounter.getMonster().getName()
                                    + " tấn công sau khi bắt hụt. "
                                    + "Damage nhận: "
                                    + enemyDamage
                                    + ". HP còn lại: 0.",
                            false,
                            true,
                            playerMonster.getId());

            applyEnemyMetadata(
                    result,
                    enemyOutcome);

            return result;
        }

        // =================================================
        // BATTLE CONTINUES
        // =================================================

        playerMonsterRepository.save(
                playerMonster);

        wildEncounterRepository.save(
                encounter);

        BattleResult result =
                new BattleResult(
                        "Bắt Monster thất bại! "
                                + encounter.getMonster().getName()
                                + " được quyền tấn công. "
                                + "Damage nhận: "
                                + enemyDamage
                                + ". HP của anh còn "
                                + newPlayerHp
                                + ".",
                        false,
                        false,
                        playerMonster.getId());

        applyEnemyMetadata(
                result,
                enemyOutcome);

        return result;
    }

    // =====================================================
    // WILD ATTACK
    // =====================================================

    private AttackOutcome performWildAttack(
            WildEncounter encounter,
            PlayerMonster playerMonster) {

        return calculateDamage(
                encounter.getLevel(),
                encounter.getMonster().getBaseAttack(),
                playerMonster.getMonster().getBaseDefense(),
                35,
                encounter.getMonster().getType(),
                encounter.getMonster().getType(),
                playerMonster.getMonster().getType());
    }

    // =====================================================
    // SWITCH MONSTER
    // =====================================================

    @Transactional
    public BattleResult switchMonster(
            String username,
            Long newPlayerMonsterId,
            Long wildEncounterId) {

        User user =
                userRepository.findByUsername(
                        username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi.");
        }

        PlayerMonster newMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                newPlayerMonsterId,
                                user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster không thuộc tài khoản."));

        List<TeamMember> members =
                teamService.getTeamMembers(
                        username);

        boolean inTeam = false;

        for (TeamMember member : members) {

            if (member.getPlayerMonster()
                    .getId()
                    .equals(newMonster.getId())) {

                inTeam = true;
                break;
            }
        }

        if (!inTeam) {

            throw new RuntimeException(
                    "Monster không nằm trong Team.");
        }

        if (newMonster.getCurrentHp() <= 0) {

            throw new RuntimeException(
                    "Monster này đã bị hạ.");
        }

        WildEncounter encounter =
                wildEncounterRepository
                        .findById(wildEncounterId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy Battle."));

        validateEncounterOwner(
                encounter,
                user);

        if (!encounter.isActive()) {

            return new BattleResult(
                    "Battle đã kết thúc.",
                    false,
                    false,
                    newMonster.getId());
        }

        // =================================================
        // WILD ATTACK AFTER SWITCH
        // =================================================

        AttackOutcome enemyOutcome =
                performWildAttack(
                        encounter,
                        newMonster);

        int enemyDamage =
                enemyOutcome.getDamage();

        int oldHp =
                newMonster.getCurrentHp();

        int newHp =
                Math.max(
                        0,
                        oldHp
                                - enemyDamage);

        newMonster.setCurrentHp(
                newHp);

        if (newHp <= 0) {

            newMonster.setCurrentHp(0);

            playerMonsterRepository.save(
                    newMonster);

            encounter.setActive(false);

            wildEncounterRepository.save(
                    encounter);

            BattleResult result =
                    new BattleResult(
                            "Anh đã đổi Monster nhưng "
                                    + encounter.getMonster().getName()
                                    + " phản công và hạ Monster mới. "
                                    + "Damage: "
                                    + enemyDamage
                                    + ".",
                            false,
                            true,
                            newMonster.getId());

            applyEnemyMetadata(
                    result,
                    enemyOutcome);

            return result;
        }

        playerMonsterRepository.save(
                newMonster);

        wildEncounterRepository.save(
                encounter);

        BattleResult result =
                new BattleResult(
                        "Đã đổi sang "
                                + newMonster.getMonster().getName()
                                + ". "
                                + encounter.getMonster().getName()
                                + " phản công gây "
                                + enemyDamage
                                + " damage. "
                                + "HP còn "
                                + newHp
                                + ".",
                        false,
                        false,
                        newMonster.getId());

        applyEnemyMetadata(
                result,
                enemyOutcome);

        return result;
    }

    // =====================================================
    // USE ITEM
    // =====================================================

    @Transactional
    public BattleResult useItem(
            String username,
            Long activePlayerMonsterId,
            Long targetPlayerMonsterId,
            Long wildEncounterId,
            String itemCode) {

        User user =
                userRepository.findByUsername(
                        username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi.");
        }

        PlayerMonster activeMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                activePlayerMonsterId,
                                user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster hiện tại không hợp lệ."));

        PlayerMonster targetMonster =
                playerMonsterRepository
                        .findByIdAndUser(
                                targetPlayerMonsterId,
                                user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Monster mục tiêu không hợp lệ."));

        WildEncounter encounter =
                wildEncounterRepository
                        .findById(wildEncounterId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy Battle."));

        validateEncounterOwner(
                encounter,
                user);

        if (!encounter.isActive()) {

            return new BattleResult(
                    "Battle đã kết thúc.",
                    false,
                    false,
                    activeMonster.getId());
        }

        if (activeMonster.getCurrentHp() <= 0) {

            return new BattleResult(
                    "Monster hiện tại đã bị hạ.",
                    false,
                    true,
                    activeMonster.getId());
        }

        // =================================================
        // CHECK TEAM
        // =================================================

        if (!isMonsterInTeam(
                username,
                targetMonster.getId())) {

            throw new RuntimeException(
                    "Monster mục tiêu không nằm trong Team.");
        }

        // =================================================
        // ITEM
        // =================================================

        consumeItemOrThrow(
                username,
                itemCode);

        String normalized =
                itemCode == null
                        ? ""
                        : itemCode.trim()
                                .toUpperCase();

        int maxHp =
                statService.calculatePlayerMaxHp(
                        targetMonster);

        int oldHp =
                targetMonster.getCurrentHp();

        // =================================================
        // POTION
        // =================================================

        if ("POTION".equals(normalized)) {

            int newHp =
                    Math.min(
                            maxHp,
                            oldHp + 30);

            targetMonster.setCurrentHp(
                    newHp);
        }

        // =================================================
        // SUPER POTION
        // =================================================

        else if ("SUPER_POTION".equals(normalized)) {

            int newHp =
                    Math.min(
                            maxHp,
                            oldHp + 100);

            targetMonster.setCurrentHp(
                    newHp);
        }

        // =================================================
        // REVIVE
        // =================================================

        else if ("REVIVE".equals(normalized)) {

            if (oldHp > 0) {

                throw new RuntimeException(
                        "Monster chưa bị hạ.");
            }

            targetMonster.setCurrentHp(
                    Math.max(
                            1,
                            maxHp / 2));
        }

        else {

            throw new RuntimeException(
                    "Item không được hỗ trợ trong Battle: "
                            + itemCode);
        }

        playerMonsterRepository.save(
                targetMonster);

        // =================================================
        // WILD COUNTER ATTACK
        // =================================================

        AttackOutcome enemyOutcome =
                performWildAttack(
                        encounter,
                        activeMonster);

        int enemyDamage =
                enemyOutcome.getDamage();

        int activeOldHp =
                activeMonster.getCurrentHp();

        int activeNewHp =
                Math.max(
                        0,
                        activeOldHp
                                - enemyDamage);

        activeMonster.setCurrentHp(
                activeNewHp);

        if (activeNewHp <= 0) {

            activeMonster.setCurrentHp(0);

            encounter.setActive(false);

            playerMonsterRepository.save(
                    activeMonster);

            wildEncounterRepository.save(
                    encounter);

            BattleResult result =
                    new BattleResult(
                            "Dùng "
                                    + itemCode
                                    + " thành công nhưng "
                                    + encounter.getMonster().getName()
                                    + " phản công gây "
                                    + enemyDamage
                                    + " damage. Monster đang chiến đấu đã bị hạ.",
                            false,
                            true,
                            targetMonster.getId());

            applyEnemyMetadata(
                    result,
                    enemyOutcome);

            return result;
        }

        playerMonsterRepository.save(
                activeMonster);

        wildEncounterRepository.save(
                encounter);

        BattleResult result =
                new BattleResult(
                        "Đã sử dụng "
                                + itemCode
                                + ". "
                                + "Monster được hồi HP. "
                                + encounter.getMonster().getName()
                                + " phản công gây "
                                + enemyDamage
                                + " damage.",
                        false,
                        false,
                        targetMonster.getId());

        applyEnemyMetadata(
                result,
                enemyOutcome);

        return result;
    }

    // =====================================================
    // CHECK MONSTER IN TEAM
    // =====================================================

    private boolean isMonsterInTeam(
            String username,
            Long playerMonsterId) {

        List<TeamMember> members =
                teamService.getTeamMembers(
                        username);

        for (TeamMember member :
                members) {

            if (member.getPlayerMonster()
                    .getId()
                    .equals(playerMonsterId)) {

                return true;
            }
        }

        return false;
    }

    // =====================================================
    // CONSUME ITEM
    // =====================================================

    private void consumeItemOrThrow(
            String username,
            String itemCode) {

        int quantity =
                inventoryService.getQuantity(
                        username,
                        itemCode);

        if (quantity <= 0) {

            throw new RuntimeException(
                    "Không còn item: "
                            + itemCode);
        }

        inventoryService.consumeItem(
                username,
                itemCode,
                1);
    }

    // =====================================================
    // VALIDATE ENCOUNTER OWNER
    // =====================================================

    private void validateEncounterOwner(
            WildEncounter encounter,
            User user) {

        if (encounter.getUser() == null
                || encounter.getUser().getId() == null
                || !encounter.getUser()
                        .getId()
                        .equals(user.getId())) {

            throw new RuntimeException(
                    "Battle không thuộc tài khoản.");
        }
    }

    // =====================================================
    // DAMAGE
    // =====================================================

    private AttackOutcome calculateDamage(
            int level,
            int attack,
            int defense,
            int power,
            String attackType,
            String attackerMonsterType,
            String defenderType) {

        if (defense <= 0) {
            defense = 1;
        }

        double base =
                (((2.0 * level / 5.0) + 2)
                        * power
                        * (attack / (double) defense)
                        / 50.0)
                        + 2;

        // =================================================
        // STAB
        // =================================================

        double stab = 1.0;

        if (normalizeType(attackType)
                .equals(
                        normalizeType(
                                attackerMonsterType))) {

            stab = 1.5;
        }

        // =================================================
        // TYPE
        // =================================================

        double typeMultiplier =
                getTypeMultiplier(
                        attackType,
                        defenderType);

        // =================================================
        // CRITICAL
        // =================================================

        boolean critical =
                random.nextInt(100) < 6;

        double criticalMultiplier =
                critical ? 1.5 : 1.0;

        // =================================================
        // RANDOM
        // =================================================

        double randomMultiplier =
                0.85
                        + random.nextDouble()
                        * 0.15;

        double damage =
                base
                        * stab
                        * typeMultiplier
                        * criticalMultiplier
                        * randomMultiplier;

        int finalDamage =
                Math.max(
                        1,
                        (int) damage);

        return new AttackOutcome(
                finalDamage,
                critical,
                typeMultiplier);
    }

    // =====================================================
    // PLAYER METADATA
    // =====================================================

    private void applyPlayerMetadata(
            BattleResult result,
            AttackOutcome outcome) {

        if (result == null
                || outcome == null) {

            return;
        }

        result.setPlayerCritical(
                outcome.isCritical());

        result.setPlayerTypeMultiplier(
                outcome.getTypeMultiplier());
    }

    // =====================================================
    // ENEMY METADATA
    // =====================================================

    private void applyEnemyMetadata(
            BattleResult result,
            AttackOutcome outcome) {

        if (result == null
                || outcome == null) {

            return;
        }

        result.setEnemyCritical(
                outcome.isCritical());

        result.setEnemyTypeMultiplier(
                outcome.getTypeMultiplier());
    }

    // =====================================================
    // NORMALIZE TYPE
    // =====================================================

    private String normalizeType(
            String type) {

        if (type == null) {
            return "NORMAL";
        }

        if (type.equalsIgnoreCase(
                "GRASS")) {

            return "NATURE";
        }

        return type.toUpperCase();
    }

    // =====================================================
    // TYPE EFFECTIVENESS
    // =====================================================

    private double getTypeMultiplier(
            String attackType,
            String defenderType) {

        String attack =
                normalizeType(
                        attackType);

        String defender =
                normalizeType(
                        defenderType);

        // FIRE > NATURE

        if (attack.equals("FIRE")
                && defender.equals("NATURE")) {

            return 2.0;
        }

        // WATER > FIRE

        if (attack.equals("WATER")
                && defender.equals("FIRE")) {

            return 2.0;
        }

        // NATURE > WATER

        if (attack.equals("NATURE")
                && defender.equals("WATER")) {

            return 2.0;
        }

        // FIRE < WATER

        if (attack.equals("FIRE")
                && defender.equals("WATER")) {

            return 0.5;
        }

        // WATER < NATURE

        if (attack.equals("WATER")
                && defender.equals("NATURE")) {

            return 0.5;
        }

        // NATURE < FIRE

        if (attack.equals("NATURE")
                && defender.equals("FIRE")) {

            return 0.5;
        }

        return 1.0;
    }
}