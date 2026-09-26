package com.aetherlia.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "map_areas")
public class MapArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int mapNumber;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String terrain;

    @Column(nullable = false, length = 500)
    private String unlockCondition;

    @Column(length = 255)
    private String leaderBoss;

    @Column(nullable = false)
    private boolean unlockedByDefault;

    public MapArea() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getMapNumber() {
        return mapNumber;
    }

    public void setMapNumber(int mapNumber) {
        this.mapNumber = mapNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerrain() {
        return terrain;
    }

    public void setTerrain(String terrain) {
        this.terrain = terrain;
    }

    public String getUnlockCondition() {
        return unlockCondition;
    }

    public void setUnlockCondition(String unlockCondition) {
        this.unlockCondition = unlockCondition;
    }

    public String getLeaderBoss() {
        return leaderBoss;
    }

    public void setLeaderBoss(String leaderBoss) {
        this.leaderBoss = leaderBoss;
    }

    public boolean isUnlockedByDefault() {
        return unlockedByDefault;
    }

    public void setUnlockedByDefault(boolean unlockedByDefault) {
        this.unlockedByDefault = unlockedByDefault;
    }
}