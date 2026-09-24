package com.aetherlia.repository;

import com.aetherlia.entity.InventoryItem;
import com.aetherlia.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository
        extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByUserAndItemCode(
            User user,
            String itemCode
    );

    List<InventoryItem> findByUserOrderByItemCodeAsc(
            User user
    );
}