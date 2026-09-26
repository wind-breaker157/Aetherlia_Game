package com.aetherlia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.aetherlia.entity.User;
import com.aetherlia.entity.Wallet;

import jakarta.persistence.LockModeType;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT w
        FROM Wallet w
        WHERE w.user.id = :userId
    """)
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") Long userId);
}