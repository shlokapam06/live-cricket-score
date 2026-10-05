package com.cricketscore.cricket_score.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketscore.cricket_score.model.BallEvent;

public interface BallEventRepository extends JpaRepository<BallEvent, Long> {
	List<BallEvent> findByMatchIdOrderBySequenceNumberAsc(Long matchId);
}