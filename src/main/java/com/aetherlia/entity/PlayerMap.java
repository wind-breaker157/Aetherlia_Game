package com.aetherlia.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "player_maps",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"user_id", "map_area_id"}
        )
    }
)
public class PlayerMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "map_area_id", nullable = false)
    private MapArea mapArea;

    @Column(nullable = false)
    private boolean unlocked;

    public PlayerMap() {
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

    public MapArea getMapArea() {
        return mapArea;
    }

    public void setMapArea(MapArea mapArea) {
        this.mapArea = mapArea;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}