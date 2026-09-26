package com.aetherlia.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "top_up_transactions",
    indexes = {
        @Index(name = "idx_topup_payment_code", columnList = "payment_code"),
        @Index(name = "idx_topup_gateway_id", columnList = "gateway_transaction_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "payment_code"),
        @UniqueConstraint(columnNames = "gateway_transaction_id")
    }
)
public class TopUpTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "payment_code", nullable = false, unique = true, length = 30)
    private String paymentCode;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TopUpStatus status = TopUpStatus.PENDING;

    @Column(name = "gateway_transaction_id", unique = true)
    private Long gatewayTransactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public TopUpTransaction() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public TopUpStatus getStatus() {
        return status;
    }

    public void setStatus(TopUpStatus status) {
        this.status = status;
    }

    public Long getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public void setGatewayTransactionId(Long gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}