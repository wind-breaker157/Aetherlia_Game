package com.aetherlia.controller;

import com.aetherlia.entity.InventoryItem;
import com.aetherlia.service.InventoryService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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

        List<InventoryItem> items =
                inventoryService.getMyItems(username);

        model.addAttribute(
                "items",
                items
        );

        return "inventory";
    }
}