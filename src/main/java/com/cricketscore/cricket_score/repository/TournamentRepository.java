package com.cricketscore.cricket_score.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketscore.cricket_score.model.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
	boolean existsByNameIgnoreCase(String name);
}