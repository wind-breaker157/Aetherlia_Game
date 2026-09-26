package com.aetherlia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.aetherlia.entity.TopUpTransaction;
import com.aetherlia.entity.User;

import jakarta.persistence.LockModeType;

public interface TopUpTransactionRepository
        extends JpaRepository<TopUpTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TopUpTransaction t
        WHERE t.paymentCode = :paymentCode
    """)
    Optional<TopUpTransaction> findByPaymentCodeForUpdate(
            @Param("paymentCode") String paymentCode
    );

    Optional<TopUpTransaction> findByPaymentCodeAndUser(
            String paymentCode,
            User user
    );

    Optional<TopUpTransaction> findByGatewayTransactionId(
            Long gatewayTransactionId
    );
}