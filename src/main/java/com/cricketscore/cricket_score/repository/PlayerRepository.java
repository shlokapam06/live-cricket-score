package com.cricketscore.cricket_score.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketscore.cricket_score.model.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
	List<Player> findByTeamIdOrderByNameAsc(Long teamId);
	boolean existsByNameIgnoreCaseAndTeamId(String name, Long teamId);
	boolean existsByNameIgnoreCaseAndTeamIdAndIdNot(String name, Long teamId, Long id);
}