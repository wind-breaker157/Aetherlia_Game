package com.aetherlia.battle;

public class BattleResult {

    private String message;
    private boolean victory;
    private boolean defeat;

    public BattleResult(
            String message,
            boolean victory,
            boolean defeat) {

        this.message = message;
        this.victory = victory;
        this.defeat = defeat;
    }

    public String getMessage() {
        return message;
    }

    public boolean isVictory() {
        return victory;
    }

    public boolean isDefeat() {
        return defeat;
    }
}