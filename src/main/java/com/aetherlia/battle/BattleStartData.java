package com.aetherlia.battle;

public class BattleStartData {

    private Long playerMonsterId;
    private Long wildEncounterId;

    public BattleStartData(
            Long playerMonsterId,
            Long wildEncounterId) {

        this.playerMonsterId = playerMonsterId;
        this.wildEncounterId = wildEncounterId;
    }

    public Long getPlayerMonsterId() {
        return playerMonsterId;
    }

    public Long getWildEncounterId() {
        return wildEncounterId;
    }
}