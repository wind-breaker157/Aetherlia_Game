package com.aetherlia.battle;

public class BattleResult {

    private String message;

    private boolean victory;

    private boolean defeat;

    private Long playerMonsterId;

    // =====================================================
    // THÔNG TIN PLAYER ATTACK
    // =====================================================

    private boolean playerCritical;

    private double playerTypeMultiplier = 1.0;

    // =====================================================
    // THÔNG TIN WILD ATTACK
    // =====================================================

    private boolean enemyCritical;

    private double enemyTypeMultiplier = 1.0;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public BattleResult(
            String message,
            boolean victory,
            boolean defeat) {

        this(
                message,
                victory,
                defeat,
                null
        );
    }

    public BattleResult(
            String message,
            boolean victory,
            boolean defeat,
            Long playerMonsterId) {

        this.message = message;
        this.victory = victory;
        this.defeat = defeat;
        this.playerMonsterId = playerMonsterId;
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isVictory() {
        return victory;
    }

    public void setVictory(boolean victory) {
        this.victory = victory;
    }

    public boolean isDefeat() {
        return defeat;
    }

    public void setDefeat(boolean defeat) {
        this.defeat = defeat;
    }

    public Long getPlayerMonsterId() {
        return playerMonsterId;
    }

    public void setPlayerMonsterId(Long playerMonsterId) {
        this.playerMonsterId = playerMonsterId;
    }

    // =====================================================
    // PLAYER CRITICAL
    // =====================================================

    public boolean isPlayerCritical() {
        return playerCritical;
    }

    public void setPlayerCritical(boolean playerCritical) {
        this.playerCritical = playerCritical;
    }

    // =====================================================
    // PLAYER TYPE MULTIPLIER
    // =====================================================

    public double getPlayerTypeMultiplier() {
        return playerTypeMultiplier;
    }

    public void setPlayerTypeMultiplier(double playerTypeMultiplier) {
        this.playerTypeMultiplier = playerTypeMultiplier;
    }

    // =====================================================
    // ENEMY CRITICAL
    // =====================================================

    public boolean isEnemyCritical() {
        return enemyCritical;
    }

    public void setEnemyCritical(boolean enemyCritical) {
        this.enemyCritical = enemyCritical;
    }

    // =====================================================
    // ENEMY TYPE MULTIPLIER
    // =====================================================

    public double getEnemyTypeMultiplier() {
        return enemyTypeMultiplier;
    }

    public void setEnemyTypeMultiplier(double enemyTypeMultiplier) {
        this.enemyTypeMultiplier = enemyTypeMultiplier;
    }
}