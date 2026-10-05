package com.cricketscore.cricket_score.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cricket_matches")
@Getter
@Setter
@NoArgsConstructor
public class CricketMatch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "batting_team_id", nullable = false)
	private Team battingTeam;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bowling_team_id", nullable = false)
	private Team bowlingTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tournament_id")
	private Tournament tournament;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winner_team_id")
	private Team winnerTeam;

	@NotBlank
	private String venue;

	@Min(1)
	@Max(50)
	private int maxOvers;

	@Enumerated(EnumType.STRING)
	private MatchStatus status = MatchStatus.LIVE;

	private LocalDateTime createdAt = LocalDateTime.now();

	public CricketMatch(Team battingTeam, Team bowlingTeam, String venue, int maxOvers) {
		this.battingTeam = battingTeam;
		this.bowlingTeam = bowlingTeam;
		this.venue = venue;
		this.maxOvers = maxOvers;
		this.status = MatchStatus.LIVE;
		this.createdAt = LocalDateTime.now();
	}

	public CricketMatch(Team battingTeam, Team bowlingTeam, String venue, int maxOvers, Tournament tournament) {
		this(battingTeam, bowlingTeam, venue, maxOvers);
		this.tournament = tournament;
	}
}