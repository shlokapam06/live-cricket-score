package com.cricketscore.cricket_score.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cricketscore.cricket_score.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
	boolean existsByShortNameIgnoreCase(String shortName);
	boolean existsByNameIgnoreCase(String name);
	boolean existsByShortNameIgnoreCaseAndIdNot(String shortName, Long id);
	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}