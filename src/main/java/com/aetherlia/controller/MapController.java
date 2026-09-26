package com.aetherlia.controller;

import com.aetherlia.entity.MapArea;
import com.aetherlia.entity.PlayerCharacter;
import com.aetherlia.entity.PlayerMap;
import com.aetherlia.entity.PlayerPosition;

import com.aetherlia.service.MapService;
import com.aetherlia.service.PlayerCharacterService;
import com.aetherlia.service.PlayerPositionService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class MapController {

    private final MapService mapService;

    private final PlayerPositionService playerPositionService;

    private final PlayerCharacterService playerCharacterService;


    public MapController(
            MapService mapService,
            PlayerPositionService playerPositionService,
            PlayerCharacterService playerCharacterService) {

        this.mapService = mapService;

        this.playerPositionService =
                playerPositionService;

        this.playerCharacterService =
                playerCharacterService;
    }


    // =========================================================
    // DANH SÁCH TẤT CẢ MAP
    // =========================================================

    @GetMapping("/map")
    public String map(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        List<PlayerMap> playerMaps =
                mapService.getPlayerMaps(username);


        model.addAttribute(
                "playerMaps",
                playerMaps
        );


        return "map";
    }


    // =========================================================
    // CHI TIẾT MAP
    // =========================================================

    @GetMapping("/map/{mapNumber}")
    public String mapDetail(
            @PathVariable int mapNumber,
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        // -----------------------------------------------------
        // Lấy danh sách map của người chơi
        // -----------------------------------------------------

        List<PlayerMap> playerMaps =
                mapService.getPlayerMaps(username);


        // -----------------------------------------------------
        // Tìm map được chọn
        // -----------------------------------------------------

        PlayerMap selectedPlayerMap = null;


        for (PlayerMap playerMap : playerMaps) {

            if (
                    playerMap.getMapArea() != null
                    &&
                    playerMap.getMapArea().getMapNumber()
                            == mapNumber
            ) {

                selectedPlayerMap =
                        playerMap;

                break;
            }
        }


        // -----------------------------------------------------
        // Không tìm thấy map
        // -----------------------------------------------------

        if (selectedPlayerMap == null) {

            return "redirect:/map";
        }


        // -----------------------------------------------------
        // Map chưa mở khóa
        // -----------------------------------------------------

        if (!selectedPlayerMap.isUnlocked()) {

            return "redirect:/map";
        }


        // -----------------------------------------------------
        // Map đã mở khóa
        // -----------------------------------------------------

        MapArea mapArea =
                selectedPlayerMap.getMapArea();


        // -----------------------------------------------------
        // Lấy vị trí nhân vật
        // -----------------------------------------------------

        PlayerPosition position =
                playerPositionService
                        .getOrCreatePosition(username);


        // -----------------------------------------------------
        // Lấy thông tin nhân vật
        // -----------------------------------------------------

        PlayerCharacter character =
                playerCharacterService
                        .getOrCreateCharacter(username);


        // -----------------------------------------------------
        // Gửi dữ liệu sang Thymeleaf
        // -----------------------------------------------------

        model.addAttribute(
                "mapArea",
                mapArea
        );


        model.addAttribute(
                "position",
                position
        );


        model.addAttribute(
                "character",
                character
        );


        return "map-detail";
    }
}