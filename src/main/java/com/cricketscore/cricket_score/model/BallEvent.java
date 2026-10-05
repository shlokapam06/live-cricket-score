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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ball_events", uniqueConstraints = @UniqueConstraint(columnNames = { "match_id", "sequence_number" }))
@Getter
@Setter
@NoArgsConstructor
public class BallEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id", nullable = false)
	private CricketMatch match;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "batter_id", nullable = false)
	private Player batter;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "bowler_id", nullable = false)
	private Player bowler;

	@Enumerated(EnumType.STRING)
	private BallType type;

	private int sequenceNumber;
	private int overNumber;
	private int ballNumber;
	private int batterRuns;
	private int extraRuns;
	private int totalRuns;
	private boolean legalDelivery;
	private boolean wicket;
	private LocalDateTime createdAt = LocalDateTime.now();

	public BallEvent(CricketMatch match, Player batter, Player bowler, BallType type, int sequenceNumber,
			int overNumber, int ballNumber, int batterRuns, int extraRuns, int totalRuns,
			boolean legalDelivery, boolean wicket) {
		this.match = match;
		this.batter = batter;
		this.bowler = bowler;
		this.type = type;
		this.sequenceNumber = sequenceNumber;
		this.overNumber = overNumber;
		this.ballNumber = ballNumber;
		this.batterRuns = batterRuns;
		this.extraRuns = extraRuns;
		this.totalRuns = totalRuns;
		this.legalDelivery = legalDelivery;
		this.wicket = wicket;
		this.createdAt = LocalDateTime.now();
	}
}