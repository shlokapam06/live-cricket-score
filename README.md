# Live Cricket Score Management System

A beginner-friendly Spring Boot system for managing cricket teams, players, matches, and ball-by-ball score updates. It includes JSON REST APIs and a responsive scorekeeper dashboard at `/`.

## Stack

- Java 17, Spring Boot 4, Spring Web MVC, Spring Data JPA, Bean Validation, Lombok
- MySQL for the default persistent database
- H2 file storage for a local demo and H2 memory storage for tests
- HTML, CSS, and JavaScript dashboard

## Run it

Install JDK 17 or newer. Start MySQL and create a user that can create the `cricket_score` database, or create it yourself. Default connection values are `root` / `password`; override them in PowerShell as needed:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-password"
$env:DB_URL = "jdbc:mysql://localhost:3306/cricket_score?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
.\mvnw.cmd spring-boot:run
```

Hibernate creates or updates tables at startup. A sample India vs Australia LIVE match is inserted only when there are no matches in the database. Open [http://localhost:8080](http://localhost:8080). The dashboard refreshes every four seconds and includes controls for recording deliveries.

To run without MySQL, use the persistent local H2 profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

The local H2 database is stored under `data/`.

## REST API

All bodies and responses are JSON. Validation and scoring errors return HTTP 400 with a `message`; missing resources return HTTP 404. Team and player deletion can be rejected by the database while they are referenced by a match or event.

| Method | Endpoint | Description |
| --- | --- | --- |
| GET, POST | `/api/teams` | List or create teams |
| PUT, DELETE | `/api/teams/{teamId}` | Update or delete a team |
| GET | `/api/players?teamId={teamId}` | List players, optionally filtered by team |
| POST | `/api/players` | Create a player |
| PUT, DELETE | `/api/players/{playerId}` | Update or delete a player |
| GET, POST | `/api/tournaments` | List or create tournaments |
| GET | `/api/tournaments/{tournamentId}/standings` | Tournament points table |
| GET, POST | `/api/matches` | List matches or create a match/innings |
| GET | `/api/matches/{matchId}` | Get match details |
| PATCH | `/api/matches/{matchId}/complete` | Complete a match and optionally record its winner |
| GET, POST | `/api/matches/{matchId}/events` | List deliveries or record a delivery |
| GET | `/api/matches/{matchId}/score` | Live score, current batters/bowler, recent six deliveries |
| GET | `/api/matches/{matchId}/summary` | Score, batting and bowling figures, and full innings |

Create a team with `POST /api/teams`:

```json
{"name":"New Zealand","shortName":"NZ"}
```

Create a player with `POST /api/players`:

```json
{"name":"Player Name","role":"Batter","teamId":1}
```

Create a match with `POST /api/matches`:

```json
{"battingTeamId":1,"bowlingTeamId":2,"venue":"Melbourne Cricket Ground","maxOvers":20}
```

Add `"tournamentId":1` to associate the fixture with a tournament. Complete a match with `PATCH /api/matches/{matchId}/complete` and a body such as `{"winnerTeamId":1}`; use an empty body for a draw. Standings award two points for a win and one for a draw.

Record a delivery with `POST /api/matches/{matchId}/events`. The batter and bowler IDs must belong to the match's batting and bowling teams.

```json
{"type":"NO_BALL","batterId":3,"bowlerId":14,"runs":2,"extraRuns":1}
```

Supported event types are `RUN`, `FOUR`, `SIX`, `WICKET`, `WIDE`, `NO_BALL`, `BYE`, and `LEG_BYE`. `RUN` accepts 0-3 batter runs; `FOUR` requires 4; `SIX` requires 6; `WICKET` requires 0. A `WIDE` needs `runs: 0` and `extraRuns` of at least 1. A `NO_BALL` requires `extraRuns` of at least 1 and can also award batter runs. For `BYE` and `LEG_BYE`, put the number of runs in `runs` and set `extraRuns` to 0.

Wides and no-balls add runs but do not advance overs. Byes and leg-byes advance overs but are not charged to the bowler. Statistics are calculated from the persisted events. This assignment version manages one innings per match; an innings completes at its over limit or ten wickets.

## Tests and package

Tests use an isolated in-memory H2 database:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```
