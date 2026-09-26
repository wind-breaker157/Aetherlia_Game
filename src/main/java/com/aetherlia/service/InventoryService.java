package com.aetherlia.service;

import com.aetherlia.entity.InventoryItem;
import com.aetherlia.entity.User;

import com.aetherlia.repository.InventoryItemRepository;
import com.aetherlia.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final UserRepository userRepository;

    public InventoryService(
            InventoryItemRepository inventoryItemRepository,
            UserRepository userRepository) {

        this.inventoryItemRepository = inventoryItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addItem(
            String username,
            String itemCode,
            int quantity) {

        if (quantity <= 0) {
            return;
        }

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        InventoryItem item =
                inventoryItemRepository
                        .findByUserAndItemCode(
                                user,
                                itemCode
                        )
                        .orElse(null);

        if (item == null) {

            item = new InventoryItem();

            item.setUser(user);
            item.setItemCode(itemCode);
            item.setQuantity(quantity);

        } else {

            item.setQuantity(
                    item.getQuantity() + quantity
            );
        }

        inventoryItemRepository.save(item);
    }

    @Transactional
    public boolean consumeItem(
            String username,
            String itemCode,
            int quantity) {

        if (quantity <= 0) {
            return true;
        }

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        InventoryItem item =
                inventoryItemRepository
                        .findByUserAndItemCode(
                                user,
                                itemCode
                        )
                        .orElse(null);

        if (item == null) {
            return false;
        }

        if (item.getQuantity() < quantity) {
            return false;
        }

        item.setQuantity(
                item.getQuantity() - quantity
        );

        inventoryItemRepository.save(item);

        return true;
    }

    public int getQuantity(
            String username,
            String itemCode) {

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        return inventoryItemRepository
                .findByUserAndItemCode(
                        user,
                        itemCode
                )
                .map(InventoryItem::getQuantity)
                .orElse(0);
    }

    public List<InventoryItem> getMyItems(
            String username) {

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        return inventoryItemRepository
                .findByUserOrderByItemCodeAsc(user);
    }
}