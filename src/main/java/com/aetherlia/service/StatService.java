package com.aetherlia.service;

import com.aetherlia.entity.Monster;
import com.aetherlia.entity.PlayerMonster;

import org.springframework.stereotype.Service;

@Service
public class StatService {

    /*
     * HP tăng theo cấp số nhân:
     *
     * Max HP = Base HP × 1.03^(Level - 1)
     *
     * 3% mỗi level.
     */
    public int calculateMaxHp(int baseHp, int level) {

        if (level < 1) {
            level = 1;
        }

        double maxHp =
                baseHp
                * Math.pow(1.03, level - 1);

        return Math.max(
                1,
                (int) Math.round(maxHp)
        );
    }

    public int calculatePlayerMaxHp(
            PlayerMonster playerMonster) {

        return calculateMaxHp(
                playerMonster.getMonster().getBaseHp(),
                playerMonster.getLevel()
        );
    }

    public int calculateWildMaxHp(
            Monster monster,
            int level) {

        return calculateMaxHp(
                monster.getBaseHp(),
                level
        );
    }
}