package com.aetherlia.battle;

public class BattleSkill {

    private String id;
    private String name;
    private String type;
    private int power;
    private int accuracy;

    public BattleSkill(
            String id,
            String name,
            String type,
            int power,
            int accuracy) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }
}