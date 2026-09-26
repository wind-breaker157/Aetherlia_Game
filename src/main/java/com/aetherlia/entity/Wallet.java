package com.aetherlia.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "wallets",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
    }
)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Long balance = 0L;

    public Wallet() {
    }

    public Wallet(User user) {
        this.user = user;
        this.balance = 0L;
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

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}