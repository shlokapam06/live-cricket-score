package com.cricketscore.cricket_score.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketscore.cricket_score.model.CricketMatch;

public interface MatchRepository extends JpaRepository<CricketMatch, Long> {
	List<CricketMatch> findAllByOrderByCreatedAtDesc();
	List<CricketMatch> findByTournamentIdOrderByCreatedAtAsc(Long tournamentId);
}