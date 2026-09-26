package com.aetherlia.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aetherlia.battle.BattleResult;
import com.aetherlia.battle.BattleSkill;
import com.aetherlia.battle.BattleStartData;
import com.aetherlia.battle.CaptureResult;
import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.entity.User;
import com.aetherlia.entity.WildEncounter;
import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.repository.WildEncounterRepository;
import com.aetherlia.service.BattleService;
import com.aetherlia.service.CaptureService;
import com.aetherlia.service.InventoryService;
import com.aetherlia.service.StatService;
import com.aetherlia.service.TeamService;

@Controller
public class BattleController {

    private final BattleService battleService;
    private final PlayerMonsterRepository playerMonsterRepository;
    private final WildEncounterRepository wildEncounterRepository;
    private final UserRepository userRepository;
    private final CaptureService captureService;
    private final InventoryService inventoryService;
    private final StatService statService;
    private final TeamService teamService;

    public BattleController(
            BattleService battleService,
            PlayerMonsterRepository playerMonsterRepository,
            WildEncounterRepository wildEncounterRepository,
            UserRepository userRepository,
            CaptureService captureService,
            InventoryService inventoryService,
            StatService statService,
            TeamService teamService) {

        this.battleService = battleService;
        this.playerMonsterRepository = playerMonsterRepository;
        this.wildEncounterRepository = wildEncounterRepository;
        this.userRepository = userRepository;
        this.captureService = captureService;
        this.inventoryService = inventoryService;
        this.statService = statService;
        this.teamService = teamService;
    }

    // =====================================================
    // BẮT ĐẦU BATTLE MỚI
    // =====================================================

    @GetMapping("/battle/start")
    public String startBattle(HttpSession session) {

        clearBattleSession(session);

        return "redirect:/battle";
    }

    // =====================================================
    // MỞ BATTLE
    // =====================================================

    @GetMapping("/battle")
    public String battle(
            Authentication authentication,
            HttpSession session,
            Model model) {

        String username = authentication.getName();

        // =================================================
        // USER
        // =================================================

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("Không tìm thấy người chơi.");
        }

        // =================================================
        // LẤY BATTLE SESSION
        // =================================================

        Long playerMonsterId =
                (Long) session.getAttribute("battlePlayerMonsterId");

        Long wildEncounterId =
                (Long) session.getAttribute("battleWildEncounterId");

        // =================================================
        // NẾU CHƯA CÓ BATTLE SESSION
        // =================================================

        boolean needNewBattle =
                playerMonsterId == null || wildEncounterId == null;

        if (needNewBattle) {

            Optional<WildEncounter> activeEncounter =
                    wildEncounterRepository.findByUserAndActiveTrue(user);

            if (activeEncounter.isEmpty()) {
                return "redirect:/map/1";
            }

            BattleStartData start =
                    battleService.prepareBattle(username);

            playerMonsterId = start.getPlayerMonsterId();
            wildEncounterId = start.getWildEncounterId();

            session.setAttribute(
                    "battlePlayerMonsterId",
                    playerMonsterId);

            session.setAttribute(
                    "battleWildEncounterId",
                    wildEncounterId);
        }

        // =================================================
        // PLAYER MONSTER
        // =================================================

        PlayerMonster playerMonster =
                playerMonsterRepository
                        .findByIdAndUser(playerMonsterId, user)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy Monster của người chơi."));

        // =================================================
        // WILD ENCOUNTER
        // =================================================

        WildEncounter encounter =
                wildEncounterRepository
                        .findById(wildEncounterId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Không tìm thấy Monster hoang dã."));

        // =================================================
        // KIỂM TRA QUYỀN SỞ HỮU ENCOUNTER
        // =================================================

        if (encounter.getUser() == null
                || encounter.getUser().getId() == null
                || !encounter.getUser()
                        .getId()
                        .equals(user.getId())) {

            clearBattleSession(session);

            return "redirect:/map/1";
        }

        // =================================================
        // BATTLE RESULT
        // =================================================

        Boolean victory =
                (Boolean) session.getAttribute("battleVictory");

        Boolean defeat =
                (Boolean) session.getAttribute("battleDefeat");

        String captureMessage =
                (String) session.getAttribute("battleCaptureMessage");

        Boolean captureSuccess =
                (Boolean) session.getAttribute("battleCaptureSuccess");

        if (victory == null) {
            victory = false;
        }

        if (defeat == null) {
            defeat = false;
        }

        if (captureSuccess == null) {
            captureSuccess = false;
        }

        // =================================================
        // KIỂM TRA ENCOUNTER
        // =================================================

        boolean hasBattleResult =
                Boolean.TRUE.equals(victory)
                        || Boolean.TRUE.equals(defeat);

        if (!encounter.isActive() && !hasBattleResult) {

            clearBattleSession(session);

            return "redirect:/map/1";
        }

        // =================================================
        // MAX HP
        // =================================================

        int playerMaxHp =
                statService.calculatePlayerMaxHp(playerMonster);

        int wildMaxHp =
                statService.calculateWildMaxHp(
                        encounter.getMonster(),
                        encounter.getLevel());

        // =================================================
        // SKILLS
        // =================================================

        List<BattleSkill> skills =
                battleService.getAvailableSkills();

        // =================================================
        // TEAM
        // =================================================

        List<TeamMember> teamMembers =
                teamService.getTeamMembers(username);

        // =================================================
        // MESSAGE
        // =================================================

        String message =
                (String) session.getAttribute("battleMessage");

        // Đọc xong thì xóa message cũ
        session.removeAttribute("battleMessage");

        // =================================================
        // ORBS
        // =================================================

        int basicOrb =
                inventoryService.getQuantity(
                        username,
                        "BASIC_ORB");

        int greatOrb =
                inventoryService.getQuantity(
                        username,
                        "GREAT_ORB");

        int ultraOrb =
                inventoryService.getQuantity(
                        username,
                        "ULTRA_ORB");

        int masterOrb =
                inventoryService.getQuantity(
                        username,
                        "MASTER_ORB");

        // =================================================
        // HEALING
        // =================================================

        int potion =
                inventoryService.getQuantity(
                        username,
                        "POTION");

        int superPotion =
                inventoryService.getQuantity(
                        username,
                        "SUPER_POTION");

        int revive =
                inventoryService.getQuantity(
                        username,
                        "REVIVE");

        // =================================================
        // MODEL - MONSTER
        // =================================================

        model.addAttribute(
                "playerMonster",
                playerMonster);

        model.addAttribute(
                "encounter",
                encounter);

        // =================================================
        // MODEL - HP
        // =================================================

        model.addAttribute(
                "playerMaxHp",
                playerMaxHp);

        model.addAttribute(
                "wildMaxHp",
                wildMaxHp);

        // =================================================
        // MODEL - SKILLS
        // =================================================

        model.addAttribute(
                "skills",
                skills);

        // =================================================
        // MODEL - TEAM
        // =================================================

        model.addAttribute(
                "teamMembers",
                teamMembers);

        // =================================================
        // MODEL - MESSAGE
        // =================================================

        model.addAttribute(
                "message",
                message);

        model.addAttribute(
                "victory",
                victory);

        model.addAttribute(
                "defeat",
                defeat);

        model.addAttribute(
                "captureMessage",
                captureMessage);

        model.addAttribute(
                "captureSuccess",
                captureSuccess);

        // =================================================
        // MODEL - ORBS
        // =================================================

        model.addAttribute(
                "basicOrb",
                basicOrb);

        model.addAttribute(
                "greatOrb",
                greatOrb);

        model.addAttribute(
                "ultraOrb",
                ultraOrb);

        model.addAttribute(
                "masterOrb",
                masterOrb);

        // =================================================
        // MODEL - HEALING
        // =================================================

        model.addAttribute(
                "potion",
                potion);

        model.addAttribute(
                "superPotion",
                superPotion);

        model.addAttribute(
                "revive",
                revive);

        return "battle";
    }

    // =====================================================
    // PLAYER DÙNG SKILL - CÁCH CŨ
    // =====================================================

    @PostMapping("/battle/attack")
    public String attack(
            @RequestParam String skillId,
            Authentication authentication,
            HttpSession session) {

        String username = authentication.getName();

        Long playerMonsterId =
                (Long) session.getAttribute(
                        "battlePlayerMonsterId");

        Long wildEncounterId =
                (Long) session.getAttribute(
                        "battleWildEncounterId");

        if (playerMonsterId == null
                || wildEncounterId == null) {

            return "redirect:/battle";
        }

        session.removeAttribute("battleCaptureMessage");
        session.removeAttribute("battleCaptureSuccess");

        BattleResult result =
                battleService.playerAttack(
                        username,
                        playerMonsterId,
                        wildEncounterId,
                        skillId);

        session.setAttribute(
                "battleMessage",
                result.getMessage());

        session.setAttribute(
                "battleVictory",
                result.isVictory());

        session.setAttribute(
                "battleDefeat",
                result.isDefeat());

        if (result.getPlayerMonsterId() != null) {

            session.setAttribute(
                    "battlePlayerMonsterId",
                    result.getPlayerMonsterId());
        }

        return "redirect:/battle";
    }

    // =====================================================
    // API ATTACK CHO BATTLE 3D
    // =====================================================

    @PostMapping("/battle/api/attack")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiAttack(
            @RequestParam String skillId,
            Authentication authentication,
            HttpSession session) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        try {

            String username =
                    authentication.getName();

            // =================================================
            // LẤY SESSION
            // =================================================

            Long playerMonsterId =
                    (Long) session.getAttribute(
                            "battlePlayerMonsterId");

            Long wildEncounterId =
                    (Long) session.getAttribute(
                            "battleWildEncounterId");

            if (playerMonsterId == null
                    || wildEncounterId == null) {

                response.put("success", false);
                response.put(
                        "message",
                        "Battle session không tồn tại.");

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            // =================================================
            // USER
            // =================================================

            User user =
                    userRepository.findByUsername(username);

            if (user == null) {

                response.put("success", false);
                response.put(
                        "message",
                        "Không tìm thấy người chơi.");

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            // =================================================
            // KIỂM TRA PLAYER MONSTER
            // =================================================

            PlayerMonster playerBefore =
                    playerMonsterRepository
                            .findByIdAndUser(
                                    playerMonsterId,
                                    user)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Monster của người chơi."));

            // =================================================
            // KIỂM TRA WILD
            // =================================================

            WildEncounter encounterBefore =
                    wildEncounterRepository
                            .findById(wildEncounterId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Wild Monster."));

            // =================================================
            // KIỂM TRA QUYỀN SỞ HỮU WILD
            // =================================================

            if (encounterBefore.getUser() == null
                    || encounterBefore.getUser().getId() == null
                    || !encounterBefore.getUser()
                            .getId()
                            .equals(user.getId())) {

                response.put("success", false);
                response.put(
                        "message",
                        "Wild Encounter không thuộc người chơi.");

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            // =================================================
            // HP TRƯỚC KHI ĐÁNH
            // =================================================

            int playerHpBefore =
                    Math.max(
                            0,
                            playerBefore.getCurrentHp());

            int wildHpBefore =
                    Math.max(
                            0,
                            encounterBefore.getCurrentHp());

            int playerMaxHp =
                    statService.calculatePlayerMaxHp(
                            playerBefore);

            int wildMaxHp =
                    statService.calculateWildMaxHp(
                            encounterBefore.getMonster(),
                            encounterBefore.getLevel());

            // =================================================
            // XÓA MESSAGE CAPTURE CŨ
            // =================================================

            session.removeAttribute(
                    "battleCaptureMessage");

            session.removeAttribute(
                    "battleCaptureSuccess");

            // =================================================
            // PLAYER ĐÁNH WILD
            //
            // BattleService xử lý:
            // PLAYER -> WILD
            // sau đó WILD -> PLAYER
            // =================================================

            BattleResult result =
                    battleService.playerAttack(
                            username,
                            playerMonsterId,
                            wildEncounterId,
                            skillId);

            // =================================================
            // PLAYER MONSTER ID MỚI
            // =================================================

            if (result.getPlayerMonsterId() != null) {

                session.setAttribute(
                        "battlePlayerMonsterId",
                        result.getPlayerMonsterId());

                playerMonsterId =
                        result.getPlayerMonsterId();
            }

            // =================================================
            // LẤY HP SAU KHI XỬ LÝ LƯỢT ĐÁNH
            // =================================================

            PlayerMonster playerAfter =
                    playerMonsterRepository
                            .findByIdAndUser(
                                    playerMonsterId,
                                    user)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Monster sau lượt đánh."));

            WildEncounter encounterAfter =
                    wildEncounterRepository
                            .findById(wildEncounterId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Wild Monster sau lượt đánh."));

            int playerHpAfter =
                    Math.max(
                            0,
                            playerAfter.getCurrentHp());

            int wildHpAfter =
                    Math.max(
                            0,
                            encounterAfter.getCurrentHp());

            // =================================================
            // DAMAGE
            //
            // PLAYER DAMAGE:
            // HP WILD trước - HP WILD sau
            //
            // ENEMY DAMAGE:
            // HP PLAYER trước - HP PLAYER sau
            // =================================================

            int playerDamage =
                    Math.max(
                            0,
                            wildHpBefore - wildHpAfter);

            int enemyDamage =
                    Math.max(
                            0,
                            playerHpBefore - playerHpAfter);

            // =================================================
            // LƯU BATTLE RESULT
            // =================================================

            session.setAttribute(
                    "battleMessage",
                    result.getMessage());

            session.setAttribute(
                    "battleVictory",
                    result.isVictory());

            session.setAttribute(
                    "battleDefeat",
                    result.isDefeat());

            // =================================================
            // RESPONSE
            // =================================================

            response.put(
                    "success",
                    true);

            response.put(
                    "message",
                    result.getMessage());

            // PLAYER gây damage cho WILD
            response.put(
                    "playerDamage",
                    playerDamage);

            // WILD gây damage cho PLAYER
            response.put(
                    "enemyDamage",
                    enemyDamage);

            // =================================================
            // HP
            // =================================================

            response.put(
                    "playerHpBefore",
                    playerHpBefore);

            response.put(
                    "wildHpBefore",
                    wildHpBefore);

            response.put(
                    "playerHp",
                    playerHpAfter);

            response.put(
                    "wildHp",
                    wildHpAfter);

            response.put(
                    "playerMaxHp",
                    playerMaxHp);

            response.put(
                    "wildMaxHp",
                    wildMaxHp);

            // =================================================
            // KẾT QUẢ
            // =================================================

            response.put(
                    "victory",
                    result.isVictory());

            response.put(
                    "defeat",
                    result.isDefeat());

            response.put(
                    "playerMonsterId",
                    playerMonsterId);

            // =================================================
            // CRITICAL / TYPE
            // =================================================

            response.put(
                    "playerCritical",
                    result.isPlayerCritical());

            response.put(
                    "playerTypeMultiplier",
                    result.getPlayerTypeMultiplier());

            response.put(
                    "enemyCritical",
                    result.isEnemyCritical());

            response.put(
                    "enemyTypeMultiplier",
                    result.getEnemyTypeMultiplier());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            response.put(
                    "success",
                    false);

            response.put(
                    "message",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Không thể xử lý lượt đánh.");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    // =====================================================
    // API CAPTURE CHO BATTLE 3D
    // =====================================================

    @PostMapping("/battle/api/capture")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiCapture(
            @RequestParam(defaultValue = "BASIC_ORB") String orbType,
            Authentication authentication,
            HttpSession session) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        try {

            String username =
                    authentication.getName();

            // =================================================
            // LẤY SESSION
            // =================================================

            Long playerMonsterId =
                    (Long) session.getAttribute(
                            "battlePlayerMonsterId");

            Long wildEncounterId =
                    (Long) session.getAttribute(
                            "battleWildEncounterId");

            if (playerMonsterId == null
                    || wildEncounterId == null) {

                response.put("success", false);
                response.put(
                        "message",
                        "Battle session không tồn tại.");

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            // =================================================
            // USER
            // =================================================

            User user =
                    userRepository.findByUsername(username);

            if (user == null) {

                response.put("success", false);
                response.put(
                        "message",
                        "Không tìm thấy người chơi.");

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            // =================================================
            // PLAYER TRƯỚC KHI CAPTURE
            // =================================================

            PlayerMonster playerBefore =
                    playerMonsterRepository
                            .findByIdAndUser(
                                    playerMonsterId,
                                    user)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Monster của người chơi."));

            int playerHpBefore =
                    Math.max(
                            0,
                            playerBefore.getCurrentHp());

            int playerMaxHp =
                    statService.calculatePlayerMaxHp(
                            playerBefore);

            // =================================================
            // WILD TRƯỚC KHI CAPTURE
            // =================================================

            WildEncounter encounterBefore =
                    wildEncounterRepository
                            .findById(wildEncounterId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Wild Monster."));

            int wildHpBefore =
                    Math.max(
                            0,
                            encounterBefore.getCurrentHp());

            int wildMaxHp =
                    statService.calculateWildMaxHp(
                            encounterBefore.getMonster(),
                            encounterBefore.getLevel());

            // =================================================
            // CAPTURE
            // =================================================

            CaptureResult result =
                    captureService.capture(
                            username,
                            wildEncounterId,
                            orbType);

            // =================================================
            // REQUEST THÀNH CÔNG
            // =================================================

            response.put(
                    "success",
                    true);

            response.put(
                    "captureSuccess",
                    result.isSuccess());

            response.put(
                    "message",
                    result.getMessage());

            response.put(
                    "orbType",
                    orbType);

            response.put(
                    "shakes",
                    result.getShakes());

            // =================================================
            // CAPTURE THÀNH CÔNG
            // =================================================

            if (result.isSuccess()) {

                response.put(
                        "enemyTurn",
                        false);

                response.put(
                        "enemyDamage",
                        0);

                response.put(
                        "playerHpBefore",
                        playerHpBefore);

                response.put(
                        "playerHp",
                        playerHpBefore);

                response.put(
                        "playerMaxHp",
                        playerMaxHp);

                response.put(
                        "wildHpBefore",
                        wildHpBefore);

                response.put(
                        "wildHp",
                        wildHpBefore);

                response.put(
                        "wildMaxHp",
                        wildMaxHp);

                response.put(
                        "defeat",
                        false);

                response.put(
                        "redirectUrl",
                        "/my-monsters");

                clearBattleSession(session);

                return ResponseEntity.ok(response);
            }

            // =================================================
            // CAPTURE THẤT BẠI
            // WILD PHẢN CÔNG PLAYER
            // =================================================

            session.setAttribute(
                    "battleCaptureMessage",
                    result.getMessage());

            session.setAttribute(
                    "battleCaptureSuccess",
                    false);

            BattleResult enemyResult =
                    battleService.enemyTurnAfterCapture(
                            username,
                            playerMonsterId,
                            wildEncounterId);

            // =================================================
            // PLAYER SAU PHẢN CÔNG
            // =================================================

            if (enemyResult.getPlayerMonsterId() != null) {

                playerMonsterId =
                        enemyResult.getPlayerMonsterId();

                session.setAttribute(
                        "battlePlayerMonsterId",
                        playerMonsterId);
            }

            PlayerMonster playerAfter =
                    playerMonsterRepository
                            .findByIdAndUser(
                                    playerMonsterId,
                                    user)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Monster sau phản công."));

            WildEncounter encounterAfter =
                    wildEncounterRepository
                            .findById(wildEncounterId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Wild Monster sau phản công."));

            int playerHpAfter =
                    Math.max(
                            0,
                            playerAfter.getCurrentHp());

            int wildHpAfter =
                    Math.max(
                            0,
                            encounterAfter.getCurrentHp());

            int enemyDamage =
                    Math.max(
                            0,
                            playerHpBefore - playerHpAfter);

            // =================================================
            // CẬP NHẬT BATTLE RESULT
            // =================================================

            session.setAttribute(
                    "battleMessage",
                    enemyResult.getMessage());

            session.setAttribute(
                    "battleVictory",
                    enemyResult.isVictory());

            session.setAttribute(
                    "battleDefeat",
                    enemyResult.isDefeat());

            // =================================================
            // RESPONSE
            // =================================================

            response.put(
                    "enemyTurn",
                    true);

            response.put(
                    "enemyDamage",
                    enemyDamage);

            response.put(
                    "playerHpBefore",
                    playerHpBefore);

            response.put(
                    "playerHp",
                    playerHpAfter);

            response.put(
                    "playerMaxHp",
                    playerMaxHp);

            response.put(
                    "wildHpBefore",
                    wildHpBefore);

            response.put(
                    "wildHp",
                    wildHpAfter);

            response.put(
                    "wildMaxHp",
                    wildMaxHp);

            response.put(
                    "defeat",
                    enemyResult.isDefeat());

            response.put(
                    "playerMonsterId",
                    playerMonsterId);

            // =================================================
            // CRITICAL / TYPE CỦA PHẢN CÔNG
            // =================================================

            response.put(
                    "enemyCritical",
                    enemyResult.isEnemyCritical());

            response.put(
                    "enemyTypeMultiplier",
                    enemyResult.getEnemyTypeMultiplier());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            response.put(
                    "success",
                    false);

            response.put(
                    "message",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Không thể thực hiện Capture.");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    // =====================================================
    // DÙNG ITEM
    // =====================================================

    @PostMapping("/battle/item")
    public String useItem(
            @RequestParam String itemCode,
            @RequestParam Long targetPlayerMonsterId,
            Authentication authentication,
            HttpSession session) {

        String username =
                authentication.getName();

        Long activePlayerMonsterId =
                (Long) session.getAttribute(
                        "battlePlayerMonsterId");

        Long wildEncounterId =
                (Long) session.getAttribute(
                        "battleWildEncounterId");

        if (activePlayerMonsterId == null
                || wildEncounterId == null) {

            return "redirect:/battle";
        }

        session.removeAttribute(
                "battleCaptureMessage");

        session.removeAttribute(
                "battleCaptureSuccess");

        BattleResult result =
                battleService.useItem(
                        username,
                        activePlayerMonsterId,
                        targetPlayerMonsterId,
                        wildEncounterId,
                        itemCode);

        if (result.getPlayerMonsterId() != null) {

            session.setAttribute(
                    "battlePlayerMonsterId",
                    result.getPlayerMonsterId());
        }

        session.setAttribute(
                "battleMessage",
                result.getMessage());

        session.setAttribute(
                "battleVictory",
                result.isVictory());

        session.setAttribute(
                "battleDefeat",
                result.isDefeat());

        return "redirect:/battle";
    }

    // =====================================================
    // SWITCH MONSTER
    // =====================================================

    @PostMapping("/battle/switch")
    public String switchMonster(
            @RequestParam Long playerMonsterId,
            Authentication authentication,
            HttpSession session) {

        String username =
                authentication.getName();

        Long wildEncounterId =
                (Long) session.getAttribute(
                        "battleWildEncounterId");

        if (wildEncounterId == null) {

            return "redirect:/battle";
        }

        session.removeAttribute(
                "battleCaptureMessage");

        session.removeAttribute(
                "battleCaptureSuccess");

        BattleResult result =
                battleService.switchMonster(
                        username,
                        playerMonsterId,
                        wildEncounterId);

        if (result.getPlayerMonsterId() != null) {

            session.setAttribute(
                    "battlePlayerMonsterId",
                    result.getPlayerMonsterId());
        }

        session.setAttribute(
                "battleMessage",
                result.getMessage());

        session.setAttribute(
                "battleVictory",
                result.isVictory());

        session.setAttribute(
                "battleDefeat",
                result.isDefeat());

        return "redirect:/battle";
    }

    // =====================================================
    // CAPTURE - CÁCH CŨ
    // =====================================================

    @PostMapping("/battle/capture")
    public String capture(
            @RequestParam(defaultValue = "BASIC_ORB") String orbType,
            Authentication authentication,
            HttpSession session) {

        String username =
                authentication.getName();

        Long playerMonsterId =
                (Long) session.getAttribute(
                        "battlePlayerMonsterId");

        Long wildEncounterId =
                (Long) session.getAttribute(
                        "battleWildEncounterId");

        if (playerMonsterId == null
                || wildEncounterId == null) {

            return "redirect:/battle";
        }

        // =================================================
        // CAPTURE
        // =================================================

        CaptureResult result =
                captureService.capture(
                        username,
                        wildEncounterId,
                        orbType);

        // =================================================
        // THÀNH CÔNG
        // =================================================

        if (result.isSuccess()) {

            clearBattleSession(session);

            return "redirect:/my-monsters";
        }

        // =================================================
        // THẤT BẠI
        // =================================================

        session.setAttribute(
                "battleCaptureMessage",
                result.getMessage());

        session.setAttribute(
                "battleCaptureSuccess",
                false);

        // =================================================
        // WILD PHẢN CÔNG
        // =================================================

        BattleResult enemyResult =
                battleService.enemyTurnAfterCapture(
                        username,
                        playerMonsterId,
                        wildEncounterId);

        // =================================================
        // CẬP NHẬT PLAYER MONSTER ID
        // =================================================

        if (enemyResult.getPlayerMonsterId() != null) {

            session.setAttribute(
                    "battlePlayerMonsterId",
                    enemyResult.getPlayerMonsterId());
        }

        // =================================================
        // CẬP NHẬT BATTLE RESULT
        // =================================================

        session.setAttribute(
                "battleMessage",
                enemyResult.getMessage());

        session.setAttribute(
                "battleVictory",
                enemyResult.isVictory());

        session.setAttribute(
                "battleDefeat",
                enemyResult.isDefeat());

        return "redirect:/battle";
    }

    // =====================================================
    // RỜI BATTLE
    // =====================================================

    @GetMapping("/battle/leave")
    public String leaveBattle(
            HttpSession session) {

        clearBattleSession(session);

        return "redirect:/map/1";
    }

    // =====================================================
    // CHẠY KHỎI BATTLE
    // =====================================================

    @GetMapping("/battle/run")
    public String runBattle(
            Authentication authentication,
            HttpSession session) {

        String username =
                authentication.getName();

        Long wildEncounterId =
                (Long) session.getAttribute(
                        "battleWildEncounterId");

        if (wildEncounterId != null) {

            User user =
                    userRepository.findByUsername(username);

            if (user != null) {

                Optional<WildEncounter> optionalEncounter =
                        wildEncounterRepository
                                .findById(wildEncounterId);

                if (optionalEncounter.isPresent()) {

                    WildEncounter encounter =
                            optionalEncounter.get();

                    if (encounter.getUser() != null
                            && encounter.getUser().getId() != null
                            && encounter.getUser()
                                    .getId()
                                    .equals(user.getId())) {

                        encounter.setActive(false);

                        wildEncounterRepository
                                .save(encounter);
                    }
                }
            }
        }

        clearBattleSession(session);

        return "redirect:/map/1";
    }

    // =====================================================
    // XÓA BATTLE SESSION
    // =====================================================

    private void clearBattleSession(
            HttpSession session) {

        session.removeAttribute(
                "battlePlayerMonsterId");

        session.removeAttribute(
                "battleWildEncounterId");

        session.removeAttribute(
                "battleMessage");

        session.removeAttribute(
                "battleVictory");

        session.removeAttribute(
                "battleDefeat");

        session.removeAttribute(
                "battleCaptureMessage");

        session.removeAttribute(
                "battleCaptureSuccess");
    }
}