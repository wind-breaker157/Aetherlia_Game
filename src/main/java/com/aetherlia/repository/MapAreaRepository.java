package com.aetherlia.repository;

import com.aetherlia.entity.MapArea;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MapAreaRepository
        extends JpaRepository<MapArea, Long> {

    List<MapArea> findAllByOrderByMapNumberAsc();

    Optional<MapArea> findByMapNumber(int mapNumber);
}