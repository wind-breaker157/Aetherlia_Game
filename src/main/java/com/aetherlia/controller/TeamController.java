package com.aetherlia.controller;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.service.TeamService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }


    // ==========================================
    // HIỂN THỊ TEAM
    // ==========================================

    @GetMapping("/team")
    public String team(
            Authentication authentication,
            Model model) {

        // Lấy username của người đang đăng nhập
        String username = authentication.getName();


        // Lấy Monster trong Team
        List<TeamMember> teamMembers =
                teamService.getTeamMembers(username);


        // Lấy tất cả Monster người chơi sở hữu
        List<PlayerMonster> myMonsters =
                teamService.getMyMonsters(username);


        // ==========================================
        // Lưu ID các PlayerMonster đang ở Team
        // ==========================================

        Set<Long> teamPlayerMonsterIds =
                new HashSet<>();

        for (TeamMember member : teamMembers) {

            if (member.getPlayerMonster() != null) {

                teamPlayerMonsterIds.add(
                        member.getPlayerMonster().getId()
                );
            }
        }


        // ==========================================
        // Gửi dữ liệu sang team.html
        // ==========================================

        model.addAttribute(
                "teamMembers",
                teamMembers
        );

        model.addAttribute(
                "myMonsters",
                myMonsters
        );

        model.addAttribute(
                "teamPlayerMonsterIds",
                teamPlayerMonsterIds
        );


        return "team";
    }


    // ==========================================
    // THÊM MONSTER VÀO TEAM
    // ==========================================

    @PostMapping("/team/add")
    public String addToTeam(
            @RequestParam Long playerMonsterId,
            Authentication authentication) {

        String username =
                authentication.getName();

        teamService.addToTeam(
                username,
                playerMonsterId
        );

        return "redirect:/team";
    }


    // ==========================================
    // XÓA MONSTER KHỎI TEAM
    // ==========================================

    @PostMapping("/team/remove")
    public String removeFromTeam(
            @RequestParam Long playerMonsterId,
            Authentication authentication) {

        String username =
                authentication.getName();

        teamService.removeFromTeam(
                username,
                playerMonsterId
        );

        return "redirect:/team";
    }
}