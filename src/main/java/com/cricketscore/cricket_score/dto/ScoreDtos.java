package com.cricketscore.cricket_score.dto;

import java.util.List;

import com.cricketscore.cricket_score.model.BallType;
import com.cricketscore.cricket_score.model.MatchStatus;

public final class ScoreDtos {
	private ScoreDtos() {
	}

	public record TeamView(Long id, String name, String shortName) {
	}

	public record PlayerView(Long id, String name, String role, TeamView team) {
	}

	public record MatchView(Long id, TeamView battingTeam, TeamView bowlingTeam, String venue,
			int maxOvers, MatchStatus status, TournamentView tournament, TeamView winnerTeam) {
	}

	public record TournamentView(Long id, String name, String format) {
	}

	public record StandingsRow(TeamView team, int played, int won, int lost, int drawn, int points) {
	}

	public record BattingFigure(Long playerId, String playerName, int runs, int balls,
			int fours, int sixes, boolean out) {
	}

	public record BowlingFigure(Long playerId, String playerName, String overs, int maidens,
			int runs, int wickets, int legalBalls) {
	}

	public record BallView(int overNumber, int ballNumber, BallType type, String label,
			int runs, boolean wicket, String batterName, String bowlerName) {
	}

	public record ScoreResponse(Long matchId, TeamView battingTeam, TeamView bowlingTeam,
			String venue, int maxOvers, MatchStatus status, int runs, int wickets,
			int legalBalls, String overs, double runRate, List<BattingFigure> currentBatsmen,
			BowlingFigure currentBowler, List<BallView> recentBalls) {
	}

	public record SummaryResponse(ScoreResponse score, List<BattingFigure> battingFigures,
			List<BowlingFigure> bowlingFigures, List<BallView> innings) {
	}
}