package com.cricketscore.cricket_score;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;

import com.cricketscore.cricket_score.dto.Requests.BallEventRequest;
import com.cricketscore.cricket_score.dto.Requests.MatchRequest;
import com.cricketscore.cricket_score.dto.Requests.TournamentRequest;
import com.cricketscore.cricket_score.dto.ScoreDtos.ScoreResponse;
import com.cricketscore.cricket_score.dto.ScoreDtos.StandingsRow;
import com.cricketscore.cricket_score.model.BallType;
import com.cricketscore.cricket_score.model.Player;
import com.cricketscore.cricket_score.model.Team;
import com.cricketscore.cricket_score.repository.PlayerRepository;
import com.cricketscore.cricket_score.repository.TeamRepository;
import com.cricketscore.cricket_score.service.CricketService;

@SpringBootTest
@AutoConfigureMockMvc
class CricketScoringTests {
	@Autowired
	private CricketService cricketService;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Transactional
	void extrasDoNotAdvanceOversAndWicketDoesNotEndInnings() {
		Team india = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("IND")).findFirst().orElseThrow();
		Team australia = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("AUS")).findFirst().orElseThrow();
		List<Player> batters = playerRepository.findByTeamIdOrderByNameAsc(india.getId());
		List<Player> bowlers = playerRepository.findByTeamIdOrderByNameAsc(australia.getId());
		Long matchId = cricketService.createMatch(new MatchRequest(india.getId(), australia.getId(), "Test Ground", 2)).id();

		cricketService.recordEvent(matchId, event(BallType.RUN, batters.get(0), bowlers.get(0), 1, 0));
		cricketService.recordEvent(matchId, event(BallType.WIDE, batters.get(0), bowlers.get(0), 0, 1));
		cricketService.recordEvent(matchId, event(BallType.FOUR, batters.get(0), bowlers.get(0), 4, 0));
		cricketService.recordEvent(matchId, event(BallType.NO_BALL, batters.get(0), bowlers.get(0), 2, 1));
		cricketService.recordEvent(matchId, event(BallType.BYE, batters.get(0), bowlers.get(0), 2, 0));
		cricketService.recordEvent(matchId, event(BallType.SIX, batters.get(0), bowlers.get(0), 6, 0));
		cricketService.recordEvent(matchId, event(BallType.WICKET, batters.get(0), bowlers.get(0), 0, 0));

		ScoreResponse score = cricketService.getScore(matchId);
		assertThat(score.runs()).isEqualTo(17);
		assertThat(score.wickets()).isEqualTo(1);
		assertThat(score.legalBalls()).isEqualTo(5);
		assertThat(score.overs()).isEqualTo("0.5");
		assertThat(score.status().name()).isEqualTo("LIVE");
		assertThat(cricketService.getSummary(matchId).bowlingFigures().get(0).runs()).isEqualTo(15);
	}

	@Test
	void liveScoreApiReturnsSeededIndiaAustraliaMatch() throws Exception {
		mockMvc.perform(get("/api/matches"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").exists());
		Long matchId = cricketService.getMatches().get(0).id();
		mockMvc.perform(get("/api/matches/{matchId}/score", matchId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.battingTeam.shortName").value("IND"))
				.andExpect(jsonPath("$.status").value("LIVE"))
				.andExpect(jsonPath("$.legalBalls").value(10));
	}

	@Test
	@Transactional
	void rejectsAnInvalidWide() {
		Team india = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("IND")).findFirst().orElseThrow();
		Team australia = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("AUS")).findFirst().orElseThrow();
		Player batter = playerRepository.findByTeamIdOrderByNameAsc(india.getId()).get(0);
		Player bowler = playerRepository.findByTeamIdOrderByNameAsc(australia.getId()).get(0);
		Long matchId = cricketService.createMatch(new MatchRequest(india.getId(), australia.getId(), "Test Ground", 2)).id();

		assertThatThrownBy(() -> cricketService.recordEvent(matchId,
				event(BallType.WIDE, batter, bowler, 1, 0)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("do not match");
	}

	@Test
	@Transactional
	void tournamentStandingsAwardPointsToTheRecordedWinner() {
		Team india = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("IND")).findFirst().orElseThrow();
		Team australia = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("AUS")).findFirst().orElseThrow();
		var tournament = cricketService.createTournament(new TournamentRequest("College Cup", "T20"));
		var match = cricketService.createMatch(new MatchRequest(india.getId(), australia.getId(),
				"Campus Ground", 20, tournament.id()));

		cricketService.completeMatch(match.id(), india.getId());

		List<StandingsRow> table = cricketService.getStandings(tournament.id());
		assertThat(table).hasSize(2);
		assertThat(table.get(0).team().shortName()).isEqualTo("IND");
		assertThat(table.get(0).points()).isEqualTo(2);
		assertThat(table.get(1).points()).isZero();
	}

	@Test
	@Transactional
	void managementApisCreateTournamentPlayerAndFixture() throws Exception {
		Team india = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("IND")).findFirst().orElseThrow();
		Team australia = teamRepository.findAll().stream().filter(team -> team.getShortName().equals("AUS")).findFirst().orElseThrow();
		String tournamentResponse = mockMvc.perform(post("/api/tournaments")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"REST Cup\",\"format\":\"T20\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("REST Cup"))
				.andReturn().getResponse().getContentAsString();
		int tournamentId = com.jayway.jsonpath.JsonPath.read(tournamentResponse, "$.id");

		mockMvc.perform(post("/api/players")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"New Batter\",\"role\":\"Batter\",\"teamId\":" + india.getId() + "}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("New Batter"));

		mockMvc.perform(post("/api/matches")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"battingTeamId\":" + india.getId() + ",\"bowlingTeamId\":" + australia.getId()
						+ ",\"venue\":\"REST Ground\",\"maxOvers\":20,\"tournamentId\":" + tournamentId + "}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tournament.name").value("REST Cup"));
	}

	private BallEventRequest event(BallType type, Player batter, Player bowler, int runs, int extras) {
		return new BallEventRequest(type, batter.getId(), bowler.getId(), runs, extras);
	}
}