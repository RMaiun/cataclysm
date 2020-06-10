package com.mairo.cataclysm.repository;

import com.mairo.cataclysm.domain.Player;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends R2dbcRepository<Player, Long> {
}
