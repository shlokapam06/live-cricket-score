package com.cricketscore.cricket_score.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cricketscore.cricket_score.model.BallEvent;
import com.cricketscore.cricket_score.model.BallType;
import com.cricketscore.cricket_score.model.CricketMatch;
import com.cricketscore.cricket_score.model.Player;
import com.cricketscore.cricket_score.model.Team;
import com.cricketscore.cricket_score.repository.BallEventRepository;
import com.cricketscore.cricket_score.repository.MatchRepository;
import com.cricketscore.cricket_score.repository.PlayerRepository;
import com.cricketscore.cricket_score.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {
	private final TeamRepository teamRepository;
	private final PlayerRepository playerRepository;
	private final MatchRepository matchRepository;
	private final BallEventRepository ballEventRepository;

	@Override
	public void run(String... args) {
		if (matchRepository.count() > 0) {
			return;
		}

		Team india = teamRepository.save(new Team("India", "IND"));
		Team australia = teamRepository.save(new Team("Australia", "AUS"));
		List<Player> indiaPlayers = createSquad(india, new String[] {
				"Rohit Sharma", "Virat Kohli", "Shubman Gill", "KL Rahul", "Hardik Pandya",
				"Ravindra Jadeja", "Rishabh Pant", "Jasprit Bumrah", "Mohammed Siraj",
				"Kuldeep Yadav", "Arshdeep Singh"
		});
		List<Player> australiaPlayers = createSquad(australia, new String[] {
				"David Warner", "Travis Head", "Steve Smith", "Marnus Labuschagne", "Glenn Maxwell",
				"Alex Carey", "Pat Cummins", "Mitchell Starc", "Josh Hazlewood", "Adam Zampa", "Marcus Stoinis"
		});

		CricketMatch match = matchRepository.save(new CricketMatch(india, australia, "Ahmedabad", 20));
		int legalBalls = 0;
		int sequence = 0;
		sequence = saveEvent(match, indiaPlayers.get(0), australiaPlayers.get(7), BallType.RUN, 1, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(0), australiaPlayers.get(7), BallType.FOUR, 4, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(0), australiaPlayers.get(7), BallType.SIX, 6, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(0), australiaPlayers.get(7), BallType.WIDE, 0, 1, legalBalls, sequence);
		sequence = saveEvent(match, indiaPlayers.get(0), australiaPlayers.get(7), BallType.RUN, 2, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(0), australiaPlayers.get(7), BallType.BYE, 1, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(0), australiaPlayers.get(7), BallType.WICKET, 0, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(1), australiaPlayers.get(9), BallType.RUN, 1, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(1), australiaPlayers.get(9), BallType.FOUR, 4, 0, legalBalls++, sequence);
		sequence = saveEvent(match, indiaPlayers.get(2), australiaPlayers.get(9), BallType.SIX, 6, 0, legalBalls++, sequence);
		saveEvent(match, indiaPlayers.get(2), australiaPlayers.get(9), BallType.RUN, 1, 0, legalBalls, sequence);
	}

	private List<Player> createSquad(Team team, String[] names) {
		List<Player> players = new ArrayList<>();
		for (int index = 0; index < names.length; index++) {
			String role = index < 4 ? "Batter" : index < 7 ? "All-rounder" : "Bowler";
			players.add(playerRepository.save(new Player(names[index], role, team)));
		}
		return players;
	}

	private int saveEvent(CricketMatch match, Player batter, Player bowler, BallType type,
			int runs, int extraRuns, int legalBallsBefore, int sequence) {
		boolean legal = type != BallType.WIDE && type != BallType.NO_BALL;
		int batterRuns = switch (type) {
			case RUN, FOUR, SIX, NO_BALL -> runs;
			default -> 0;
		};
		int extras = switch (type) {
			case WIDE, NO_BALL -> extraRuns;
			case BYE, LEG_BYE -> runs;
			default -> 0;
		};
		BallEvent event = new BallEvent(match, batter, bowler, type, sequence + 1,
				legalBallsBefore / 6, legalBallsBefore % 6 + 1, batterRuns, extras,
				batterRuns + extras, legal, type == BallType.WICKET);
		ballEventRepository.save(event);
		return sequence + 1;
	}
}