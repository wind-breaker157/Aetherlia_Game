package com.aetherlia.game;

public class EncounterCheckResult {

    private boolean encounter;

    private Long encounterId;

    private String monsterName;

    private int level;


    public EncounterCheckResult() {
    }


    public EncounterCheckResult(
            boolean encounter,
            Long encounterId,
            String monsterName,
            int level) {

        this.encounter = encounter;
        this.encounterId = encounterId;
        this.monsterName = monsterName;
        this.level = level;
    }


    public boolean isEncounter() {
        return encounter;
    }

    public void setEncounter(boolean encounter) {
        this.encounter = encounter;
    }


    public Long getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(Long encounterId) {
        this.encounterId = encounterId;
    }


    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}