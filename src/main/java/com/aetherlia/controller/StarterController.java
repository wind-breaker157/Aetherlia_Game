package com.aetherlia.controller;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.User;
import com.aetherlia.repository.PlayerMonsterRepository;
import com.aetherlia.repository.UserRepository;
import com.aetherlia.service.TeamService;

@Controller
public class StarterController {

    private final UserRepository userRepository;
    private final PlayerMonsterRepository playerMonsterRepository;
    private final TeamService teamService;
    private final EntityManager entityManager;

    public StarterController(
            UserRepository userRepository,
            PlayerMonsterRepository playerMonsterRepository,
            TeamService teamService,
            EntityManager entityManager) {

        this.userRepository = userRepository;
        this.playerMonsterRepository = playerMonsterRepository;
        this.teamService = teamService;
        this.entityManager = entityManager;
    }

    // =====================================================
    // TRANG CHỌN MONSTER KHỞI ĐẦU
    // =====================================================

    @GetMapping("/starter")
    public String showStarterPage(
            Authentication authentication,
            Model model) {

        // Phải đăng nhập mới được chọn starter
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getName())) {

            return "redirect:/login";
        }

        String username = authentication.getName();

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            return "redirect:/login";
        }

        // =================================================
        // KIỂM TRA USER ĐÃ CÓ MONSTER CHƯA
        // =================================================

        List<PlayerMonster> monsters =
                playerMonsterRepository.findByUser(user);

        // Đã có Monster -> không cho chọn lại
        if (!monsters.isEmpty()) {
            return "redirect:/dashboard";
        }

        // =================================================
        // LẤY 3 MONSTER STARTER TRONG DATABASE
        //
        // ID 1 = Spriglet
        // ID 2 = Pyron
        // ID 3 = Aquaff
        // =================================================

        List<Monster> starters =
                entityManager
                        .createQuery(
                                """
                                SELECT m
                                FROM Monster m
                                WHERE m.id IN (1, 2, 3)
                                ORDER BY m.id
                                """,
                                Monster.class
                        )
                        .getResultList();

        model.addAttribute(
                "starters",
                starters
        );

        return "starter";
    }

    // =====================================================
    // XỬ LÝ CHỌN MONSTER
    // =====================================================

    @PostMapping("/starter/choose")
    @Transactional
    public String chooseStarter(
            @RequestParam("starterId") Long starterId,
            Authentication authentication) {

        // =================================================
        // PHẢI ĐĂNG NHẬP
        // =================================================

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getName())) {

            return "redirect:/login";
        }

        String username = authentication.getName();

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            return "redirect:/login";
        }

        // =================================================
        // KHÔNG CHO CHỌN STARTER LẦN 2
        // =================================================

        List<PlayerMonster> existingMonsters =
                playerMonsterRepository.findByUser(user);

        if (!existingMonsters.isEmpty()) {
            return "redirect:/dashboard";
        }

        // =================================================
        // KIỂM TRA ID
        // =================================================

        if (starterId == null
                || (starterId != 1L
                && starterId != 2L
                && starterId != 3L)) {

            throw new RuntimeException(
                    "Monster khởi đầu không hợp lệ."
            );
        }

        // =================================================
        // TÌM MONSTER THEO ID
        // =================================================

        Monster monster;

        try {

            monster =
                    entityManager
                            .createQuery(
                                    """
                                    SELECT m
                                    FROM Monster m
                                    WHERE m.id = :id
                                    """,
                                    Monster.class
                            )
                            .setParameter(
                                    "id",
                                    starterId
                            )
                            .getSingleResult();

        } catch (NoResultException e) {

            throw new RuntimeException(
                    "Không tìm thấy Monster ID "
                    + starterId
                    + " trong database."
            );
        }

        // =================================================
        // TẠO PLAYER MONSTER
        // =================================================

        PlayerMonster playerMonster =
                new PlayerMonster();

        playerMonster.setUser(user);

        playerMonster.setMonster(monster);

        playerMonster.setLevel(1);

        playerMonster.setExp(0);

        playerMonster.setCurrentHp(
                monster.getBaseHp()
        );

        playerMonster =
                playerMonsterRepository.save(
                        playerMonster
                );

        // =================================================
        // THÊM MONSTER VÀO TEAM
        // =================================================

        teamService.addToTeam(
                username,
                playerMonster.getId()
        );

        // =================================================
        // HOÀN TẤT
        // =================================================

        return "redirect:/dashboard";
    }
}