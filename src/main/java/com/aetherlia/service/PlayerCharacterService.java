package com.aetherlia.service;

import com.aetherlia.entity.PlayerCharacter;
import com.aetherlia.entity.User;

import com.aetherlia.repository.PlayerCharacterRepository;
import com.aetherlia.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class PlayerCharacterService {

    private final PlayerCharacterRepository playerCharacterRepository;
    private final UserRepository userRepository;

    public PlayerCharacterService(
            PlayerCharacterRepository playerCharacterRepository,
            UserRepository userRepository) {

        this.playerCharacterRepository =
                playerCharacterRepository;

        this.userRepository =
                userRepository;
    }

    public PlayerCharacter getOrCreateCharacter(
            String username) {

        User user =
                userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(
                    "Không tìm thấy người chơi."
            );
        }

        PlayerCharacter character =
                playerCharacterRepository.findByUser(user);

        if (character == null) {

            character =
                    new PlayerCharacter();

            character.setUser(user);

            character.setName(
                    user.getUsername()
            );

            character.setSpriteCode(
                    "player_default"
            );

            character.setFacing(
                    "DOWN"
            );

            character =
                    playerCharacterRepository.save(
                            character
                    );
        }

        return character;
    }

    public void updateFacing(
            String username,
            String direction) {

        PlayerCharacter character =
                getOrCreateCharacter(username);

        character.setFacing(direction);

        playerCharacterRepository.save(
                character
        );
    }
}