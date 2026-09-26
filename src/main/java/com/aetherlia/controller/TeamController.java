package com.aetherlia.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.TeamMember;
import com.aetherlia.service.TeamService;

@Controller
public class TeamController {

    private final TeamService teamService;

    public TeamController(
            TeamService teamService) {

        this.teamService = teamService;
    }

    // ==========================================
    // XEM TEAM
    // ==========================================

    @GetMapping("/team")
    public String team(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();

        List<TeamMember> members =
                teamService.getTeamMembers(username);

        List<PlayerMonster> playerMonsters =
                teamService.getMyMonsters(username);

        if (members == null) {
            members = List.of();
        }

        if (playerMonsters == null) {
            playerMonsters = List.of();
        }

        /*
         * ID những Monster đang nằm trong Team.
         */
        Set<Long> teamMonsterIds =
                new HashSet<>();

        for (TeamMember member : members) {

            if (member.getPlayerMonster() != null
                    && member.getPlayerMonster().getId() != null) {

                teamMonsterIds.add(
                        member.getPlayerMonster().getId()
                );
            }
        }

        model.addAttribute(
                "members",
                members
        );

        model.addAttribute(
                "playerMonsters",
                playerMonsters
        );

        model.addAttribute(
                "teamMonsterIds",
                teamMonsterIds
        );

        return "team";
    }

    // ==========================================
    // THÊM MONSTER
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
    // XÓA MONSTER
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

    // ==========================================
    // ĐỔI VỊ TRÍ
    // ==========================================

    @PostMapping("/team/move")
    public String moveMonster(
            @RequestParam Long playerMonsterId,
            @RequestParam String direction,
            Authentication authentication) {

        String username =
                authentication.getName();

        teamService.moveMonster(
                username,
                playerMonsterId,
                direction
        );

        return "redirect:/team";
    }
}