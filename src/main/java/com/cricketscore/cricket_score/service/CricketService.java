package com.cricketscore.cricket_score.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cricketscore.cricket_score.dto.Requests.BallEventRequest;
import com.cricketscore.cricket_score.dto.Requests.MatchRequest;
import com.cricketscore.cricket_score.dto.Requests.PlayerRequest;
import com.cricketscore.cricket_score.dto.Requests.TeamRequest;
import com.cricketscore.cricket_score.dto.Requests.TournamentRequest;
import com.cricketscore.cricket_score.dto.ScoreDtos.BallView;
import com.cricketscore.cricket_score.dto.ScoreDtos.BattingFigure;
import com.cricketscore.cricket_score.dto.ScoreDtos.BowlingFigure;
import com.cricketscore.cricket_score.dto.ScoreDtos.MatchView;
import com.cricketscore.cricket_score.dto.ScoreDtos.PlayerView;
import com.cricketscore.cricket_score.dto.ScoreDtos.ScoreResponse;
import com.cricketscore.cricket_score.dto.ScoreDtos.StandingsRow;
import com.cricketscore.cricket_score.dto.ScoreDtos.SummaryResponse;
import com.cricketscore.cricket_score.dto.ScoreDtos.TeamView;
import com.cricketscore.cricket_score.dto.ScoreDtos.TournamentView;
import com.cricketscore.cricket_score.exception.ResourceNotFoundException;
import com.cricketscore.cricket_score.model.BallEvent;
import com.cricketscore.cricket_score.model.BallType;
import com.cricketscore.cricket_score.model.CricketMatch;
import com.cricketscore.cricket_score.model.MatchStatus;
import com.cricketscore.cricket_score.model.Player;
import com.cricketscore.cricket_score.model.Team;
import com.cricketscore.cricket_score.model.Tournament;
import com.cricketscore.cricket_score.repository.BallEventRepository;
import com.cricketscore.cricket_score.repository.MatchRepository;
import com.cricketscore.cricket_score.repository.PlayerRepository;
import com.cricketscore.cricket_score.repository.TeamRepository;
import com.cricketscore.cricket_score.repository.TournamentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CricketService {
	private final TeamRepository teamRepository;
	private final PlayerRepository playerRepository;
	private final MatchRepository matchRepository;
	private final BallEventRepository ballEventRepository;
	private final TournamentRepository tournamentRepository;

	public List<TeamView> getTeams() {
		return teamRepository.findAll().stream().map(this::teamView).toList();
	}

	public TeamView createTeam(TeamRequest request) {
		if (teamRepository.existsByNameIgnoreCase(request.name().trim())
				|| teamRepository.existsByShortNameIgnoreCase(request.shortName().trim())) {
			throw new IllegalArgumentException("Team name and short name must be unique");
		}
		return teamView(teamRepository.save(new Team(request.name().trim(), request.shortName().trim())));
	}

	public TeamView updateTeam(Long id, TeamRequest request) {
		Team team = getTeam(id);
		if (teamRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)
				|| teamRepository.existsByShortNameIgnoreCaseAndIdNot(request.shortName().trim(), id)) {
			throw new IllegalArgumentException("Team name and short name must be unique");
		}
		team.setName(request.name().trim());
		team.setShortName(request.shortName().trim());
		return teamView(teamRepository.save(team));
	}

	public void deleteTeam(Long id) {
		teamRepository.delete(getTeam(id));
	}

	public List<PlayerView> getPlayers(Long teamId) {
		List<Player> players;
		if (teamId == null) {
			players = playerRepository.findAll();
		} else {
			getTeam(teamId);
			players = playerRepository.findByTeamIdOrderByNameAsc(teamId);
		}
		return players.stream().map(this::playerView).toList();
	}

	public PlayerView createPlayer(PlayerRequest request) {
		Team team = getTeam(request.teamId());
		if (playerRepository.existsByNameIgnoreCaseAndTeamId(request.name().trim(), team.getId())) {
			throw new IllegalArgumentException("A player with this name already belongs to the team");
		}
		return playerView(playerRepository.save(new Player(
				request.name().trim(), request.role().trim(), team)));
	}

	public PlayerView updatePlayer(Long id, PlayerRequest request) {
		Player player = getPlayer(id);
		Team team = getTeam(request.teamId());
		if (playerRepository.existsByNameIgnoreCaseAndTeamIdAndIdNot(
				request.name().trim(), team.getId(), id)) {
			throw new IllegalArgumentException("A player with this name already belongs to the team");
		}
		player.setName(request.name().trim());
		player.setRole(request.role().trim());
		player.setTeam(team);
		return playerView(playerRepository.save(player));
	}

	public void deletePlayer(Long id) {
		playerRepository.delete(getPlayer(id));
	}

	public List<MatchView> getMatches() {
		return matchRepository.findAllByOrderByCreatedAtDesc().stream().map(this::matchView).toList();
	}

	public List<TournamentView> getTournaments() {
		return tournamentRepository.findAll().stream().map(this::tournamentView).toList();
	}

	public TournamentView createTournament(TournamentRequest request) {
		if (tournamentRepository.existsByNameIgnoreCase(request.name().trim())) {
			throw new IllegalArgumentException("Tournament name must be unique");
		}
		return tournamentView(tournamentRepository.save(new Tournament(
				request.name().trim(), request.format().trim())));
	}

	public List<StandingsRow> getStandings(Long tournamentId) {
		getTournament(tournamentId);
		Map<Long, TeamStandingAccumulator> standings = new LinkedHashMap<>();
		for (CricketMatch match : matchRepository.findByTournamentIdOrderByCreatedAtAsc(tournamentId)) {
			TeamStandingAccumulator batting = standings.computeIfAbsent(match.getBattingTeam().getId(),
					ignored -> new TeamStandingAccumulator(match.getBattingTeam()));
			TeamStandingAccumulator bowling = standings.computeIfAbsent(match.getBowlingTeam().getId(),
					ignored -> new TeamStandingAccumulator(match.getBowlingTeam()));
			if (match.getStatus() != MatchStatus.COMPLETED) {
				continue;
			}
			batting.played++;
			bowling.played++;
			if (match.getWinnerTeam() == null) {
				batting.drawn++;
				bowling.drawn++;
				batting.points++;
				bowling.points++;
			} else if (match.getWinnerTeam().getId().equals(batting.team.getId())) {
				batting.won++;
				batting.points += 2;
				bowling.lost++;
			} else {
				bowling.won++;
				bowling.points += 2;
				batting.lost++;
			}
		}
		return standings.values().stream()
				.sorted((first, second) -> {
					int pointsOrder = Integer.compare(second.points, first.points);
					return pointsOrder != 0 ? pointsOrder : Integer.compare(second.won, first.won);
				})
				.map(TeamStandingAccumulator::toRow).toList();
	}

	public MatchView createMatch(MatchRequest request) {
		if (request.battingTeamId().equals(request.bowlingTeamId())) {
			throw new IllegalArgumentException("A team cannot play against itself");
		}
		Tournament tournament = request.tournamentId() == null ? null : getTournament(request.tournamentId());
		CricketMatch match = new CricketMatch(getTeam(request.battingTeamId()),
				getTeam(request.bowlingTeamId()), request.venue().trim(), request.maxOvers(), tournament);
		return matchView(matchRepository.save(match));
	}

	public MatchView getMatch(Long matchId) {
		return matchView(getCricketMatch(matchId));
	}

	public MatchView completeMatch(Long matchId, Long winnerTeamId) {
		CricketMatch match = getCricketMatch(matchId);
		if (winnerTeamId == null) {
			match.setWinnerTeam(null);
		} else {
			Team winner = getTeam(winnerTeamId);
			if (!winner.getId().equals(match.getBattingTeam().getId())
					&& !winner.getId().equals(match.getBowlingTeam().getId())) {
				throw new IllegalArgumentException("Winner must be one of the match teams");
			}
			match.setWinnerTeam(winner);
		}
		match.setStatus(MatchStatus.COMPLETED);
		return matchView(matchRepository.save(match));
	}

	public List<BallView> getEvents(Long matchId) {
		getCricketMatch(matchId);
		return ballEventRepository.findByMatchIdOrderBySequenceNumberAsc(matchId).stream()
				.map(this::ballView).toList();
	}

	public BallView recordEvent(Long matchId, BallEventRequest request) {
		CricketMatch match = getCricketMatch(matchId);
		if (match.getStatus() == MatchStatus.COMPLETED) {
			throw new IllegalArgumentException("This match is already complete");
		}
		Player batter = getPlayer(request.batterId());
		Player bowler = getPlayer(request.bowlerId());
		if (!batter.getTeam().getId().equals(match.getBattingTeam().getId())) {
			throw new IllegalArgumentException("The batter must belong to the batting team");
		}
		if (!bowler.getTeam().getId().equals(match.getBowlingTeam().getId())) {
			throw new IllegalArgumentException("The bowler must belong to the bowling team");
		}
		validateEvent(request);

		List<BallEvent> existing = ballEventRepository.findByMatchIdOrderBySequenceNumberAsc(matchId);
		int legalBalls = (int) existing.stream().filter(BallEvent::isLegalDelivery).count();
		if (legalBalls >= match.getMaxOvers() * 6) {
			throw new IllegalArgumentException("The innings has reached its over limit");
		}
		boolean legal = request.type() != BallType.WIDE && request.type() != BallType.NO_BALL;
		int batterRuns = switch (request.type()) {
			case RUN, FOUR, SIX, NO_BALL -> request.runs();
			default -> 0;
		};
		int extraRuns = switch (request.type()) {
			case WIDE, NO_BALL -> request.extraRuns();
			case BYE, LEG_BYE -> request.runs();
			default -> 0;
		};
		int totalRuns = batterRuns + extraRuns;
		BallEvent event = new BallEvent(match, batter, bowler, request.type(), existing.size() + 1,
				legalBalls / 6, legalBalls % 6 + 1, batterRuns, extraRuns, totalRuns,
				legal, request.type() == BallType.WICKET);
		ballEventRepository.save(event);
		if (legalBalls + (legal ? 1 : 0) >= match.getMaxOvers() * 6
				|| existing.stream().filter(BallEvent::isWicket).count() + (event.isWicket() ? 1 : 0) >= 10) {
			match.setStatus(MatchStatus.COMPLETED);
			matchRepository.save(match);
		}
		return ballView(event);
	}

	public ScoreResponse getScore(Long matchId) {
		CricketMatch match = getCricketMatch(matchId);
		List<BallEvent> events = ballEventRepository.findByMatchIdOrderBySequenceNumberAsc(matchId);
		return score(match, events);
	}

	public SummaryResponse getSummary(Long matchId) {
		CricketMatch match = getCricketMatch(matchId);
		List<BallEvent> events = ballEventRepository.findByMatchIdOrderBySequenceNumberAsc(matchId);
		List<BattingFigure> batting = battingFigures(events);
		List<BowlingFigure> bowling = bowlingFigures(events);
		return new SummaryResponse(score(match, events), batting, bowling,
				events.stream().map(this::ballView).toList());
	}

	private void validateEvent(BallEventRequest request) {
		int runs = request.runs();
		int extras = request.extraRuns();
		boolean valid = switch (request.type()) {
			case RUN -> runs <= 3 && extras == 0;
			case FOUR -> runs == 4 && extras == 0;
			case SIX -> runs == 6 && extras == 0;
			case WICKET -> runs == 0 && extras == 0;
			case WIDE -> runs == 0 && extras >= 1;
			case NO_BALL -> extras >= 1;
			case BYE, LEG_BYE -> extras == 0;
		};
		if (!valid) {
			throw new IllegalArgumentException("Runs and extras do not match the selected ball type");
		}
	}

	private ScoreResponse score(CricketMatch match, List<BallEvent> events) {
		int legalBalls = (int) events.stream().filter(BallEvent::isLegalDelivery).count();
		int runs = events.stream().mapToInt(BallEvent::getTotalRuns).sum();
		int wickets = (int) events.stream().filter(BallEvent::isWicket).count();
		List<BattingFigure> batting = battingFigures(events);
		List<BowlingFigure> bowling = bowlingFigures(events);
		Set<Long> dismissed = new HashSet<>();
		events.stream().filter(BallEvent::isWicket).forEach(event -> dismissed.add(event.getBatter().getId()));
		Set<Long> added = new HashSet<>();
		List<BattingFigure> currentBatsmen = new ArrayList<>();
		for (int index = events.size() - 1; index >= 0 && currentBatsmen.size() < 2; index--) {
			Long playerId = events.get(index).getBatter().getId();
			if (!dismissed.contains(playerId) && added.add(playerId)) {
				batting.stream().filter(line -> line.playerId().equals(playerId)).findFirst()
						.ifPresent(currentBatsmen::add);
			}
		}
		BowlingFigure currentBowler = events.isEmpty() ? null : bowling.stream()
				.filter(line -> line.playerId().equals(events.get(events.size() - 1).getBowler().getId()))
				.findFirst().orElse(null);
		List<BallView> recent = events.stream().skip(Math.max(0, events.size() - 6L))
				.map(this::ballView).toList();
		return new ScoreResponse(match.getId(), teamView(match.getBattingTeam()),
				teamView(match.getBowlingTeam()), match.getVenue(), match.getMaxOvers(), match.getStatus(),
				runs, wickets, legalBalls, overs(legalBalls), legalBalls == 0 ? 0.0 : round(runs * 6.0 / legalBalls),
				currentBatsmen, currentBowler, recent);
	}

	private List<BattingFigure> battingFigures(List<BallEvent> events) {
		Map<Long, BattingAccumulator> figures = new LinkedHashMap<>();
		for (BallEvent event : events) {
			BattingAccumulator figure = figures.computeIfAbsent(event.getBatter().getId(),
					ignored -> new BattingAccumulator(event.getBatter()));
			figure.runs += event.getBatterRuns();
			if (event.isLegalDelivery() || event.getType() == BallType.NO_BALL) {
				figure.balls++;
			}
			if (event.getType() == BallType.FOUR) {
				figure.fours++;
			}
			if (event.getType() == BallType.SIX) {
				figure.sixes++;
			}
			figure.out |= event.isWicket();
		}
		return figures.values().stream().map(BattingAccumulator::toFigure).toList();
	}

	private List<BowlingFigure> bowlingFigures(List<BallEvent> events) {
		Map<Long, BowlingAccumulator> figures = new LinkedHashMap<>();
		for (BallEvent event : events) {
			BowlingAccumulator figure = figures.computeIfAbsent(event.getBowler().getId(),
					ignored -> new BowlingAccumulator(event.getBowler()));
			if (event.isLegalDelivery()) {
				figure.legalBalls++;
			}
			if (event.getType() != BallType.BYE && event.getType() != BallType.LEG_BYE) {
				figure.runs += event.getTotalRuns();
			}
			if (event.isWicket()) {
				figure.wickets++;
			}
		}
		return figures.values().stream().map(BowlingAccumulator::toFigure).toList();
	}

	private BallView ballView(BallEvent event) {
		String label = switch (event.getType()) {
			case WICKET -> "W";
			case WIDE -> "Wd+" + event.getExtraRuns();
			case NO_BALL -> "Nb+" + event.getTotalRuns();
			case BYE -> "B" + event.getExtraRuns();
			case LEG_BYE -> "Lb" + event.getExtraRuns();
			default -> Integer.toString(event.getBatterRuns());
		};
		return new BallView(event.getOverNumber(), event.getBallNumber(), event.getType(), label,
				event.getTotalRuns(), event.isWicket(), event.getBatter().getName(), event.getBowler().getName());
	}

	private TeamView teamView(Team team) {
		return new TeamView(team.getId(), team.getName(), team.getShortName());
	}

	private PlayerView playerView(Player player) {
		return new PlayerView(player.getId(), player.getName(), player.getRole(), teamView(player.getTeam()));
	}

	private MatchView matchView(CricketMatch match) {
		return new MatchView(match.getId(), teamView(match.getBattingTeam()), teamView(match.getBowlingTeam()),
				match.getVenue(), match.getMaxOvers(), match.getStatus(),
				match.getTournament() == null ? null : new TournamentView(match.getTournament().getId(),
						match.getTournament().getName(), match.getTournament().getFormat()),
				match.getWinnerTeam() == null ? null : teamView(match.getWinnerTeam()));
	}

	private TournamentView tournamentView(Tournament tournament) {
		return new TournamentView(tournament.getId(), tournament.getName(), tournament.getFormat());
	}

	private Team getTeam(Long id) {
		return teamRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Team " + id + " was not found"));
	}

	private Player getPlayer(Long id) {
		return playerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Player " + id + " was not found"));
	}

	private CricketMatch getCricketMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Match " + id + " was not found"));
	}

	private Tournament getTournament(Long id) {
		return tournamentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tournament " + id + " was not found"));
	}

	private String overs(int balls) {
		return (balls / 6) + "." + (balls % 6);
	}

	private double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	private static class BattingAccumulator {
		private final Player player;
		private int runs;
		private int balls;
		private int fours;
		private int sixes;
		private boolean out;

		private BattingAccumulator(Player player) {
			this.player = player;
		}

		private BattingFigure toFigure() {
			return new BattingFigure(player.getId(), player.getName(), runs, balls, fours, sixes, out);
		}
	}

	private static class BowlingAccumulator {
		private final Player player;
		private int legalBalls;
		private int runs;
		private int wickets;

		private BowlingAccumulator(Player player) {
			this.player = player;
		}

		private BowlingFigure toFigure() {
			return new BowlingFigure(player.getId(), player.getName(),
					(legalBalls / 6) + "." + (legalBalls % 6), 0, runs, wickets, legalBalls);
		}
	}

	private static class TeamStandingAccumulator {
		private final Team team;
		private int played;
		private int won;
		private int lost;
		private int drawn;
		private int points;

		private TeamStandingAccumulator(Team team) {
			this.team = team;
		}

		private StandingsRow toRow() {
			return new StandingsRow(new TeamView(team.getId(), team.getName(), team.getShortName()),
					played, won, lost, drawn, points);
		}
	}
}