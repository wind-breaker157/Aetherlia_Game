package com.aetherlia.service;

import com.aetherlia.entity.PlayerMonster;
import com.aetherlia.entity.WildEncounter;

import com.aetherlia.repository.PlayerMonsterRepository;

import org.springframework.stereotype.Service;

@Service
public class ProgressionService {

    private final PlayerMonsterRepository playerMonsterRepository;

    public ProgressionService(
            PlayerMonsterRepository playerMonsterRepository) {

        this.playerMonsterRepository =
                playerMonsterRepository;
    }

    public String grantWildExp(
            PlayerMonster playerMonster,
            WildEncounter encounter) {

        int myLevel =
                playerMonster.getLevel();

        int enemyLevel =
                encounter.getLevel();

        int baseExp =
                encounter
                        .getMonster()
                        .getBaseExp();

        /*
         * levelGapMod =
         * clamp(
         *   1 + (enemyLv - myLv) * 0.1,
         *   0.3,
         *   1.8
         * )
         */
        double levelGapMod =
                1.0
                +
                (
                    enemyLevel - myLevel
                )
                * 0.1;

        levelGapMod =
                Math.max(
                        0.3,
                        Math.min(
                                1.8,
                                levelGapMod
                        )
                );

        /*
         * Wild EXP:
         *
         * (baseExp * enemyLv / 5)
         * * levelGapMod
         */
        int gainedExp =
                (int) Math.round(
                        (
                            baseExp
                            * enemyLevel
                            / 5.0
                        )
                        * levelGapMod
                );

        playerMonster.setExp(
                playerMonster.getExp()
                + gainedExp
        );

        int oldLevel =
                playerMonster.getLevel();

        int levelUps = 0;

        /*
         * Kiểm tra lên level
         */
        while (
                playerMonster.getLevel() < 100
                &&
                playerMonster.getExp()
                    >=
                    expToNext(
                        playerMonster.getLevel() + 1
                    )
        ) {

            playerMonster.setLevel(
                    playerMonster.getLevel() + 1
            );

            levelUps++;
        }

        playerMonsterRepository.save(
                playerMonster
        );

        if (levelUps == 0) {

            return "EXP +"
                    + gainedExp
                    + ".";
        }

        return "EXP +"
                + gainedExp
                + ". "
                + "Monster lên từ Lv."
                + oldLevel
                + " → Lv."
                + playerMonster.getLevel()
                + "!";
    }

    private int expToNext(int level) {

        if (level <= 1) {
            return 0;
        }

        double exp =
                1.2
                * level
                * level
                * level

                - 15
                * level
                * level

                + 100
                * level

                - 140;

        return Math.max(
                0,
                (int) Math.ceil(exp)
        );
    }
}