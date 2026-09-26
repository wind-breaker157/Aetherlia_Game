package com.aetherlia.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.aetherlia.entity.InventoryItem;
import com.aetherlia.service.InventoryService;

@Controller
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService) {

        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory")
    public String inventory(
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        /*
         * Lấy toàn bộ vật phẩm của người chơi.
         */
        List<InventoryItem> items =
                inventoryService.getMyItems(username);

        /*
         * Tránh trường hợp Service trả về null.
         */
        if (items == null) {
            items = Collections.emptyList();
        }

        model.addAttribute(
                "items",
                items
        );

        /*
         * Lấy riêng số lượng các Orb thường dùng.
         * Dữ liệu này sẽ dùng tiếp cho Battle/Capture.
         */
        model.addAttribute(
                "basicOrb",
                inventoryService.getQuantity(
                        username,
                        "BASIC_ORB"
                )
        );

        model.addAttribute(
                "greatOrb",
                inventoryService.getQuantity(
                        username,
                        "GREAT_ORB"
                )
        );

        model.addAttribute(
                "ultraOrb",
                inventoryService.getQuantity(
                        username,
                        "ULTRA_ORB"
                )
        );

        model.addAttribute(
                "masterOrb",
                inventoryService.getQuantity(
                        username,
                        "MASTER_ORB"
                )
        );

        model.addAttribute(
                "potion",
                inventoryService.getQuantity(
                        username,
                        "POTION"
                )
        );

        model.addAttribute(
                "superPotion",
                inventoryService.getQuantity(
                        username,
                        "SUPER_POTION"
                )
        );

        model.addAttribute(
                "revive",
                inventoryService.getQuantity(
                        username,
                        "REVIVE"
                )
        );

        return "inventory";
    }
}