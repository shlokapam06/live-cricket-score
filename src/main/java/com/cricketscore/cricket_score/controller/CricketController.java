package com.cricketscore.cricket_score.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cricketscore.cricket_score.dto.Requests.BallEventRequest;
import com.cricketscore.cricket_score.dto.Requests.CompleteMatchRequest;
import com.cricketscore.cricket_score.dto.Requests.MatchRequest;
import com.cricketscore.cricket_score.dto.Requests.PlayerRequest;
import com.cricketscore.cricket_score.dto.Requests.TeamRequest;
import com.cricketscore.cricket_score.dto.Requests.TournamentRequest;
import com.cricketscore.cricket_score.dto.ScoreDtos.BallView;
import com.cricketscore.cricket_score.dto.ScoreDtos.MatchView;
import com.cricketscore.cricket_score.dto.ScoreDtos.PlayerView;
import com.cricketscore.cricket_score.dto.ScoreDtos.ScoreResponse;
import com.cricketscore.cricket_score.dto.ScoreDtos.StandingsRow;
import com.cricketscore.cricket_score.dto.ScoreDtos.SummaryResponse;
import com.cricketscore.cricket_score.dto.ScoreDtos.TeamView;
import com.cricketscore.cricket_score.dto.ScoreDtos.TournamentView;
import com.cricketscore.cricket_score.service.CricketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CricketController {
	private final CricketService cricketService;

	@GetMapping("/teams")
	public List<TeamView> teams() {
		return cricketService.getTeams();
	}

	@PostMapping("/teams")
	@ResponseStatus(HttpStatus.CREATED)
	public TeamView createTeam(@Valid @RequestBody TeamRequest request) {
		return cricketService.createTeam(request);
	}

	@PutMapping("/teams/{teamId}")
	public TeamView updateTeam(@PathVariable Long teamId, @Valid @RequestBody TeamRequest request) {
		return cricketService.updateTeam(teamId, request);
	}

	@DeleteMapping("/teams/{teamId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTeam(@PathVariable Long teamId) {
		cricketService.deleteTeam(teamId);
	}

	@GetMapping("/players")
	public List<PlayerView> players(@RequestParam(required = false) Long teamId) {
		return cricketService.getPlayers(teamId);
	}

	@PostMapping("/players")
	@ResponseStatus(HttpStatus.CREATED)
	public PlayerView createPlayer(@Valid @RequestBody PlayerRequest request) {
		return cricketService.createPlayer(request);
	}

	@PutMapping("/players/{playerId}")
	public PlayerView updatePlayer(@PathVariable Long playerId, @Valid @RequestBody PlayerRequest request) {
		return cricketService.updatePlayer(playerId, request);
	}

	@DeleteMapping("/players/{playerId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePlayer(@PathVariable Long playerId) {
		cricketService.deletePlayer(playerId);
	}

	@GetMapping("/matches")
	public List<MatchView> matches() {
		return cricketService.getMatches();
	}

	@GetMapping("/tournaments")
	public List<TournamentView> tournaments() {
		return cricketService.getTournaments();
	}

	@PostMapping("/tournaments")
	@ResponseStatus(HttpStatus.CREATED)
	public TournamentView createTournament(@Valid @RequestBody TournamentRequest request) {
		return cricketService.createTournament(request);
	}

	@GetMapping("/tournaments/{tournamentId}/standings")
	public List<StandingsRow> standings(@PathVariable Long tournamentId) {
		return cricketService.getStandings(tournamentId);
	}

	@PostMapping("/matches")
	@ResponseStatus(HttpStatus.CREATED)
	public MatchView createMatch(@Valid @RequestBody MatchRequest request) {
		return cricketService.createMatch(request);
	}

	@GetMapping("/matches/{matchId}")
	public MatchView match(@PathVariable Long matchId) {
		return cricketService.getMatch(matchId);
	}

	@PatchMapping("/matches/{matchId}/complete")
	public MatchView completeMatch(@PathVariable Long matchId,
			@RequestBody(required = false) CompleteMatchRequest request) {
		return cricketService.completeMatch(matchId, request == null ? null : request.winnerTeamId());
	}

	@GetMapping("/matches/{matchId}/events")
	public List<BallView> events(@PathVariable Long matchId) {
		return cricketService.getEvents(matchId);
	}

	@PostMapping("/matches/{matchId}/events")
	@ResponseStatus(HttpStatus.CREATED)
	public BallView recordEvent(@PathVariable Long matchId, @Valid @RequestBody BallEventRequest request) {
		return cricketService.recordEvent(matchId, request);
	}

	@GetMapping("/matches/{matchId}/score")
	public ScoreResponse score(@PathVariable Long matchId) {
		return cricketService.getScore(matchId);
	}

	@GetMapping("/matches/{matchId}/summary")
	public SummaryResponse summary(@PathVariable Long matchId) {
		return cricketService.getSummary(matchId);
	}
}
