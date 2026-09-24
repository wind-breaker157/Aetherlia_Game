package com.aetherlia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "player_characters")
public class PlayerCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true
    )
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String spriteCode;

    @Column(nullable = false)
    private String facing;

    public PlayerCharacter() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpriteCode() {
        return spriteCode;
    }

    public void setSpriteCode(String spriteCode) {
        this.spriteCode = spriteCode;
    }

    public String getFacing() {
        return facing;
    }

    public void setFacing(String facing) {
        this.facing = facing;
    }
}