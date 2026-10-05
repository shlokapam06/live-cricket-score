package com.cricketscore.cricket_score.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.cricketscore.cricket_score.model.BallType;

public final class Requests {
	private Requests() {
	}

	public record TeamRequest(@NotBlank String name, @NotBlank String shortName) {
	}

	public record PlayerRequest(@NotBlank String name, @NotBlank String role, @NotNull Long teamId) {
	}

	public record MatchRequest(@NotNull Long battingTeamId, @NotNull Long bowlingTeamId,
			@NotBlank String venue, @Min(1) @Max(50) int maxOvers, Long tournamentId) {
		public MatchRequest(Long battingTeamId, Long bowlingTeamId, String venue, int maxOvers) {
			this(battingTeamId, bowlingTeamId, venue, maxOvers, null);
		}
	}

	public record TournamentRequest(@NotBlank String name, @NotBlank String format) {
	}

	public record CompleteMatchRequest(Long winnerTeamId) {
	}

	public record BallEventRequest(@NotNull BallType type, @NotNull Long batterId, @NotNull Long bowlerId,
			@Min(0) @Max(6) int runs, @Min(0) @Max(6) int extraRuns) {
	}
}