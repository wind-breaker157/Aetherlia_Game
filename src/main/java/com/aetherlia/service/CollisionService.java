package com.aetherlia.service;

import com.aetherlia.entity.MapArea;
import org.springframework.stereotype.Service;

@Service
public class CollisionService {

    /*
     * Kích thước hitbox dưới chân nhân vật.
     *
     * Nhân vật SVG rộng 64px và cao 100px,
     * nhưng khi va chạm ta không dùng toàn bộ thân người.
     *
     * Ta chỉ kiểm tra vùng dưới chân.
     */
    private static final int PLAYER_HALF_WIDTH = 9;
    private static final int PLAYER_TOP_OFFSET = 10;
    private static final int PLAYER_BOTTOM_OFFSET = 2;

    /*
     * Kích thước vùng game.
     */
    private static final int WORLD_MIN_X = 32;
    private static final int WORLD_MAX_X = 868;

    private static final int WORLD_MIN_Y = 42;
    private static final int WORLD_MAX_Y = 468;


    /*
     * Kiểm tra vị trí mới có bị chặn hay không.
     */
    public boolean isBlocked(
            MapArea mapArea,
            int x,
            int y) {

        /*
         * Ngoài bản đồ.
         */
        if (x < WORLD_MIN_X || x > WORLD_MAX_X) {
            return true;
        }

        if (y < WORLD_MIN_Y || y > WORLD_MAX_Y) {
            return true;
        }


        /*
         * Hiện tại Map 1 là Làng Lục Diệp.
         */
        if (mapArea == null) {
            return false;
        }

        if (mapArea.getMapNumber() != 1) {
            return false;
        }


        /*
         * Hitbox dưới chân nhân vật.
         */
        int playerLeft =
                x - PLAYER_HALF_WIDTH;

        int playerRight =
                x + PLAYER_HALF_WIDTH;

        int playerTop =
                y - PLAYER_TOP_OFFSET;

        int playerBottom =
                y + PLAYER_BOTTOM_OFFSET;


        /*
         * Các vật cản của Map 1.
         *
         * Format:
         * {left, top, right, bottom}
         */
        int[][] obstacles = {

            /*
             * Tree 1
             */
            {78, 100, 106, 132},

            /*
             * Tree 2
             */
            {688, 95, 716, 128},

            /*
             * Tree 3
             */
            {723, 370, 751, 403},

            /*
             * House
             *
             * Chừa một khoảng ở giữa phía dưới
             * để sau này có thể làm cửa.
             */
            {355, 95, 415, 190},
            {485, 95, 545, 190},
            {415, 95, 485, 125}
        };


        /*
         * Kiểm tra va chạm.
         */
        for (int[] obstacle : obstacles) {

            int obstacleLeft = obstacle[0];
            int obstacleTop = obstacle[1];

            int obstacleRight = obstacle[2];
            int obstacleBottom = obstacle[3];


            boolean overlapX =
                    playerRight > obstacleLeft
                    &&
                    playerLeft < obstacleRight;


            boolean overlapY =
                    playerBottom > obstacleTop
                    &&
                    playerTop < obstacleBottom;


            if (overlapX && overlapY) {
                return true;
            }
        }


        return false;
    }
}