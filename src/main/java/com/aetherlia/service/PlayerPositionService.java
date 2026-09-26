package com.aetherlia.service;

import com.aetherlia.entity.MapArea;
import com.aetherlia.entity.PlayerPosition;
import com.aetherlia.entity.User;
import com.aetherlia.game.MovementResult;
import com.aetherlia.repository.MapAreaRepository;
import com.aetherlia.repository.PlayerPositionRepository;
import com.aetherlia.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;


@Service
public class PlayerPositionService {

    private static final int STEP = 16;

    /*
     * Vùng cỏ cao của Map 1.
     *
     * Tọa độ:
     *
     * X: 60 -> 330
     * Y: 270 -> 460
     */
    private static final int GRASS_MIN_X = 60;
    private static final int GRASS_MAX_X = 330;

    private static final int GRASS_MIN_Y = 270;
    private static final int GRASS_MAX_Y = 460;


    private final PlayerPositionRepository playerPositionRepository;

    private final UserRepository userRepository;

    private final MapAreaRepository mapAreaRepository;

    private final CollisionService collisionService;


    public PlayerPositionService(
            PlayerPositionRepository playerPositionRepository,
            UserRepository userRepository,
            MapAreaRepository mapAreaRepository,
            CollisionService collisionService) {

        this.playerPositionRepository =
                playerPositionRepository;

        this.userRepository =
                userRepository;

        this.mapAreaRepository =
                mapAreaRepository;

        this.collisionService =
                collisionService;
    }


    /* =========================================================
       GET OR CREATE POSITION
    ========================================================= */

    @Transactional
    public PlayerPosition getOrCreatePosition(
            String username) {

        User user =
                userRepository.findByUsername(username);


        if (user == null) {

            throw new RuntimeException(
                    "Không tìm thấy User: " + username
            );
        }


        PlayerPosition position =
                playerPositionRepository.findByUser(user);


        /*
         * Chưa có vị trí.
         */
        if (position == null) {

            MapArea map1 =
                    mapAreaRepository
                            .findByMapNumber(1)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Không tìm thấy Map 1"
                                    )
                            );


            position = new PlayerPosition();

            position.setUser(user);

            position.setMapArea(map1);

            position.setX(450);

            position.setY(250);

            position.setLocationCode("village");


            return playerPositionRepository.save(position);
        }


        /*
         * Nếu dữ liệu cũ là 0,0
         * thì đưa nhân vật về giữa làng.
         */
        if (position.getX() == 0 &&
            position.getY() == 0) {

            position.setX(450);

            position.setY(250);
        }


        /*
         * Nếu chưa có MapArea.
         */
        if (position.getMapArea() == null) {

            MapArea map1 =
                    mapAreaRepository
                            .findByMapNumber(1)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "Không tìm thấy Map 1"
                                    )
                            );

            position.setMapArea(map1);
        }


        /*
         * Tự xác định location hiện tại
         * dựa trên X/Y.
         */
        String locationCode =
                determineLocationCode(
                        position.getX(),
                        position.getY()
                );


        position.setLocationCode(
                locationCode
        );


        return playerPositionRepository.save(position);
    }


    /* =========================================================
       DETERMINE LOCATION
    ========================================================= */

    private String determineLocationCode(
            int x,
            int y) {

        /*
         * Nếu nằm trong vùng cỏ.
         */
        if (
                x >= GRASS_MIN_X &&
                x <= GRASS_MAX_X &&
                y >= GRASS_MIN_Y &&
                y <= GRASS_MAX_Y
        ) {

            return "route_grass";
        }


        /*
         * Các vị trí còn lại của Map 1
         * hiện được xem là làng.
         */
        return "village";
    }


    /* =========================================================
       MOVE CHARACTER
    ========================================================= */

    @Transactional
    public MovementResult moveCharacter(
            String username,
            String direction) {


        PlayerPosition position =
                getOrCreatePosition(username);


        int oldX = position.getX();

        int oldY = position.getY();


        int newX = oldX;

        int newY = oldY;


        /*
         * Nếu direction null.
         */
        if (direction == null) {

            return new MovementResult(
                    oldX,
                    oldY,
                    "DOWN",
                    false,
                    position.getLocationCode()
            );
        }


        /*
         * Tính vị trí mới.
         */
        switch (direction.toUpperCase()) {

            case "UP":

                newY -= STEP;

                break;


            case "DOWN":

                newY += STEP;

                break;


            case "LEFT":

                newX -= STEP;

                break;


            case "RIGHT":

                newX += STEP;

                break;


            default:

                return new MovementResult(
                        oldX,
                        oldY,
                        "DOWN",
                        false,
                        position.getLocationCode()
                );
        }


        /*
         * Kiểm tra collision.
         */
        boolean blocked =
                collisionService.isBlocked(
                        position.getMapArea(),
                        newX,
                        newY
                );


        /*
         * Nếu bị chặn.
         */
        if (blocked) {

            return new MovementResult(
                    oldX,
                    oldY,
                    direction.toUpperCase(),
                    true,
                    position.getLocationCode()
            );
        }


        /*
         * Cập nhật vị trí.
         */
        position.setX(newX);

        position.setY(newY);


        /*
         * Xác định khu vực mới.
         */
        String newLocationCode =
                determineLocationCode(
                        newX,
                        newY
                );


        position.setLocationCode(
                newLocationCode
        );


        /*
         * Lưu DB.
         */
        playerPositionRepository.save(
                position
        );


        /*
         * Trả kết quả về JavaScript.
         */
        return new MovementResult(
                newX,
                newY,
                direction.toUpperCase(),
                false,
                newLocationCode
        );
    }
}